#!/usr/bin/env bash
# Two-way sync between docs/knowledge/ and the GitHub repo wiki.
#
# Model: git is the merge engine on BOTH sides - the wiki is itself a git
# repo (<repo>.wiki.git). Content flows both ways per-page, three-way
# merged against a recorded base (KB_DIR/.gh-wiki-sync/ - commit it,
# never hand-edit). Structure flows UP: the wiki has no hierarchy UI, so
# the docs/knowledge/ tree owns the layout - page names encode the path
# (Architecture-Decisions-Foo) and a generated _Sidebar.md shows the
# tree. A page created fresh in the wiki UI lands at the KB root on pull,
# reported for filing; moving it into a section renames the page on the
# next sync.
#
# Mapping: docs/knowledge/README.md <-> Home; a section's README.md <->
# the section page (Architecture-Decisions); leaves <-> path-joined
# pages. _Sidebar.md is generated - wiki-side edits to it are
# overwritten. A local move shows up as a page rename (content match);
# move+edit degrades to delete+create - pass --allow-delete after a
# reorganize to prune the old page.
#
# Usage: gh-wiki-sync.sh [KB_DIR] [options]
#   KB_DIR          sync domain (default ./docs/knowledge)
#   --repo O/R      GitHub repo (default: tracker.repo from
#                   .agents/config/story-tools.json)
#   --pull-only     apply wiki -> local only; local changes reported pending
#   --allow-delete  a locally deleted file deletes its wiki page
#   --new-section N approve publishing a top-level section not seen before
#                   (repeatable). Sub-groups inside a known section need no
#                   approval - only a new top-level directory does.
#   --force         bootstrap over a non-empty KB_DIR with no sync state:
#                   local files are adopted and pushed as pages
#   --dry-run       print the full action plan; change nothing anywhere
#   --help          this text
#
# Conflicts get git-style markers and are NEVER pushed until the markers
# are gone - resolve locally, then sync again.
# Exit codes: 0 ok, 1 error, 2 completed but conflicts need resolution.
set -uo pipefail

KB_DIR=""; REPO=""; DRY=0; PULL_ONLY=0; ALLOW_DELETE=0; FORCE=0; NEW_SECTIONS=""
while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo) REPO="$2"; shift 2;;
    --pull-only) PULL_ONLY=1; shift;;
    --allow-delete) ALLOW_DELETE=1; shift;;
    --new-section) NEW_SECTIONS="$NEW_SECTIONS $2"; shift 2;;
    --force) FORCE=1; shift;;
    --dry-run) DRY=1; shift;;
    --help) awk 'NR>1 && !/^#/{exit} NR>1{sub(/^# ?/,""); print}' "$0"; exit 0;;
    *) KB_DIR="$1"; shift;;
  esac
done
KB_DIR="${KB_DIR:-./docs/knowledge}"

command -v git >/dev/null 2>&1 || { echo "error: git is required" >&2; exit 1; }
command -v python3 >/dev/null 2>&1 || { echo "error: python3 is required" >&2; exit 1; }

read_pointer() {
  local f="./.agents/config/story-tools.json"
  [[ -f "$f" ]] || return 0
  sed -nE 's/.*"'"$1"'": *"?([^",}]+)"?.*/\1/p' "$f" | head -1
}
[[ -z "$REPO" ]] && REPO="$(read_pointer repo || true)"

# GH_WIKI_URL overrides the remote entirely (used by the test harness).
if [[ -z "${GH_WIKI_URL:-}" ]]; then
  [[ -z "$REPO" ]] && { echo "usage: gh-wiki-sync.sh [KB_DIR] --repo owner/repo" >&2; exit 1; }
  CONN="$(read_pointer connection || true)"
  if [[ -z "${GITHUB_TOKEN:-}" && -n "$CONN" && -f "$HOME/.agents/story-tools/connections/$CONN.env" ]]; then
    # shellcheck disable=SC1090
    source "$HOME/.agents/story-tools/connections/$CONN.env"
  fi
  if [[ -z "${GITHUB_TOKEN:-}" && -f "$HOME/.agents/story-tools/connections/github.env" ]]; then
    # shellcheck disable=SC1091
    source "$HOME/.agents/story-tools/connections/github.env"
  fi
  if [[ -z "${GITHUB_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
    GITHUB_TOKEN="$(gh auth token 2>/dev/null || true)"
  fi
  [[ -z "${GITHUB_TOKEN:-}" ]] && { echo "error: no GitHub token - run the story-tools installer" >&2; exit 1; }
  WIKI_URL="https://x-access-token:${GITHUB_TOKEN}@github.com/${REPO}.wiki.git"
else
  WIKI_URL="$GH_WIKI_URL"
fi

# ---- capability check + clone ---------------------------------------------
if ! git ls-remote "$WIKI_URL" >/dev/null 2>&1; then
  if [[ -n "$REPO" && -n "${GITHUB_TOKEN:-}" ]]; then
    has_wiki="$(curl -sfS -m 10 -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Accept: application/vnd.github+json" \
      "https://api.github.com/repos/$REPO" 2>/dev/null \
      | grep -oE '"has_wiki": *(true|false)' | grep -oE 'true|false' || true)"
    if [[ "$has_wiki" == "false" ]]; then
      echo "error: the wiki is disabled on $REPO (Settings > Features; private repos need a paid plan)" >&2
    elif [[ "$has_wiki" == "true" ]]; then
      echo "error: the wiki on $REPO is enabled but uninitialized - create the Home page once in the web UI, then re-run" >&2
    else
      echo "error: cannot reach the wiki repo for $REPO (check the token's Contents permission)" >&2
    fi
  else
    echo "error: cannot reach the wiki repo at $WIKI_URL" >&2
  fi
  exit 1
fi

TMP="$(mktemp -d)"; trap 'rm -rf "$TMP"' EXIT
git clone -q "$WIKI_URL" "$TMP/wiki" || { echo "error: wiki clone failed" >&2; exit 1; }

export KB_DIR REPO WIKI_URL DRY PULL_ONLY ALLOW_DELETE FORCE NEW_SECTIONS
export WIKI_CLONE="$TMP/wiki"
python3 <<'EOF'
import os, re, subprocess, sys, datetime, tempfile

KB   = os.environ['KB_DIR'].rstrip('/')
W    = os.environ['WIKI_CLONE']
REPO = os.environ.get('REPO') or os.environ['WIKI_URL']
DRY  = os.environ['DRY'] == '1'
PULL = os.environ['PULL_ONLY'] == '1'
ADEL = os.environ['ALLOW_DELETE'] == '1'
FORCE = os.environ['FORCE'] == '1'
NEWSEC = set(os.environ.get('NEW_SECTIONS', '').split())

STATE = os.path.join(KB, '.gh-wiki-sync')
BASE  = os.path.join(STATE, 'base')
MANF  = os.path.join(STATE, 'manifest.tsv')

def run(*args, cwd=None, check=True, capture=False):
    return subprocess.run(args, cwd=cwd, check=check,
                          capture_output=capture, text=True)

def read(p):
    with open(p, encoding='utf-8') as f: return f.read()
def write(p, s):
    os.makedirs(os.path.dirname(p) or '.', exist_ok=True)
    with open(p, 'w', encoding='utf-8') as f: f.write(s)

def cap(seg):
    return '-'.join(w[:1].upper() + w[1:] for w in re.split(r'[-_ ]+', seg) if w)

# CHANGING THIS BREAKS THE NEW-SECTION GUARD - fix both in one commit.
# The guard decides "this section is already published" by testing live page
# names against page_for(sec + '/README.md') + '-', which encodes the
# flattening separator. Give pages real paths and every section fails the
# test, so the sync hard-exits on every run asking to approve sections that
# have existed for months. Replace the prefix test with a directory-existence
# check against the wiki clone at the same time: that tests the fact rather
# than a name derived from it, and survives any later naming change.
def page_for(path):          # rel path -> wiki page name
    p = path[:-3] if path.endswith('.md') else path
    if p == 'README': return 'Home'
    if p.endswith('/README'): p = p[:-len('/README')]
    return '-'.join(cap(re.sub(r'[^A-Za-z0-9._ -]+', '', s)) for s in p.split('/'))

# ---- link notation ---------------------------------------------------------
# Repo-relative is the authoring form and stays that way on disk. The wiki has
# a flat page namespace, so a link correct in the repo dead-ends there. The
# INVARIANT: wiki form is the comparison and merge currency; the working tree
# is the only thing in repo-relative form. Every read of a local file for
# comparison or merging goes through wikify(); every write back to one goes
# through unwikify(); base/ and the clone are always wiki form.
LINK = re.compile(r'(\]\()([^)\s]+)(\))')
ESCAPED = []                 # real targets with no page - served by the repo
UNRESOLVED = []              # link targets that name nothing at all

IMG_EXT = ('.png', '.jpg', '.jpeg', '.gif', '.svg', '.webp', '.avif')

BLOB_RE = re.compile(r'^https://github\.com/' + re.escape(REPO)
                     + r'/blob/HEAD/([^)\s?#]+)(?:\?raw=1)?$')

def _blob(rel):              # HEAD resolves to the repo's default branch
    url = f"https://github.com/{REPO}/blob/HEAD/{rel}"
    # An embed needs the bytes, not the file's HTML page. '?raw=1' is served
    # through github.com, so it carries the reader's session - a raw.github-
    # usercontent URL would 404 for everyone on a private repo.
    return url + '?raw=1' if rel.lower().endswith(IMG_EXT) else url

def wikify(text, pth):       # repo-relative -> wiki page names
    d = os.path.dirname(pth)
    def sub(mo):
        pre, t, post = mo.groups()
        if re.match(r'^(https?:|mailto:|#)', t): return mo.group(0)
        body, sep, frag = t.partition('#')
        if not body: return mo.group(0)
        full = os.path.normpath(os.path.join(KB, d, body))
        # Does it name something real? A target already in page form has no
        # extension either, and resolving it would invent a path. Anything
        # that does not exist is left alone: a page name stays a page name
        # (so wikify is idempotent), and a typo stays visibly broken rather
        # than becoming a plausible URL that 404s.
        if not os.path.exists(full):
            # A page name has neither a slash nor '.md', so it is left in
            # peace. Anything that LOOKS like a path and resolves to nothing
            # is a broken cross-reference in the repo - say so.
            if '/' in body or body.endswith('.md'):
                UNRESOLVED.append(f"{pth} -> {body}")
            return mo.group(0)
        rel = os.path.relpath(full, KB)
        inside = not rel.startswith('..')
        # A page exists only for a .md FILE inside the sync domain. page_for
        # strips '.md' and prefixes anything else, so it must not be handed a
        # directory, an image, or a non-markdown file - and none of those has
        # a page to link to anyway.
        if inside and body.endswith('.md') and os.path.isfile(full):
            return pre + page_for(rel) + sep + frag + post
        # Everything else real - a directory, an image, a file outside the
        # KB - is served by the repo, not the wiki. Relative to the REPO
        # ROOT, which is where this runs and where the pointer lives.
        out = os.path.relpath(full, os.path.abspath('.'))
        if out.startswith('..'): return mo.group(0)   # outside the repo
        ESCAPED.append(f"{pth} -> {body}")
        return pre + _blob(out) + sep + frag + post
    return LINK.sub(sub, text)

def unwikify(text, pth):     # wiki page names -> repo-relative
    d = os.path.dirname(pth)
    def sub(mo):
        pre, t, post = mo.groups()
        # A blob URL is what wikify emitted for a non-page target. Turning it
        # back makes the pair a true inverse: without this, one round trip
        # would leave an absolute URL sitting in the working tree.
        m = BLOB_RE.match(t)
        if m:
            full = os.path.join(os.path.abspath('.'), m.group(1))
            # only if it still names something: a dead blob URL turned into a
            # dead relative path is harder to see, not easier
            if os.path.exists(full):
                return pre + os.path.relpath(full, os.path.join(KB, d)) + post
            return mo.group(0)
        if re.match(r'^(https?:|mailto:|#)', t): return mo.group(0)
        body, sep, frag = t.partition('#')
        tgt = pairs_path(body)
        if not tgt: return mo.group(0)
        return pre + os.path.relpath(tgt, d or '.') + sep + frag + post
    return LINK.sub(sub, text)

def pairs_path(pg):          # page name -> local path, this run or recorded
    for pth, g in pairs.items():
        if g == pg: return pth
    return pages_of.get(pg)

def same_text(text, path):
    try: return text == read(path)
    except OSError: return False

def title_for(path):         # sidebar label: the document's own H1
    # The good title is in the file. A filename is a slug - typed prefix,
    # hyphens, no punctuation - so deriving the label from it loses words
    # and question marks that were part of the title. Display only: the
    # sidebar is regenerated every sync and nothing references these.
    # Page NAMES still come from the path; see the note above page_for().
    try:
        m = re.search(r'^#\s+(.+?)\s*$', read(os.path.join(KB, path)), re.M)
        if m:
            # ] and [ would break the markdown link the label sits inside
            return m.group(1).replace('[', '(').replace(']', ')')
    except OSError:
        pass
    p = path[:-3] if path.endswith('.md') else path
    if p.endswith('/README'): p = p[:-len('/README')]
    return cap(os.path.basename(p)).replace('-', ' ')

def has_markers(p):
    try: return bool(re.search(r'^<{7}( |$)', read(p), re.M))
    except OSError: return False

def same(a, b):
    try: return read(a) == read(b)
    except OSError: return False

# ---- gather ----------------------------------------------------------------
manifest = {}                      # path -> page
if os.path.isfile(MANF):
    for line in read(MANF).splitlines():
        if '\t' in line:
            pth, pg = line.split('\t', 1)
            manifest[pth] = pg
pages_of = {v: k for k, v in manifest.items()}

local_files = []
for root, dirs, files in os.walk(KB):
    dirs[:] = [d for d in dirs if d not in ('.gh-wiki-sync', '.yt-sync')]
    for f in files:
        if f.endswith('.md'):
            local_files.append(os.path.relpath(os.path.join(root, f), KB))
local_files.sort()

wiki_pages = sorted(f[:-3] for f in os.listdir(W)
                    if f.endswith('.md') and f not in ('_Sidebar.md', '_Footer.md'))

# A new TOP-LEVEL directory is a new public section - a claim that the
# project has a kind of knowledge it did not have before, published as a
# side effect of an agent creating a folder. Sub-groups inside a known
# section inherit their parent's meaning and publish status, so they pass
# without ceremony. Only interrupt for the line that actually matters.
if os.path.isfile(MANF):
    # Known means PUBLISHED UNDER THAT SECTION. The manifest is sync state an
    # agent may rewrite, and its path column is the field a staged rename
    # touches - so reading sections from it lets one already-published page
    # moved into a fresh directory mark that directory approved. The wiki's
    # own page names cannot be rewritten locally, so ask them instead.
    # Forward through page_for() rather than trying to invert it: a section
    # like case-studies becomes the two tokens Case-Studies, and splitting a
    # page name on '-' cannot tell where the directory ends.
    # This is a PROXY for "the section exists on the wiki", and it holds only
    # while page names encode the path. See the note above page_for(): under
    # directory mirroring this must become a directory-existence check, in
    # the same commit, or it flags every section on every run.
    live = set(wiki_pages)
    found = {p.split('/')[0] for p in local_files if '/' in p}
    unapproved = []
    for sec in sorted(found - NEWSEC):
        pfx = page_for(sec + '/README.md')        # section -> its page prefix
        if not any(pg == pfx or pg.startswith(pfx + '-') for pg in live):
            unapproved.append(sec)
    if unapproved:
        print("error: this sync would publish new top-level section(s):",
              file=sys.stderr)
        for sec in unapproved:
            n = sum(1 for p in local_files if p.split('/')[0] == sec)
            print(f"    {sec}/  ({n} page{'s' if n != 1 else ''})",
                  file=sys.stderr)
        print("  Sections are created by people, so this needs your nod once.",
              file=sys.stderr)
        print("  A section named in the taxonomy is fine - approve it and it",
              file=sys.stderr)
        print("  never asks again. One an agent invented usually belongs inside",
              file=sys.stderr)
        print("  an existing section instead: a sub-group like research/studies/",
              file=sys.stderr)
        print("  needs no approval and publishes fine.",
              file=sys.stderr)
        print("  Meant it? " + " ".join(f"--new-section {s}" for s in unapproved),
              file=sys.stderr)
        sys.exit(1)

if not os.path.isfile(MANF) and local_files and not FORCE:
    print(f"error: {KB} is non-empty but has no sync state.", file=sys.stderr)
    print("  --force adopts every local file as a wiki page (and pulls wiki-only pages).", file=sys.stderr)
    sys.exit(1)

PUSHED, PULLED, MERGED, RENAMED, NEWWIKI, DELETED, PENDING, CONFLICTS = ([] for _ in range(8))
wiki_dirty = False
pairs = {}                         # final path -> page

def wiki_mv(old, new):
    global wiki_dirty
    if DRY: return
    r = subprocess.run(['git', 'mv', old + '.md', new + '.md'], cwd=W,
                       capture_output=True, text=True)
    if r.returncode != 0:
        os.replace(os.path.join(W, old + '.md'), os.path.join(W, new + '.md'))
    b = os.path.join(BASE, old + '.md')
    if os.path.isfile(b): os.replace(b, os.path.join(BASE, new + '.md'))
    wiki_dirty = True

def wiki_rm(pg):
    global wiki_dirty
    if DRY: return
    r = subprocess.run(['git', 'rm', '-q', pg + '.md'], cwd=W,
                       capture_output=True, text=True)
    if r.returncode != 0:
        try: os.remove(os.path.join(W, pg + '.md'))
        except OSError: pass
    try: os.remove(os.path.join(BASE, pg + '.md'))
    except OSError: pass
    wiki_dirty = True

def push_page(pth, pg):
    global wiki_dirty
    if DRY: return
    src = os.path.join(KB, pth)
    body = wikify(read(src), pth)
    write(os.path.join(W, pg + '.md'), body)
    write(os.path.join(BASE, pg + '.md'), body)
    wiki_dirty = True

# ---- pass 1: known pairings ------------------------------------------------
for pth in sorted(manifest):
    pg = manifest[pth]
    L, R, B = os.path.join(KB, pth), os.path.join(W, pg + '.md'), os.path.join(BASE, pg + '.md')
    l_ex, r_ex = os.path.isfile(L), os.path.isfile(R)

    if not l_ex and not r_ex:
        continue                                   # gone both sides

    if not l_ex:
        moved = next((c for c in local_files
                      if c not in manifest and c not in pairs
                      and os.path.isfile(B)
                      and same_text(wikify(read(os.path.join(KB, c)), c), B)), None)
        if moved:
            newpg = page_for(moved)
            RENAMED.append(f"{pg} -> {newpg} ({moved})")
            if not PULL: wiki_mv(pg, newpg)
            pairs[moved] = newpg
        elif ADEL and not PULL:
            DELETED.append(f"{pg} (local file {pth} removed)")
            wiki_rm(pg)
        else:
            PENDING.append(f"{pth} deleted locally - page '{pg}' kept (use --allow-delete)")
            pairs[pth] = pg
        continue

    if not r_ex:                                   # page deleted in the UI
        if os.path.isfile(B) and same_text(wikify(read(L), pth), B):
            PULLED.append(f"{pth} (page '{pg}' deleted in wiki - file pruned)")
            if not DRY:
                os.remove(L)
                try: os.remove(B)
                except OSError: pass
        else:
            CONFLICTS.append(f"{pth}: page '{pg}' deleted in wiki but the file has local edits - keep or delete it, then sync")
            pairs[pth] = pg
        continue

    expected = page_for(pth)
    if expected != pg:                             # local move of a live file
        RENAMED.append(f"{pg} -> {expected} ({pth})")
        if not PULL: wiki_mv(pg, expected)
        pg = expected
        # wiki_mv is a no-op under --dry-run, so the page and its base are
        # still at the OLD names. Repointing R/B at the post-rename paths
        # would stat files the dry run declined to create, and every rename
        # would read as a conflict - which made dry-run useless on exactly
        # the reorganizations it exists to preview. Report the new name;
        # keep reading where the content is.
        if not DRY:
            R, B = os.path.join(W, pg + '.md'), os.path.join(BASE, pg + '.md')
    pairs[pth] = pg

    if has_markers(L):
        CONFLICTS.append(f"{pth} still has conflict markers - not pushed")
        continue

    l_chg = not (os.path.isfile(B) and same_text(wikify(read(L), pth), B))
    r_chg = not (os.path.isfile(B) and same(R, B))

    if not l_chg and not r_chg:
        continue
    if l_chg and not r_chg:
        if PULL: PENDING.append(f"{pth} (local edit not pushed)"); continue
        PUSHED.append(f"{pth} -> {pg}")
        push_page(pth, pg)
    elif r_chg and not l_chg:
        PULLED.append(f"{pg} -> {pth}")
        if not DRY:
            write(L, unwikify(read(R), pth)); write(B, read(R))
    else:                                          # both changed: 3-way merge
        base = B if os.path.isfile(B) else os.devnull
        # merge in wiki form on all three sides. Feeding the raw working tree
        # here would show every link line as a local edit - a notation
        # difference, not a change - and conflict on every cross-reference.
        fd, ltmp = tempfile.mkstemp(suffix='.md'); os.close(fd)
        write(ltmp, wikify(read(L), pth))
        r = subprocess.run(['git', 'merge-file', '-p',
                            '-L', f'{pth} (local)', '-L', 'base', '-L', f'wiki:{pg}',
                            ltmp, base, R], capture_output=True, text=True)
        os.unlink(ltmp)
        if r.returncode == 0:
            MERGED.append(f"{pth} <-> {pg}")
            if not DRY:
                write(L, unwikify(r.stdout, pth))
                if PULL:
                    PENDING.append(f"{pth} (merged locally, push pending)")
                    write(B, read(R))
                else:
                    push_page(pth, pg)
        elif r.returncode > 0 and r.returncode < 128:
            CONFLICTS.append(f"{pth} (vs page '{pg}')")
            if not DRY:
                # markers are not links, so this is safe to map back; the
                # resolved file re-wikifies on the next push
                write(L, unwikify(r.stdout, pth))
                write(B, read(R))   # base := wiki; resolved local pushes next sync
        else:
            CONFLICTS.append(f"{pth}: merge failed ({r.stderr.strip()})")

# ---- pass 2: new local files -----------------------------------------------
for pth in local_files:
    if pth in pairs or pth in manifest: continue
    pg = page_for(pth)
    holder = next((p for p, g in pairs.items() if g == pg), None)
    if holder:
        CONFLICTS.append(f"{pth}: page name '{pg}' collides with {holder} - rename one")
        continue
    L = os.path.join(KB, pth)
    if has_markers(L):
        CONFLICTS.append(f"{pth} still has conflict markers - not pushed"); continue
    if pg in wiki_pages and pg not in pages_of:
        # bootstrap: both sides have this page
        if same_text(wikify(read(L), pth), os.path.join(W, pg + '.md')):
            pairs[pth] = pg
            if not DRY:
                write(os.path.join(BASE, pg + '.md'), wikify(read(L), pth))
            continue
        if not FORCE:
            CONFLICTS.append(f"{pth} and wiki page '{pg}' differ with no sync state - reconcile by hand or --force (local wins)")
            continue
    if PULL:
        PENDING.append(f"{pth} (new file, push pending)"); pairs[pth] = pg; continue
    PUSHED.append(f"{pth} -> {pg} (new)")
    push_page(pth, pg)
    pairs[pth] = pg

# ---- pass 3: new wiki pages ------------------------------------------------
for pg in wiki_pages:
    if pg in pairs.values() or pg in pages_of: continue
    pth = 'README.md' if pg == 'Home' else pg + '.md'
    L = os.path.join(KB, pth)
    if os.path.isfile(L) and not same_text(wikify(read(L), pth),
                                           os.path.join(W, pg + '.md')):
        CONFLICTS.append(f"{pth} exists locally with different content than new wiki page '{pg}' - reconcile by hand")
        continue
    NEWWIKI.append(f"{pg} -> {pth}")
    if not DRY:
        write(L, unwikify(read(os.path.join(W, pg + '.md')), pth))
        write(os.path.join(BASE, pg + '.md'), read(os.path.join(W, pg + '.md')))
    pairs[pth] = pg

# ---- sidebar ---------------------------------------------------------------
def sidebar():
    lines = ["<!-- GENERATED by gh-wiki-sync from docs/knowledge/ - edits here are overwritten. Content pages are fine to edit; they sync back to the repo. -->",
             "**[Home](Home)**", ""]
    section_page = {os.path.dirname(p): pairs[p]
                    for p in pairs if os.path.basename(p) == 'README.md' and p != 'README.md'}
    emitted = set()
    def ensure_dir(d):
        if d in ('', '.') or d in emitted: return
        ensure_dir(os.path.dirname(d))
        emitted.add(d)
        depth = d.count('/')
        # a section's label is its README's H1, same as a leaf's
        readme = os.path.join(d, 'README.md')
        name = (title_for(readme) if os.path.isfile(os.path.join(KB, readme))
                else cap(os.path.basename(d)).replace('-', ' '))
        if d in section_page:
            lines.append('  ' * depth + f"- **[{name}]({section_page[d]})**")
        else:
            lines.append('  ' * depth + f"- **{name}**")
    for pth in sorted(pairs):
        if pth == 'README.md': continue
        d = os.path.dirname(pth)
        ensure_dir(d)
        if os.path.basename(pth) == 'README.md': continue   # emitted as its section
        depth = 0 if d in ('', '.') else d.count('/') + 1
        lines.append('  ' * depth + f"- [{title_for(pth)}]({pairs[pth]})")
    return '\n'.join(lines) + '\n'

if not DRY and not PULL:
    sb = sidebar()
    sb_path = os.path.join(W, '_Sidebar.md')
    if not (os.path.isfile(sb_path) and read(sb_path) == sb):
        write(sb_path, sb); wiki_dirty = True

# ---- commit + push + state -------------------------------------------------
if not DRY and not PULL and wiki_dirty:
    run('git', 'add', '-A', cwd=W)
    if subprocess.run(['git', 'diff', '--cached', '--quiet'], cwd=W).returncode != 0:
        run('git', '-c', 'user.name=story-tools docs sync',
            '-c', 'user.email=docs-sync@story-tools.local',
            'commit', '-q', '-m', f'docs sync: {datetime.date.today()}', cwd=W)
        r = subprocess.run(['git', 'push', '-q', 'origin', 'HEAD'], cwd=W,
                           capture_output=True, text=True)
        if r.returncode != 0:
            print(f"error: wiki push failed (token permissions?): {r.stderr.strip()}", file=sys.stderr)
            sys.exit(1)

if not DRY:
    os.makedirs(STATE, exist_ok=True)
    write(MANF, ''.join(f"{pth}\t{pg}\n" for pth, pg in sorted(pairs.items())))

# ---- report ----------------------------------------------------------------
print(f"gh-wiki-sync: {REPO} <-> {KB}" + (' (dry run)' if DRY else ''))
for title, items in (("Pushed:", PUSHED), ("Pulled:", PULLED), ("Merged:", MERGED),
                     ("Renamed pages:", RENAMED),
                     ("New from wiki (file these into a section):", NEWWIKI),
                     ("Deleted pages:", DELETED), ("Pending (not pushed):", PENDING)):
    if items:
        print(title)
        for i in items: print("  " + i)
if ESCAPED:
    uniq = sorted(set(ESCAPED))
    print(f"Links with no wiki page ({len(uniq)}) - published as github.com/…/blob/HEAD/… :")
    for e in uniq[:10]: print("  " + e)
    if len(uniq) > 10: print(f"  ... and {len(uniq)-10} more")
    print("  These 404 silently if the target moves - GitHub does not warn.")

if UNRESOLVED:
    uniq = sorted(set(UNRESOLVED))
    print(f"Broken links ({len(uniq)}) - these target nothing in the repo either:")
    for u in uniq[:10]: print("  " + u)
    if len(uniq) > 10: print(f"  ... and {len(uniq)-10} more")

if CONFLICTS:
    print("CONFLICTS - resolve the markers, then sync again:")
    for c in CONFLICTS: print("  " + c)
    sys.exit(2)
if not any((PUSHED, PULLED, MERGED, RENAMED, NEWWIKI, DELETED, PENDING)):
    print("Everything in sync.")
EOF
