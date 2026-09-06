#!/usr/bin/env bash
# Two-way sync between the project knowledge base and docs/knowledge/.
#
# Model: YouTrack is the organizing surface, git is the history and merge
# engine. Content flows BOTH ways per-article, three-way merged against a
# recorded base. Structure (hierarchy, titles) flows DOWN only - rearrange
# in YouTrack and the tree follows. The one exception: a NEW local file's
# location chooses its parent article at birth.
#
# Files and section directories are ID-prefixed like the stories snapshot
# (PROJ-A-12_title-slug.md); new local files are renamed to match once their
# article exists.
#
# Usage: yt-sync.sh [KB_DIR] [options]
#   KB_DIR          sync domain (default ./docs/knowledge)
#   --project KEY   project key (default: .agents/config/story-tools.json)
#   --root "Title"  restrict to the subtree of one top-level article;
#                   that article's body becomes KB_DIR/README.md
#   --pull-only     apply KB -> local only; local changes reported as pending
#   --allow-delete  a locally deleted file deletes its article (soft delete)
#   --force         bootstrap over a non-empty KB_DIR with no sync state:
#                   existing files are adopted and pushed as new articles
#   --dry-run       print the full action plan; change nothing anywhere
#   --help          this text
#
# Conflicts get git-style markers and are NEVER pushed until the markers
# are gone - resolve with your usual git tooling, then sync again.
# Exit codes: 0 ok, 1 error, 2 completed but conflicts need resolution.
set -euo pipefail

KB_DIR=""; PROJECT="${YOUTRACK_PROJECT:-}"; ROOT=""; DRY=0; PULL_ONLY=0; ALLOW_DELETE=0; FORCE=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --project) PROJECT="$2"; shift 2;;
    --root) ROOT="$2"; shift 2;;
    --pull-only) PULL_ONLY=1; shift;;
    --allow-delete) ALLOW_DELETE=1; shift;;
    --force) FORCE=1; shift;;
    --dry-run) DRY=1; shift;;
    --help) awk 'NR>1 && !/^#/{exit} NR>1{sub(/^# ?/,""); print}' "$0"; exit 0;;
    *) KB_DIR="$1"; shift;;
  esac
done
KB_DIR="${KB_DIR:-./docs/knowledge}"

command -v git >/dev/null 2>&1 || { echo "error: git is required (used for three-way merges)" >&2; exit 1; }

# Credentials. A named connection - explicit, or the one this project's pointer
# names - beats whatever is already exported in the shell. The other way round,
# a stale YOUTRACK_URL left over from another instance silently redirects the
# project at the wrong server and every lookup fails for the wrong reason.
# A connection file only wins as a pair: URL and token together, or not at all.
CONN_SOURCE="environment"
candidates=( )
[[ -n "${YOUTRACK_ENV_FILE:-}" ]] && candidates+=("$YOUTRACK_ENV_FILE")
conn="${YOUTRACK_CONNECTION:-${YOUTRACK_PROFILE:-}}"
if [[ -z "$conn" ]]; then
  for pf in "./.agents/config/story-tools.json" "./.agents/youtrack.json"; do
    [[ -f "$pf" ]] && { conn=$(sed -nE 's/.*"connection": *"([^"]+)".*/\1/p' "$pf" | head -1); break; }
  done
fi
[[ -n "$conn" ]] && candidates+=("$HOME/.agents/story-tools/connections/$conn.env")
if [[ -z "${YOUTRACK_URL:-}" ]]; then
  conns=( "$HOME"/.agents/story-tools/connections/*.env )
  [[ ${#conns[@]} -eq 1 && -f "${conns[0]}" ]] && candidates+=("${conns[0]}")
fi
for f in ${candidates[@]+"${candidates[@]}"}; do
  [[ -f "$f" ]] || continue
  prev_url="${YOUTRACK_URL:-}"; prev_token="${YOUTRACK_TOKEN:-}"
  unset YOUTRACK_URL YOUTRACK_HOST YOUTRACK_TOKEN YOUTRACK_API_TOKEN
  # shellcheck disable=SC1090
  source "$f"
  if [[ -n "${YOUTRACK_URL:-${YOUTRACK_HOST:-}}" && -n "${YOUTRACK_TOKEN:-${YOUTRACK_API_TOKEN:-}}" ]]; then
    CONN_SOURCE="$f"; break
  fi
  YOUTRACK_URL="$prev_url"; YOUTRACK_TOKEN="$prev_token"
done
YOUTRACK_URL="${YOUTRACK_URL:-${YOUTRACK_HOST:-}}"
YOUTRACK_TOKEN="${YOUTRACK_TOKEN:-${YOUTRACK_API_TOKEN:-}}"
[[ -z "$YOUTRACK_URL" || -z "$YOUTRACK_TOKEN" ]] && { echo "error: no YouTrack credentials found - run the story-tools installer" >&2; exit 1; }
if [[ -z "$PROJECT" ]]; then
  for pf in "./.agents/config/story-tools.json" "./.agents/youtrack.json"; do
    [[ -f "$pf" ]] && { PROJECT=$(sed -nE 's/.*"project": *"([^"]+)".*/\1/p' "$pf" | head -1); break; }
  done
fi
[[ -z "$PROJECT" ]] && { echo "error: no project key (--project or .agents/config/story-tools.json)" >&2; exit 1; }

export YOUTRACK_URL YOUTRACK_TOKEN PROJECT KB_DIR ROOT DRY PULL_ONLY ALLOW_DELETE FORCE CONN_SOURCE
python3 <<'EOF'
import json, os, re, shutil, subprocess, sys, tempfile, urllib.request, urllib.parse

URL = os.environ['YOUTRACK_URL'].rstrip('/')
TOKEN = os.environ['YOUTRACK_TOKEN']
PROJECT = os.environ['PROJECT']
KB = os.environ['KB_DIR'].rstrip('/')
ROOT = os.environ['ROOT']
DRY = os.environ['DRY'] == '1'
PULL_ONLY = os.environ['PULL_ONLY'] == '1'
ALLOW_DELETE = os.environ['ALLOW_DELETE'] == '1'
FORCE = os.environ['FORCE'] == '1'

SYNC = os.path.join(KB, '.yt-sync')
STATE_FILE = os.path.join(SYNC, 'state.json')
BASE_DIR = os.path.join(SYNC, 'base')

def api(path, payload=None, method=None):
    req = urllib.request.Request(
        URL + path,
        data=json.dumps(payload).encode() if payload is not None else None,
        headers={'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json'},
        method=method or ('POST' if payload is not None else 'GET'))
    with urllib.request.urlopen(req) as r:
        body = r.read()
        return json.loads(body) if body else None

def canon(s):
    return (s or '').replace('\r\n', '\n').rstrip()

# YouTrack renders `summary` as the article title. A heading in the body
# therefore shows up a SECOND time under it. So the H1 lives on disk (where
# a file needs a title and title_of reads it) and is stripped at the API
# boundary. The recorded base is kept in LOCAL form, so merges compare like
# with like and only these two functions know the difference.
H1_RE = re.compile(r'\A\s*#\s+(.+?)\s*(?:\n+|\Z)')

def to_local(summary, content):
    """Body as it lives on disk: opens with its title as an H1. YouTrack
    owns the title, so a rename there rewrites the local heading."""
    body = canon(content)
    m = H1_RE.match(body)
    if m:
        body = body[m.end():]
    title = (summary or '').strip() or (m.group(1).strip() if m else 'Untitled')
    return canon(f'# {title}\n\n{body}') if body else canon(f'# {title}')

def api_upload(path, filepath):
    # urllib has no multipart, and YouTrack wants one uniquely-named field
    # per file. Small enough to build by hand; avoids a dependency in a
    # script that has to run wherever the developer is.
    import uuid, mimetypes
    boundary = uuid.uuid4().hex
    name = os.path.basename(filepath)
    ctype = mimetypes.guess_type(name)[0] or 'application/octet-stream'
    with open(filepath, 'rb') as fh:
        data = fh.read()
    body = (f'--{boundary}\r\n'
            f'Content-Disposition: form-data; name="upload"; filename="{name}"\r\n'
            f'Content-Type: {ctype}\r\n\r\n').encode() + data + \
           f'\r\n--{boundary}--\r\n'.encode()
    req = urllib.request.Request(
        URL + path, data=body, method='POST',
        headers={'Authorization': 'Bearer ' + TOKEN,
                 'Content-Type': f'multipart/form-data; boundary={boundary}'})
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

def to_remote(content):
    """Body as YouTrack stores it: no leading H1."""
    body = canon(content)
    m = H1_RE.match(body)
    return canon(body[m.end():]) if m else body

def read_file(p):
    try:
        with open(p, encoding='utf-8') as f:
            return canon(f.read())
    except FileNotFoundError:
        return None

def write_file(p, content):
    os.makedirs(os.path.dirname(p) or '.', exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f:
        f.write(content + '\n' if content else '')

HAS_MARKERS = re.compile(r'^(<{7} |>{7} |={7}$)', re.M)

def merge3(base, local, remote):
    """git merge-file three-way. Returns (merged_text, clean)."""
    with tempfile.TemporaryDirectory() as td:
        pb, pl, pr = (os.path.join(td, n) for n in ('base', 'local', 'remote'))
        for p, c in ((pb, base), (pl, local), (pr, remote)):
            with open(p, 'w', encoding='utf-8') as f:
                f.write((c or '') + '\n')
        r = subprocess.run(
            ['git', 'merge-file', '-p', '-L', 'local', '-L', 'base (last sync)',
             '-L', 'YouTrack', pl, pb, pr],
            capture_output=True, text=True)
        return canon(r.stdout), r.returncode == 0

# ---- remote state -----------------------------------------------------------

projects = api(f'/api/admin/projects?fields=id,shortName&query={urllib.parse.quote(PROJECT)}')
pid = next((p['id'] for p in projects if p.get('shortName') == PROJECT), None)
if not pid:
    # Name the server and where it came from. The usual cause is not a missing
    # project but the wrong host, and neither is visible from the old message.
    visible = [p.get('shortName') for p in api('/api/admin/projects?fields=shortName&$top=200')]
    hint = ', '.join(sorted(s for s in visible if s)) or '(none visible to this token)'
    sys.exit(f'error: project {PROJECT} not found on {URL}\n'
             f'  credentials from: {os.environ.get("CONN_SOURCE", "environment")}\n'
             f'  projects visible there: {hint}')

arts, skip = [], 0
while True:
    batch = api('/api/articles?fields=id,idReadable,summary,content,parentArticle(id),project(shortName)'
                f'&$top=100&$skip={skip}')
    if not batch: break
    arts += [a for a in batch if (a.get('project') or {}).get('shortName') == PROJECT]
    if len(batch) < 100: break
    skip += 100

children = {}
for a in arts:
    children.setdefault((a.get('parentArticle') or {}).get('id'), []).append(a)

root_id = None
if ROOT:
    root = next((a for a in children.get(None, []) if a.get('summary') == ROOT), None)
    if not root:
        sys.exit(f'error: no top-level article "{ROOT}" in the {PROJECT} knowledge base')
    root_id = root['id']
    keep = set()
    def collect(i):
        keep.add(i)
        for c in children.get(i, []): collect(c['id'])
    collect(root_id)
    arts = [a for a in arts if a['id'] in keep]

by_id = {a['id']: a for a in arts}

def slug(text, maxlen=60):
    t = re.sub(r'[^A-Za-z0-9]+', '-', text or '').strip('-').lower()
    return t[:maxlen].rstrip('-') or 'untitled'

# desired path per article: structure flows down from here.
#
# LEAVES are ID-prefixed - PROJ-A-12_title-slug.md. The id is load-bearing
# there: a story citing a KB article resolves it by globbing
# docs/knowledge/**/PROJ-A-12_*.md, with no API call and no sync state. It is
# not recoverable from the body (measured on a 338-file KB: 1 file carried
# its own id).
#
# DIRECTORIES are not. Nothing cites a section by id, and the prefix lands on
# the most stable, most read part of the tree. A section's directory is the
# plain word the taxonomy pairs with its title - "Architecture Decision
# Records" is the article, `decisions/` is the folder - so the same tree
# comes out whether a project syncs to YouTrack, to a wiki, or to nothing.
# Anything with no taxonomy row (a pillar like "Product Development") is
# slugged.
#
# The table mirrors project-docs/references/taxonomy.md. If a row is added
# there, add it here - they are two copies on purpose, because the sync
# cannot read the skill.
SECTION_DIRS = {
    'architecture-decision-records': 'decisions',
    'architectural-decision-records': 'decisions',
    'product-requirements': 'requirements',
    'specifications': 'specifications',
    'research': 'research',
    'reference': 'reference',
    'developer-guides': 'guides',
    'quality-assurance': 'testing',
    'mandates-compliance': 'compliance',
    'mandates-and-compliance': 'compliance',
    'support': 'support',
    'documents': 'documents',
}

def dir_name(a):
    return SECTION_DIRS.get(slug(a.get('summary')), slug(a.get('summary')))

def art_name(a):
    return f"{a.get('idReadable') or a['id']}_{slug(a.get('summary'))}"

paths = {}
def assign(a, dirpath, taken=None):
    kids = sorted(children.get(a['id'], []), key=lambda x: (x.get('summary') or '', x['id']))
    if kids:
        n = dir_name(a)
        # two sibling sections could share a title; disambiguate only then,
        # rather than carrying an id on every directory for a rare case
        if taken is not None:
            if n in taken: n = f"{n}-{a.get('idReadable') or a['id']}"
            taken.add(n)
        d = os.path.join(dirpath, n)
        paths[a['id']] = os.path.join(d, 'README.md')
        sub = set()
        for c in kids: assign(c, d, sub)
    else:
        paths[a['id']] = os.path.join(dirpath, art_name(a) + '.md')

if root_id:
    paths[root_id] = os.path.join(KB, 'README.md')
    top = set()
    for c in sorted(children.get(root_id, []), key=lambda x: (x.get('summary') or '', x['id'])):
        assign(c, KB, top)
else:
    top = set()
    for a in sorted(children.get(None, []), key=lambda x: (x.get('summary') or '', x['id'])):
        assign(a, KB, top)

# ---- local state ------------------------------------------------------------

state = {'project': PROJECT, 'articles': {}}
if os.path.isfile(STATE_FILE):
    with open(STATE_FILE, encoding='utf-8') as f:
        state = json.load(f)
smap = state.setdefault('articles', {})

def local_md():
    found = []
    for dp, dns, fns in os.walk(KB):
        dns[:] = [d for d in dns if d != '.yt-sync']
        for fn in fns:
            if fn.endswith('.md'):
                found.append(os.path.join(dp, fn))
    return found

bootstrapping = not os.path.isfile(STATE_FILE)
if bootstrapping and os.path.isdir(KB) and local_md() and not FORCE:
    sys.exit(f'error: {KB} has markdown files but no sync state ({STATE_FILE}).\n'
             'Re-run with --force to adopt them: each file becomes a new article, '
             'pushed up on this first sync.')

report = {k: [] for k in ('Pulled', 'Pushed', 'Merged', 'CONFLICTS', 'Moved', 'Deleted', 'New', 'Notes')}
MOVES = {}          # normalised old path -> new path, for this run
conflict_count = 0

def base_path(aid): return os.path.join(BASE_DIR, aid + '.md')
def read_base(aid): return read_file(base_path(aid))
def put_base(aid, content):
    if not DRY: write_file(base_path(aid), content)
def drop_base(aid):
    if not DRY and os.path.isfile(base_path(aid)): os.remove(base_path(aid))

# normalize local moves: a file missing from its recorded path whose exact
# content shows up at an unrecorded path moved locally - move it back.
known = {e['path'] for e in smap.values()}
present = set(local_md())
unknown = sorted(present - known)
for aid, e in sorted(smap.items()):
    if e['path'] in present or e.get('orphaned'):
        continue
    b = read_base(aid)
    if b is None: continue
    match = next((u for u in unknown if read_file(u) == b), None)
    if match:
        if not DRY:
            os.makedirs(os.path.dirname(e['path']) or '.', exist_ok=True)
            shutil.move(match, e['path'])
        unknown.remove(match)
        present.discard(match); present.add(e['path'])
        report['Notes'].append(f'normalized local move: {match} -> {e["path"]} (structure flows down; rearrange in YouTrack)')

# ---- per-article sync -------------------------------------------------------

for aid in sorted(paths):
    a = by_id[aid]
    desired = paths[aid]
    remote = to_local(a.get('summary'), a.get('content'))
    e = smap.get(aid)

    if e is None:  # new in the KB
        if read_file(desired) is not None and desired not in known:
            # bootstrap collision: an adopted local file occupies this path
            report['Notes'].append(f'path collision at {desired}; keeping KB version, local copy at {desired}.local.md')
            if not DRY: shutil.move(desired, desired + '.local.md')
            unknown = [u for u in unknown if u != desired]
        if not DRY: write_file(desired, remote)
        put_base(aid, remote)
        smap[aid] = {'path': desired, 'summary': a.get('summary'), 'idReadable': a.get('idReadable')}
        report['Pulled'].append(f'{desired}  (new: "{a.get("summary")}")')
        continue

    e.pop('orphaned', None)  # article is back; clear any orphan flag
    cur = e['path']
    if cur != desired:  # KB rename / move
        if not DRY and os.path.isfile(cur):
            os.makedirs(os.path.dirname(desired) or '.', exist_ok=True)
            shutil.move(cur, desired)
        report['Moved'].append(f'{cur} -> {desired}')
        MOVES[os.path.normpath(cur)] = os.path.normpath(desired)
        e['path'] = desired
    e['summary'] = a.get('summary'); e['idReadable'] = a.get('idReadable')

    local = read_file(desired if not DRY else (desired if os.path.isfile(desired) else cur))
    base = read_base(aid)

    if local is None:  # locally deleted
        if ALLOW_DELETE and not PULL_ONLY:
            if not DRY: api(f'/api/articles/{aid}', method='DELETE')
            drop_base(aid); smap.pop(aid, None)
            report['Deleted'].append(f'article "{a.get("summary")}" ({a.get("idReadable")}) - deleted locally, removed from KB')
        else:
            report['Notes'].append(f'{desired} deleted locally but "{a.get("summary")}" still in KB - restore it, or re-run with --allow-delete')
        continue

    if HAS_MARKERS.search(local):
        conflict_count += 1
        report['CONFLICTS'].append(f'{desired}  (unresolved markers from a previous sync - resolve, then re-run)')
        continue

    lc, rc = local != (base or ''), remote != (base or '')
    if not lc and not rc:
        continue
    if lc and not rc:
        if PULL_ONLY:
            report['Notes'].append(f'{desired} has local edits (pending push; run without --pull-only)')
        else:
            if not DRY: api(f'/api/articles/{aid}?fields=id', {'content': to_remote(local)})
            put_base(aid, local)
            report['Pushed'].append(desired)
    elif rc and not lc:
        if not DRY: write_file(desired, remote)
        put_base(aid, remote)
        report['Pulled'].append(desired)
    else:
        merged, clean = merge3(base or '', local, remote)
        if clean:
            if PULL_ONLY:
                report['Notes'].append(f'{desired} merges cleanly with KB edits (pending; run without --pull-only)')
                continue
            if not DRY:
                write_file(desired, merged)
                api(f'/api/articles/{aid}?fields=id', {'content': to_remote(merged)})
            put_base(aid, merged)
            report['Merged'].append(desired)
        else:
            conflict_count += 1
            if not DRY: write_file(desired, merged)
            put_base(aid, remote)  # after resolution, local-vs-base drives the push
            report['CONFLICTS'].append(desired)

# ---- articles gone from the KB ---------------------------------------------

for aid in sorted(set(smap) - set(paths)):
    e = smap[aid]
    local = read_file(e['path'])
    base = read_base(aid)
    if local is None or local == (base or ''):
        if local is not None and not DRY:
            os.remove(e['path'])
        drop_base(aid); smap.pop(aid, None)
        report['Deleted'].append(f'{e["path"]}  ("{e.get("summary")}" removed in YouTrack)')
    else:
        e['orphaned'] = True
        report['CONFLICTS'].append(f'{e["path"]}  (edited locally but "{e.get("summary")}" was deleted in YouTrack - '
                                   'keep by moving/renaming it (becomes a new article) or delete the file)')
        conflict_count += 1

# ---- new local files -> new articles ---------------------------------------

def title_of(path, content):
    m = re.search(r'^#\s+(.+)$', content or '', re.M)
    if m: return m.group(1).strip()
    stem = os.path.splitext(os.path.basename(path))[0]
    if stem == 'README': stem = os.path.basename(os.path.dirname(path))
    return re.sub(r'[-_]+', ' ', stem).strip().title() or 'Untitled'

dir_article = {os.path.dirname(e['path']): aid for aid, e in smap.items()
               if os.path.basename(e['path']) == 'README.md' and not e.get('orphaned')}
if root_id: dir_article[KB] = root_id

orphan_paths = {e['path'] for e in smap.values() if e.get('orphaned')}

def natkey(text):
    """Natural sort: 2-plan before 10-plan, which plain lexical gets wrong.

    Creation order decides article IDs, and the ID becomes the filename
    once the article exists - so the order files are pushed in is the
    order the KB keeps forever. A numeric prefix on a new doc is how you
    say 'these belong in this sequence'; honour it."""
    out = []
    for tok in re.split(r'(\d+)', text):
        if not tok:
            continue
        out.append((0, int(tok), '') if tok.isdigit() else (1, 0, tok.lower()))
    return tuple(out)

# shallow before deep (a section article must exist before its children),
# README first within a directory, then natural order of the path.
new_files = sorted((u for u in unknown if u not in orphan_paths),
                   key=lambda p: (p.count(os.sep),
                                  os.path.basename(p) != 'README.md',
                                  natkey(p)))
created_ids = []

for nf in new_files:
    content = read_file(nf) or ''
    if HAS_MARKERS.search(content):
        conflict_count += 1
        report['CONFLICTS'].append(f'{nf}  (new file contains conflict markers; not pushed)')
        continue
    d = os.path.dirname(nf)
    # walk up to the nearest directory that has an owning article
    chain = []
    dd = d
    while dd != KB and dd not in dir_article:
        chain.append(dd); dd = os.path.dirname(dd)
    parent = dir_article.get(dd)  # None => top-level article
    if PULL_ONLY or DRY:
        report['New'].append(f'{nf} -> article "{title_of(nf, content)}"' + (' (pending)' if PULL_ONLY else ''))
        continue
    stalled = False
    for cd in reversed(chain):  # create stub section articles for new dirs
        rp = os.path.join(cd, 'README.md')
        # A directory still wearing <ID>_<slug> is the sync's own naming
        # mid-move, never a section someone created. Reachable from a normal
        # run: a childless section is flattened to a leaf, its README moves
        # out, the emptied directory looks new, and a section gets born
        # titled from the slug - "Bc A 1 Business Development". The layout is
        # computed from the server, so a document added locally in the same
        # run does not exist yet to rescue it.
        if re.match(r'^[A-Z][A-Z0-9]*-A-[0-9]+_', os.path.basename(cd)):
            report['Notes'].append(
                f'{nf} deferred: its ancestor {cd} looks like a directory '
                'mid-move rather than a new section, so no further sections '
                'and no document were created. A shallower new section above '
                'it may already exist and stay empty until the next sync - '
                'that is expected. Settle the move (or create the section in '
                'the KB) and sync again.')
            stalled = True
            break
        stub_title = title_of(rp, read_file(rp))
        payload = {'summary': stub_title, 'content': to_remote(read_file(rp) or ''), 'project': {'id': pid}}
        if parent: payload['parentArticle'] = {'id': parent}
        made = api('/api/articles?fields=id,idReadable', payload)
        smap[made['id']] = {'path': rp, 'summary': stub_title, 'idReadable': made.get('idReadable')}
        put_base(made['id'], read_file(rp) or '')
        if read_file(rp) is None: write_file(rp, '')
        dir_article[cd] = made['id']
        parent = made['id']
        created_ids.append(made['id'])
        report['New'].append(f'{rp} -> section "{stub_title}"')
    if stalled:
        # break only left the stub loop; without this the document would be
        # created anyway, parented to the last resolved ancestor - one or
        # more levels too high, silently, while Notes claimed nothing was
        # created. Leave it for the next sync.
        continue
    if os.path.basename(nf) == 'README.md' and d in dir_article:
        continue  # created above as the section article
    title = title_of(nf, content)
    payload = {'summary': title, 'content': to_remote(content), 'project': {'id': pid}}
    if parent: payload['parentArticle'] = {'id': parent}
    made = api('/api/articles?fields=id,idReadable', payload)
    smap[made['id']] = {'path': nf, 'summary': title, 'idReadable': made.get('idReadable')}
    put_base(made['id'], content)
    created_ids.append(made['id'])
    report['New'].append(f'{nf} -> article "{title}" ({made.get("idReadable")})')

# newborn articles get their ID stamped into the file/dir name (PROJ-A-12_...)
def settle_name(aid):
    e = smap.get(aid)
    if not e: return
    p = e['path']
    fake = {'id': aid, 'idReadable': e.get('idReadable'), 'summary': e.get('summary')}
    if os.path.basename(p) == 'README.md':
        old_dir = os.path.dirname(p)
        if old_dir == KB: return
        new_dir = os.path.join(os.path.dirname(old_dir), art_name(fake))
        if new_dir == old_dir: return
        if not DRY: shutil.move(old_dir, new_dir)
        for e2 in smap.values():
            if e2['path'] == old_dir or e2['path'].startswith(old_dir + os.sep):
                e2['path'] = new_dir + e2['path'][len(old_dir):]
        report['Notes'].append(f'renamed {old_dir}/ -> {new_dir}/ (ID prefix)')
    else:
        newp = os.path.join(os.path.dirname(p), art_name(fake) + '.md')
        if newp == p: return
        if not DRY: shutil.move(p, newp)
        e['path'] = newp
        report['Notes'].append(f'renamed {p} -> {newp} (ID prefix)')

for aid in created_ids:  # shallow-first: section dirs precede their leaves
    settle_name(aid)

# ---- finish -----------------------------------------------------------------

# ---- images referenced by an article become its attachments ----------------
# YouTrack resolves ![](name.png) in an article body against that article's
# own attachments, so the local markdown and the remote body carry the
# identical string - no notation transform, unlike the wiki.
#
# Upload only what is absent BY NAME, and never replace: re-uploading a name
# creates a SECOND attachment rather than replacing the first (JT-83227,
# JT-62753), and deleting the old one breaks the embed. So a changed image
# gets a new filename - that rule lives in the skill, and this code simply
# never touches a name that already exists. Stale attachments accumulate and
# are harmless; clearing them is a manual job.
IMG_RE = re.compile(r'!\[[^\]]*\]\(([^)\s]+)\)')

def refs_of(text):
    return [r for r in IMG_RE.findall(text)
            if not re.match(r'^(https?:|data:|#)', r)]

def api_download(url, dest):
    req = urllib.request.Request(
        url if url.startswith('http') else URL + url,
        headers={'Authorization': 'Bearer ' + TOKEN})
    with urllib.request.urlopen(req) as r:
        data = r.read()
    os.makedirs(os.path.dirname(dest) or '.', exist_ok=True)
    with open(dest, 'wb') as fh:
        fh.write(data)
    return len(data)

def pull_attachments():
    """An image added in the browser has no local file. Structure flows down,
    so fetch it - otherwise the document renders in the KB and is broken in
    the repo, and the next push reports it missing forever."""
    down, notes = [], []
    for aid, e in smap.items():
        if e.get('orphaned'):
            continue
        path = e.get('path') or ''
        text = read_file(path)
        if not text or '![' not in text:
            continue                       # no API call for the common case
        want = {}
        for r in refs_of(text):
            tgt = os.path.normpath(os.path.join(os.path.dirname(path),
                                                r.split('#')[0]))
            if not os.path.exists(tgt):
                want.setdefault(os.path.basename(r.split('#')[0]), tgt)
        if not want:
            continue                       # everything referenced is on disk
        try:
            atts = api(f'/api/articles/{aid}/attachments?fields=name,url')
        except Exception as ex:                                # noqa: BLE001
            notes.append(f'{path}: could not list attachments ({ex})')
            continue
        for a in atts:
            dest = want.get(a.get('name'))
            if not dest or not a.get('url'):
                continue
            if DRY:
                down.append(f'{a["name"]} -> {dest}')
                continue
            try:
                api_download(a['url'], dest)
                down.append(f'{a["name"]} -> {dest}')
            except Exception as ex:                            # noqa: BLE001
                notes.append(f'could not fetch {a.get("name")}: {ex}')
    if down:
        report['Pulled'].append(f'attachments ({len(down)}): ' + ', '.join(down))
    for n in notes:
        report['Notes'].append(n)

def push_attachments():
    if PULL_ONLY:
        return
    up, notes = [], []
    for aid, e in smap.items():
        if e.get('orphaned'):
            continue
        path = e.get('path') or ''
        text = read_file(path)
        if not text or '![' not in text:
            continue                       # no API call for the common case
        refs = refs_of(text)
        if not refs:
            continue
        try:
            have = {a.get('name') for a in
                    api(f'/api/articles/{aid}/attachments?fields=name')}
        except Exception as ex:                                # noqa: BLE001
            notes.append(f'{path}: could not list attachments ({ex})')
            continue
        for r in refs:
            name = os.path.basename(r.split('#')[0])
            if '/' in r.split('#')[0]:
                # YouTrack resolves an embed against the article's own
                # attachments, by filename. A path renders in the repo and
                # breaks in the KB, so uploading it would not be enough.
                notes.append(f'{path} references {r} by path - '
                             'YouTrack matches attachments by filename; '
                             'keep the image beside the document')
                continue
            if name in have:
                continue                   # present on both sides - leave it
            src = os.path.normpath(os.path.join(os.path.dirname(path), r))
            if not os.path.isfile(src):
                notes.append(f'{path} references {r} - not found locally')
                continue
            if DRY:
                up.append(f'{name} -> {e.get("idReadable") or aid}')
                continue
            try:
                api_upload(f'/api/articles/{aid}/attachments?fields=id,name', src)
                up.append(f'{name} -> {e.get("idReadable") or aid}')
                have.add(name)
            except Exception as ex:                            # noqa: BLE001
                notes.append(f'could not upload {name}: {ex}')
    if up:
        report['Notes'].append(f'attachments uploaded ({len(up)}): ' + ', '.join(up))
    for n in notes:
        report['Notes'].append(n)

pull_attachments()
push_attachments()

# ---- follow the renames into every link ------------------------------------
# Structure flows down, so the sync moves files whenever an article is
# retitled or re-parented - and until now it left every reference to the old
# path pointing at nothing, then printed a reminder asking a human to go and
# fix them. Measured on one KB mid-migration: 511 dead against 37 working.
# Two things shift at once and both are handled here: a link's TARGET may
# have moved, and the file HOLDING the link may have moved, which changes
# what its relative paths mean. Resolve against where the file was, remap
# through the moves, re-relativise to where it now is.
LINK_RE = re.compile(r'(\]\()([^)\s]+)(\))')

def follow_moves():
    if not MOVES: return
    new2old = {v: k for k, v in MOVES.items()}
    touched = 0
    for aid, e in smap.items():
        if e.get('orphaned'): continue
        new = os.path.normpath(e['path'])
        if not os.path.isfile(new): continue
        old = new2old.get(new, new)
        olddir, newdir = os.path.dirname(old), os.path.dirname(new)
        text = read_file(new)
        if text is None: continue
        def sub(mo):
            pre, t, post = mo.groups()
            if re.match(r'^(https?:|mailto:|#)', t): return mo.group(0)
            body, sep, frag = t.partition('#')
            if not body: return mo.group(0)
            tgt = os.path.normpath(os.path.join(olddir, body))
            tgt = MOVES.get(tgt, tgt)
            # only rewrite what actually resolves; a link that named nothing
            # before is a defect in the document, not something to invent a
            # new path for
            if not os.path.exists(tgt): return mo.group(0)
            return pre + os.path.relpath(tgt, newdir or '.') + sep + frag + post
        out = LINK_RE.sub(sub, text)
        if out != text:
            touched += 1
            if not DRY: write_file(new, out)
    if touched:
        report['Notes'].append(
            f'links updated to follow the moves above, in {touched} file(s)')

follow_moves()

if not DRY:
    # Prune dirs emptied by moves/deletes. Ask the filesystem, do not trust
    # os.walk's dns/fns: those are captured on the way DOWN, so a parent whose
    # only child this very walk removed still lists it and survives. That left
    # BC-A-2_product-development behind after its contents moved out.
    for dp, _dns, _fns in os.walk(KB, topdown=False):
        if dp in (KB, SYNC): continue
        try:
            if not os.listdir(dp): os.rmdir(dp)
        except OSError:
            pass
    os.makedirs(SYNC, exist_ok=True)
    with open(STATE_FILE, 'w', encoding='utf-8') as f:
        json.dump(state, f, indent=2, sort_keys=True)

prefix = '(dry run) ' if DRY else ''
total = sum(len(v) for v in report.values())
if total == 0:
    print(f'{prefix}In sync: {len(paths)} articles, nothing to do.')
else:
    for section, lines in report.items():
        if lines:
            print(f'{prefix}{section}:')
            for l in lines: print(f'  {l}')
    if report['Moved']:
        print(f'{prefix}Links inside docs/knowledge followed the moves. References from OUTSIDE it - AGENTS.md, README, docs/ indexes - are not rewritten; check those.')
sys.exit(2 if conflict_count else 0)
EOF
