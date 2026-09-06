#!/usr/bin/env bash
# Pull GitHub issues into a local markdown snapshot.
#
# Usage: gh-pull.sh [owner/repo] [OUT_DIR] [--dimensions-only] [--push-tags]
#   owner/repo  defaults to tracker.repo from .agents/config/story-tools.json
#   OUT_DIR     defaults to ./docs/stories
#   --dimensions-only   refresh .agents/config/dimensions.md only (no issue snapshot)
#   --push-tags         create a topic: label for each entry in
#                       .agents/config/topical-tags.md that has none
#
# Output: one .md per issue (OC-0123_title-slug.md; prefix from
# tracker.prefix, blocks OC-A-0001 past 9999) + INDEX.md; a
# dimensions.md (Project v2 single-select fields, repo labels, milestones)
# is written to the docs root beside OUT_DIR so offline/fallback agents
# pick from real values. Files are GENERATED - GitHub stays the source of
# truth. State comes from the project's Status column when the pointer
# names a project; otherwise open/closed.
#
# Token: $GITHUB_TOKEN, else `gh auth token`, else the story-tools
# github.env connection.
set -euo pipefail

DIMONLY=0; PUSH_TAGS=0; ARGS=()
for a in "$@"; do
  case "$a" in
    --dimensions-only) DIMONLY=1;;
    --push-tags)       PUSH_TAGS=1;;
    *)                 ARGS+=("$a");;
  esac
done
# Labels are one flat namespace, so the project's own groupings carry a
# prefix to keep them apart from the workflow machinery.
TAGS_FILE="${TOPICAL_TAGS_FILE:-.agents/config/topical-tags.md}"
TOPIC_PREFIX="${TOPICAL_LABEL_PREFIX:-topic:}"
export PUSH_TAGS TAGS_FILE TOPIC_PREFIX
set -- "${ARGS[@]:-}"
REPO="${1:-}"; OUT="${2:-./docs/stories}"
read_pointer() {
  local f="./.agents/config/story-tools.json"
  [[ -f "$f" ]] || return 0
  sed -nE 's/.*"'"$1"'": *"?([^",}]+)"?.*/\1/p' "$f" | head -1
}
[[ -z "$REPO" ]] && REPO="$(read_pointer repo)"
[[ -z "$REPO" ]] && { echo "usage: gh-pull.sh <owner/repo> [OUT_DIR]" >&2; exit 1; }
PROJ="$(read_pointer project)"; PROJ="${PROJ:-}"
PREFIX="$(read_pointer prefix)"; PREFIX="${PREFIX:-}"

CONN="$(read_pointer connection)"
if [[ -z "${GITHUB_TOKEN:-}" && -n "$CONN" && -f "$HOME/.agents/story-tools/connections/$CONN.env" ]]; then
  # shellcheck disable=SC1090
  source "$HOME/.agents/story-tools/connections/$CONN.env"
fi
if [[ -z "${GITHUB_TOKEN:-}" && -f "$HOME/.agents/story-tools/connections/github.env" ]]; then
  # shellcheck disable=SC1091
  source "$HOME/.agents/story-tools/connections/github.env"   # legacy shared credential
fi
if [[ -z "${GITHUB_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
  GITHUB_TOKEN="$(gh auth token 2>/dev/null || true)"
fi
[[ -z "${GITHUB_TOKEN:-}" ]] && { echo "error: no GitHub token (GITHUB_TOKEN, gh auth, or the story-tools installer's github connection)" >&2; exit 1; }

[[ "$DIMONLY" == 1 ]] || mkdir -p "$OUT"
export GITHUB_TOKEN REPO OUT PROJ DIMONLY PREFIX
python3 <<'EOF'
import datetime, json, os, re, urllib.request, urllib.parse

TOKEN = os.environ['GITHUB_TOKEN']; REPO = os.environ['REPO']
OUT = os.environ['OUT'].rstrip('/'); PROJ = os.environ.get('PROJ') or ''
DIMONLY = os.environ.get('DIMONLY') == '1'
OWNER, NAME = REPO.split('/', 1)
# short file prefix: pointer tracker.prefix, else first two letters of the
# repo name. OC-0001; past 9999 lexical sort survives via letter blocks:
# OC-9999 < OC-A-0000 < OC-B-0000 ...
PREFIX = (os.environ.get('PREFIX') or re.sub(r'[^A-Za-z]', '', NAME)[:2]).upper() or 'GH'

def fid(num):
    block, rem = divmod(num, 10000)
    return f"{PREFIX}-{rem:04d}" if block == 0 else f"{PREFIX}-{chr(64 + block)}-{rem:04d}"
DIM_DIR = os.path.join('.agents', 'config')   # generated reference data, not docs
os.makedirs(DIM_DIR, exist_ok=True)
HDRS = {'Authorization': 'Bearer ' + TOKEN, 'Accept': 'application/vnd.github+json'}

def rest(path):
    req = urllib.request.Request('https://api.github.com' + path, headers=HDRS)
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

def rest_post(path, payload):
    req = urllib.request.Request('https://api.github.com' + path,
                                 data=json.dumps(payload).encode(),
                                 headers={**HDRS, 'Content-Type': 'application/json'},
                                 method='POST')
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

PUSH_TAGS = os.environ.get('PUSH_TAGS') == '1'
TAGS_FILE = os.environ.get('TAGS_FILE') or '.agents/config/topical-tags.md'
TOPIC_PREFIX = os.environ.get('TOPIC_PREFIX') or 'topic:'

def wanted_tags():
    # Prose above a `---` rule is the header; tags live below it. Without a
    # rule the whole file is read, so a hand-made bare list works too.
    try:
        with open(TAGS_FILE, encoding='utf-8') as fh:
            raw = fh.read()
    except OSError:
        return []
    body = raw.split('\n---', 1)
    out = []
    for line in (body[1] if len(body) > 1 else body[0]).splitlines():
        t = line.strip().lstrip('-').strip()
        if not t or t.startswith('#'):
            continue
        if t not in out:
            out.append(t)
    return out

def gql(query, variables=None):
    req = urllib.request.Request('https://api.github.com/graphql',
        data=json.dumps({'query': query, 'variables': variables or {}}).encode(),
        headers={'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as r:
        out = json.loads(r.read())
    if out.get('errors'):
        raise SystemExit('GraphQL error: ' + '; '.join(e.get('message','?') for e in out['errors']))
    return out['data']

RESERVED = {'needs-gherkin', 'discovered', 'ready-for-agent', 'ready-for-human',
            'needs-triage', 'needs-info', 'triaged', 'bug', 'enhancement', 'wontfix',
            'duplicate', 'invalid', 'question', 'wontfix', 'help wanted', 'good first issue',
            'documentation'}

def slug(text, maxlen=60):
    t = re.sub(r'[^A-Za-z0-9]+', '-', text or '').strip('-').lower()
    return t[:maxlen].rstrip('-') or 'untitled'

# ---- issues (REST, paged; PRs filtered out) --------------------------------
issues, page = [], 1
while not DIMONLY:
    batch = rest(f'/repos/{REPO}/issues?state=all&per_page=100&page={page}&sort=created&direction=asc')
    if not batch: break
    issues += [i for i in batch if 'pull_request' not in i]
    if len(batch) < 100: break
    page += 1

# ---- project Status per issue + field values (GraphQL, when configured) ----
status_by_number, proj_fields = {}, []
if PROJ:
    meta = gql("""
query($owner: String!, $num: Int!) {
  repositoryOwner(login: $owner) {
    ... on ProjectV2Owner {
      projectV2(number: $num) {
        id title
        fields(first: 30) {
          nodes { ... on ProjectV2SingleSelectField { name options { name } } }
        }
      }
    }
  }
}""", {'owner': OWNER, 'num': int(PROJ)})
    proj = (meta.get('repositoryOwner') or {}).get('projectV2')
    if proj:
        proj_fields = [f for f in proj['fields']['nodes'] if f]
        cursor = None
        while not DIMONLY:
            items = gql("""
query($owner: String!, $num: Int!, $after: String) {
  repositoryOwner(login: $owner) {
    ... on ProjectV2Owner {
      projectV2(number: $num) {
        items(first: 100, after: $after) {
          pageInfo { hasNextPage endCursor }
          nodes {
            fieldValueByName(name: "Status") {
              ... on ProjectV2ItemFieldSingleSelectValue { name }
            }
            content { ... on Issue { number repository { nameWithOwner } } }
          }
        }
      }
    }
  }
}""", {'owner': OWNER, 'num': int(PROJ), 'after': cursor})
            block = meta_items = items['repositoryOwner']['projectV2']['items']
            for n in block['nodes']:
                c = n.get('content') or {}
                if c.get('repository', {}).get('nameWithOwner') == REPO and c.get('number'):
                    fv = n.get('fieldValueByName') or {}
                    status_by_number[c['number']] = fv.get('name') or ''
            if not block['pageInfo']['hasNextPage']: break
            cursor = block['pageInfo']['endCursor']

# ---- write snapshot --------------------------------------------------------
def write_if_changed(path, text):
    """Only touch the file when the bytes differ - see yt-pull.sh for why.

    Identical rewrites are invisible to git but not free: they throw away
    git's stat cache (so the next `git status` re-hashes everything),
    destroy mtimes, and wake every file watcher for nothing.
    """
    try:
        with open(path, encoding='utf-8') as fh:
            if fh.read() == text:
                return False
    except (OSError, UnicodeDecodeError):
        pass
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(text)
    return True

written = 0
index = []
for it in [] if DIMONLY else issues:
    num = it['number']
    state = status_by_number.get(num) or ('closed' if it['state'] == 'closed' else 'open')
    labels = ', '.join(l['name'] for l in it.get('labels') or [])
    milestone = (it.get('milestone') or {}).get('title', '') or ''
    assignee = ', '.join(a['login'] for a in it.get('assignees') or [])
    body = it.get('body') or '_(no description)_'
    iid = fid(num)
    fname = f"{iid}_{slug(it.get('title'))}.md"
    for stale in os.listdir(OUT):
        if stale == fname: continue
        # prune this issue's file under any earlier naming scheme
        if (stale.startswith(f"{iid}_") or stale.startswith(f"{num:04d}_")
                or stale.startswith(f"{NAME}-{num}_") or stale == f"{NAME}-{num}.md"):
            os.remove(os.path.join(OUT, stale))
    text = (f"""---
id: "#{num}"
summary: "{(it.get('title') or '').replace('"', "'")}"
state: "{state}"
resolved: {str(it['state'] == 'closed').lower()}
labels: "{labels}"
milestone: "{milestone}"
assignee: "{assignee}"
url: {it['html_url']}
---
<!-- GENERATED: do not edit - GitHub is the source of truth. Re-run scripts/gh-pull.sh to refresh. The pull date is in INDEX.md; keeping it out of every file means an issue changes here only when it changed upstream, so two people pulling do not conflict on unchanged files. -->

# #{num}: {it.get('title','')}

{body}
""")
    if write_if_changed(os.path.join(OUT, fname), text):
        written += 1
    index.append((num, fname, it.get('title',''), state, labels, it['state'] == 'closed'))

if not DIMONLY:
  idx = [f"# GitHub snapshot: {REPO} ({datetime.date.today()})", "",
         "GENERATED - do not edit. Re-run scripts/gh-pull.sh to refresh.", "",
         "| # | Summary | State | Labels | Closed |", "|---|---|---|---|---|"]
  for num, fn, t, st, lb, closed in index:
    idx.append(f"| [#{num}]({fn}) | {t} | {st} | {lb} | {'yes' if closed else ''} |")
  write_if_changed(os.path.join(OUT, 'INDEX.md'), "\n".join(idx) + "\n")

# ---- dimensions.md ---------------------------------------------------------
lines = [f'# Project dimensions: {REPO} ({datetime.date.today()})', '',
         'GENERATED by gh-pull - do not edit. Pick from these values; adding a',
         'new one is a deliberate act (see the triage skill), never a typo.', '']
for fld in proj_fields:
    opts = [o['name'] for o in fld.get('options') or []]
    if opts:
        lines.append(f"## {fld['name']} (Project field)")
        lines += [f'- {o}' for o in opts] + ['']
# Labels: list EVERY usable label, workflow ones included. Filtering the
# machinery out left agents unable to see that needs-triage exists, so
# they invented substitutes.
WORKFLOW = [
    ('needs-triage',    'awaiting triage - the inbox'),
    ('triaged',         'has been dispositioned; never removed once earned'),
    ('ready-for-agent', 'an agent can pick this up'),
    ('ready-for-human', 'needs a person - judgment, access, or design'),
    ('needs-info',      'waiting on the reporter'),
    ('wontfix',         'closed with the reason recorded'),
    ('bug',             'category: something is broken'),
    ('enhancement',     'category: new feature or improvement'),
    ('discovered',      'born from other work, not yet triaged'),
    ('needs-gherkin',   'completion requires a QA section'),
]
labels_all = rest(f'/repos/{REPO}/labels?per_page=100')

# ---- topical labels: the agents' own vocabulary -----------------------------
# Same list file as the YouTrack side; the push target differs. Labels are
# one flat namespace shared with the workflow machinery, so the project's
# groupings carry TOPIC_PREFIX to stay distinguishable. Additive only -
# deleting a label strips it from every issue that carries it. Reading is
# free, writing is explicit: a plain run reports what is pending.
tags_pending, tags_added, tags_note = [], [], None
want = wanted_tags()
if want:
    have = {l['name'].lower() for l in labels_all}
    tags_missing = [t for t in want
                    if (TOPIC_PREFIX + t).lower() not in have]
    if tags_missing and PUSH_TAGS:
        for t in tags_missing:
            try:
                rest_post(f'/repos/{REPO}/labels',
                          {'name': TOPIC_PREFIX + t, 'color': 'ededed'})
                tags_added.append(TOPIC_PREFIX + t)
            except Exception as e:                            # noqa: BLE001
                tags_note = f'could not create "{TOPIC_PREFIX}{t}": {e}'
                break
        if tags_added:
            labels_all = rest(f'/repos/{REPO}/labels?per_page=100')
    elif tags_missing:
        tags_pending = tags_missing

present = {l['name'].lower() for l in labels_all}
reserved_lower = {r.lower() for r in RESERVED}

lines.append('## Workflow labels (machinery - apply per the triage state machine)')
lines.append('')
missing = []
for name, meaning in WORKFLOW:
    if name.lower() in present:
        lines.append(f'- `{name}` - {meaning}')
    else:
        missing.append(name)
if missing:
    lines += ['', 'Not on this repo yet (run the installer to create them): '
                  + ', '.join(f'`{m}`' for m in missing)]
lines += ['', 'These are never topical and never inherited by discovered work.', '']

rest_labels = [l['name'] for l in labels_all
               if l['name'].lower() not in reserved_lower]
topical = sorted(t for t in rest_labels if t.lower().startswith(TOPIC_PREFIX.lower()))
other = sorted(t for t in rest_labels if not t.lower().startswith(TOPIC_PREFIX.lower()))

lines.append(f'## Topical labels ({TOPIC_PREFIX}...) - this project\'s groupings')
lines += ([f'- {t}' for t in topical] or
          [f'_(none yet - add a line to {TAGS_FILE}, then '
           f'gh-pull.sh --dimensions-only --push-tags)_']) + ['']
if other:
    lines.append('## Other repo labels')
    lines.append('')
    lines.append('Not machinery and not project groupings. Reuse one if it fits;')
    lines.append(f'new groupings go in {TAGS_FILE}, not here.')
    lines.append('')
    lines += [f'- {t}' for t in other] + ['']

miles = rest(f'/repos/{REPO}/milestones?state=all&per_page=100')
openm = [m['title'] for m in miles if m.get('state') == 'open']
closed = [m['title'] for m in miles if m.get('state') == 'closed']
if openm or closed:
    lines.append('## Milestones / versions (current and upcoming)')
    lines += ([f'- {t}' for t in openm] or
              ['- _(none open - creating one is a deliberate act)_'])
    if closed:
        lines += ['', 'Already shipped - history, do not target new work: '
                      + ', '.join(closed[-12:])
                      + ('' if len(closed) <= 12 else f' (+{len(closed) - 12} older)')]
    lines += ['']
write_if_changed(os.path.join(DIM_DIR, 'dimensions.md'), '\n'.join(lines))

mode = f'Projects mode (project {PROJ})' if PROJ else 'issues-only mode'
if DIMONLY:
    print(f'Wrote dimensions.md at {DIM_DIR}/ ({mode})')
else:
    print(f'{len(issues)} issues; {written} file(s) changed + INDEX.md in {OUT}; dimensions.md at {DIM_DIR}/ ({mode})')

if tags_added:
    print(f'  topical labels: created {len(tags_added)} from {TAGS_FILE}: '
          + ', '.join(tags_added))
if tags_pending:
    print(f'  topical labels: {len(tags_pending)} in {TAGS_FILE} not on the repo '
          'yet: ' + ', '.join(TOPIC_PREFIX + t for t in tags_pending))
    print('  Create them with: gh-pull.sh --dimensions-only --push-tags')
if tags_note:
    print(f'  topical labels: {tags_note}')
EOF
