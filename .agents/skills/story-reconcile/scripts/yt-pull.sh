#!/usr/bin/env bash
# Pull YouTrack stories into a local markdown snapshot.
#
# Usage: yt-pull.sh [PROJECT_KEY] [OUT_DIR] [--dimensions-only] [--push-tags]
#   --dimensions-only   refresh .agents/config/dimensions.md only (no issue snapshot)
#   --push-tags         add each entry in .agents/config/topical-tags.md that
#                       is missing to the Topical Tags field's value set
#   PROJECT_KEY  defaults to $YOUTRACK_PROJECT from the selected profile
#   OUT_DIR      defaults to ./docs/stories
#
# Connection selection: $YOUTRACK_CONNECTION, else the machine's only one,
# else the legacy env file. The snapshot is one .md per issue plus an
# INDEX.md; a dimensions.md (the project's field values - Subsystem,
# Type, Priority, Stage, Fix versions - plus existing topical tags, so
# offline/fallback agents can pick from real values instead of guessing)
# is written to .agents/config/ (tool-read reference data, never docs);
# files are GENERATED - YouTrack stays the source of truth.
set -euo pipefail

# A named connection - explicit, or the one this project's pointer names - beats
# whatever is already exported in the shell. The other way round, a stale
# YOUTRACK_URL left over from another instance silently redirects the project at
# the wrong server. A connection file only wins as a pair: URL and token, or
# neither.
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
[[ -z "$YOUTRACK_URL" || -z "$YOUTRACK_TOKEN" ]] && { echo "error: no YouTrack credentials found" >&2; exit 1; }

DIMONLY=0; PUSH_TAGS=0; ARGS=()
for a in "$@"; do
  case "$a" in
    --dimensions-only) DIMONLY=1;;
    --push-tags)       PUSH_TAGS=1;;
    *)                 ARGS+=("$a");;
  esac
done
# Which field holds the agents' own groupings, and where they keep the list.
# The list is theirs: they append to it as work suggests a grouping, and it is
# reviewable as a diff. Nothing here constrains what a tag looks like.
TAGS_FIELD="${TOPICAL_TAGS_FIELD:-Topical Tags}"
TAGS_FILE="${TOPICAL_TAGS_FILE:-.agents/config/topical-tags.md}"
export PUSH_TAGS TAGS_FIELD TAGS_FILE
set -- "${ARGS[@]:-}"

PROJECT="${1:-${YOUTRACK_PROJECT:-}}"
[[ -z "$PROJECT" ]] && { echo "usage: yt-pull.sh <PROJECT_KEY> [OUT_DIR]" >&2; exit 1; }
OUT="${2:-./docs/stories}"
[[ "$DIMONLY" == 1 ]] || mkdir -p "$OUT"

FIELDS="idReadable,summary,description,resolved,tags(name),customFields(name,value(name)),links(direction,linkType(name),issues(idReadable))"
TOP=100; SKIP=0; TOTAL=0
if [[ "$DIMONLY" != 1 ]]; then
: > /tmp/yt-pull-issues.jsonl

while :; do
  BATCH=$(curl -sS -G "$YOUTRACK_URL/api/issues" \
    -H "Authorization: Bearer $YOUTRACK_TOKEN" \
    --data-urlencode "query=project: {$PROJECT} sort by: {issue id} asc" \
    --data-urlencode "fields=$FIELDS" \
    --data-urlencode "\$top=$TOP" \
    --data-urlencode "\$skip=$SKIP")
  COUNT=$(printf '%s' "$BATCH" | python3 -c "import json,sys; print(len(json.load(sys.stdin)))")
  [[ "$COUNT" == "0" ]] && break
  printf '%s' "$BATCH" | python3 -c "
import json, sys
for it in json.load(sys.stdin):
    print(json.dumps(it))" >> /tmp/yt-pull-issues.jsonl
  TOTAL=$((TOTAL + COUNT)); SKIP=$((SKIP + TOP))
  [[ "$COUNT" -lt "$TOP" ]] && break
done

OUT="$OUT" URL="$YOUTRACK_URL" PROJECT="$PROJECT" python3 <<'EOF'
import json, os, re, datetime

out = os.environ['OUT']; url = os.environ['URL'].rstrip('/'); project = os.environ['PROJECT']
issues = [json.loads(l) for l in open('/tmp/yt-pull-issues.jsonl') if l.strip()]

def field(it, name):
    for f in it.get('customFields') or []:
        if f.get('name') == name:
            v = f.get('value')
            if isinstance(v, dict): return v.get('name')
            if isinstance(v, list): return ', '.join(x.get('name','') for x in v)
            return v
    return None

def slug(text, maxlen=60):
    t = re.sub(r'[^A-Za-z0-9]+', '-', text or '').strip('-').lower()
    return t[:maxlen].rstrip('-') or 'untitled'


def write_if_changed(path, text):
    """Only touch the file when the bytes differ.

    Git compares content, so an identical rewrite is invisible in a diff -
    but it is NOT free: git caches stat data to avoid hashing files it can
    see are untouched, and rewriting everything throws that away, so the
    next `git status` re-hashes the lot. It also destroys mtimes (you can
    no longer see which stories actually moved) and wakes every file
    watcher, indexer and folder-sync client for nothing.
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
for it in issues:
    iid = it['idReadable']
    state = next((v for v in (field(it, n) for n in ('Stage', 'Kanban State', 'Status', 'State')) if v), '')
    subsystem = field(it, 'Subsystem') or ''
    tags = ', '.join(t['name'] for t in it.get('tags') or [])
    links = []
    for l in it.get('links') or []:
        for li in l.get('issues') or []:
            links.append(f"{l.get('linkType',{}).get('name','link')} {li['idReadable']}")
    body = it.get('description') or '_(no description)_'
    # human-findable filename: PROJ-2_title-of-the-story.md; drop stale
    # copies of this issue from earlier pulls (old name or changed title)
    fname = f"{iid}_{slug(it.get('summary'))}.md"
    for stale in os.listdir(out):
        if (stale == iid + '.md' or stale.startswith(iid + '_')) and stale != fname:
            os.remove(os.path.join(out, stale))
    text = (f"""---
id: {iid}
summary: "{(it.get('summary') or '').replace('"', "'")}"
state: "{state}"
subsystem: "{subsystem}"
resolved: {str(bool(it.get('resolved'))).lower()}
tags: "{tags}"
links: "{'; '.join(links)}"
url: {url}/issue/{iid}
---
<!-- GENERATED: do not edit - YouTrack is the source of truth. Re-run scripts/yt-pull.sh to refresh. The pull date is in INDEX.md; keeping it out of every file means a story changes here only when the issue changed, so two people pulling do not conflict on 300 unchanged files. -->

# {iid}: {it.get('summary','')}

{body}
""")
    if write_if_changed(os.path.join(out, fname), text):
        written += 1
    index.append((iid, fname, it.get('summary',''), state, subsystem, bool(it.get('resolved'))))

idx = [f"# YouTrack snapshot: project {project} ({datetime.date.today()})", "",
       "GENERATED - do not edit. Re-run scripts/yt-pull.sh to refresh.", "",
       "| ID | Summary | Subsystem | State | Resolved |", "|---|---|---|---|---|"]
for iid, fn, s, st, sub, r in index:
    idx.append(f"| [{iid}]({fn}) | {s} | {sub} | {st} | {'yes' if r else ''} |")
write_if_changed(os.path.join(out, 'INDEX.md'), "\n".join(idx) + "\n")

print(f"{len(issues)} issues; {written} file(s) changed + INDEX.md in {out}")
EOF

fi

# dimensions.md - project field values + every usable tag, for offline
# picking. Lives at the docs ROOT (parent of the stories dir), with the
# agent's other indexes - git-native, never synced to the KB. Written on
# every run, including --dimensions-only.
DIM_DIR=".agents/config"   # generated reference data, not documentation
mkdir -p "$DIM_DIR"
DIM_DIR="$DIM_DIR" URL="$YOUTRACK_URL" TOKEN="$YOUTRACK_TOKEN" PROJECT="$PROJECT" python3 <<'EOF'
import json, os, datetime, urllib.request, urllib.parse

URL = os.environ['URL'].rstrip('/'); TOKEN = os.environ['TOKEN']
PROJECT = os.environ['PROJECT']; out = os.environ['DIM_DIR']

def write_if_changed(path, text):
    # Second python block - the snapshot block's copy is not in scope here.
    try:
        with open(path, encoding='utf-8') as fh:
            if fh.read() == text:
                return False
    except (OSError, UnicodeDecodeError):
        pass
    with open(path, 'w', encoding='utf-8') as fh:
        fh.write(text)
    return True

def api(path):
    req = urllib.request.Request(URL + path, headers={'Authorization': 'Bearer ' + TOKEN})
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

def api_post(path, payload):
    req = urllib.request.Request(
        URL + path, data=json.dumps(payload).encode(),
        headers={'Authorization': 'Bearer ' + TOKEN,
                 'Content-Type': 'application/json'}, method='POST')
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())

PUSH_TAGS = os.environ.get('PUSH_TAGS') == '1'
TAGS_FIELD = os.environ.get('TAGS_FIELD') or 'Topical Tags'
TAGS_FILE = os.environ.get('TAGS_FILE') or '.agents/config/topical-tags.md'

def wanted_tags():
    try:
        with open(TAGS_FILE, encoding='utf-8') as fh:
            raw = fh.read()
    except OSError:
        return []
    # Prose above a `---` rule is the header; tags live below it. Without a
    # rule the whole file is read, so a hand-made list of bare lines works
    # too. The seeded file has the rule - otherwise its own instructions
    # parse as tags, which is exactly what happened the first time.
    body = raw.split('\n---', 1)
    lines = (body[1] if len(body) > 1 else body[0]).splitlines()
    out = []
    for line in lines:
        t = line.strip().lstrip('-').strip()
        if not t or t.startswith('#'):
            continue
        if t not in out:
            out.append(t)
    return out

RESERVED = {'needs-gherkin', 'discovered', 'ready-for-agent', 'ready-for-human',
            'needs-triage', 'needs-info', 'triaged', 'bug', 'enhancement',
            'wontfix', 'Star'}

projects = api(f'/api/admin/projects?fields=id,shortName&query={urllib.parse.quote(PROJECT)}')
pid = next((p['id'] for p in projects if p.get('shortName') == PROJECT), None)
lines = [f'# Project dimensions: {PROJECT} ({datetime.date.today()})', '',
         'GENERATED by yt-pull - do not edit. Pick from these values; adding a',
         'new one is a deliberate act (see the triage skill), never a typo.', '']
tags_pending, tags_added, tags_note = [], [], None
if pid:
    fields = api(f'/api/admin/projects/{pid}/customFields'
                 '?fields=field(name),bundle(id,values(name,archived,released))')
    # ---- topical tags: the agents' own vocabulary -------------------------------
    # The list belongs to the agents. They append to it as work suggests a
    # grouping; nothing here says what a tag should look like. This step only
    # carries the file into the tracker so the field is filterable, and never the
    # other way: removing a value would orphan the issues carrying it. Reading is
    # free, writing is explicit - a plain run reports what is pending, --push-tags
    # applies it.
    want = wanted_tags()
    if want:
        fld = next((f for f in fields
                    if ((f.get('field') or {}).get('name') == TAGS_FIELD)), None)
        bundle = (fld or {}).get('bundle') or {}
        bid = bundle.get('id')
        have = {v.get('name') for v in (bundle.get('values') or [])}
        tags_missing = [t for t in want if t not in have]
        if fld is None:
            tags_note = (f'{TAGS_FILE} lists {len(want)} tag(s) but this project has '
                         f'no "{TAGS_FIELD}" field - add the field, or set '
                         'TOPICAL_TAGS_FIELD to the one you use.')
        elif tags_missing and not bid:
            tags_note = (f'"{TAGS_FIELD}" has no editable value set; '
                         f'{len(tags_missing)} tag(s) cannot be added.')
        elif tags_missing and PUSH_TAGS:
            for t in tags_missing:
                try:
                    api_post(f'/api/admin/customFieldSettings/bundles/enum/'
                             f'{bid}/values?fields=name', {'name': t})
                    tags_added.append(t)
                except Exception as e:                              # noqa: BLE001
                    tags_note = f'could not add "{t}": {e}'
                    break
            if tags_added:   # re-read so dimensions.md shows what was just added
                fields = api(f'/api/admin/projects/{pid}/customFields'
                             '?fields=field(name),bundle(id,values(name,archived,released))')
        elif tags_missing:
            tags_pending = tags_missing


    for f in fields:
        name = (f.get('field') or {}).get('name')
        bundle = f.get('bundle')
        vals = [v for v in ((bundle or {}).get('values') or [])
                if not v.get('archived')]
        # A field WITH a bundle and no values still gets a heading. Empty is
        # information - the dimension exists and nothing has been defined yet
        # - and silence reads as "no such dimension", which is what sends an
        # agent off to invent a value. Fields with no bundle at all (text,
        # date, user) have no enumerable set and stay out.
        if not name or bundle is None:
            continue
        if not vals:
            lines.append(f'## {name}')
            lines.append('- _(no values defined yet - adding one is a '
                         'project-settings change, not something to invent)_')
            lines.append('')
            continue
        # version bundles carry `released`: current/upcoming is what new
        # work targets, shipped ones are history and should not be picked
        # by accident.
        current = [v['name'] for v in vals if not v.get('released')]
        shipped = [v['name'] for v in vals if v.get('released')]
        if shipped:
            lines.append(f'## {name} (current and upcoming)')
            lines += ([f'- {v}' for v in current] or
                      ['- _(none open - a new one is a project-settings change)_'])
            lines += ['', f'Already released - history, do not target new work: '
                          + ', '.join(shipped[-12:]) + ('' if len(shipped) <= 12
                          else f' (+{len(shipped) - 12} older)'), '']
        else:
            lines.append(f'## {name}')
            lines += [f'- {v["name"]}' for v in vals] + ['']
# Tags: list EVERY usable tag, workflow ones included. Filtering the
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
tags = api('/api/tags?fields=name&$top=500')
present = {t['name'].lower() for t in tags if t.get('name')}
reserved_lower = {r.lower() for r in RESERVED}

lines.append('## Workflow tags (machinery - apply per the triage state machine)')
lines.append('')
missing = []
for name, meaning in WORKFLOW:
    if name.lower() in present:
        lines.append(f'- `{name}` - {meaning}')
    else:
        missing.append(name)
if missing:
    lines += ['', 'Not on this server yet (run the installer to create them): '
                  + ', '.join(f'`{m}`' for m in missing)]
lines += ['', 'These are never topical and never inherited by discovered work.', '']

topical = sorted({t['name'] for t in tags
                  if t.get('name') and t['name'].lower() not in reserved_lower})
# Named for what it is - server-wide tags, not project-scoped - so it
# cannot be confused with a project custom field that may also be
# called Topical Tags. Two headings a line apart with near-identical
# names and opposite instructions is worse than either alone.
lines.append('## Server tags (global to this YouTrack)')
lines.append('')
lines.append('Cross-cutting states, shared by every project on this server.')
lines.append('Reuse one if it fits. Topical groupings do NOT belong here -')
lines.append(f'they go in the {TAGS_FIELD} field above.')
lines.append('')
if topical:
    lines += [f'- {t}' for t in topical] + ['']
else:
    lines += ['_(none yet)_', '']
write_if_changed(os.path.join(out, 'dimensions.md'), '\n'.join(lines))
print(f'Wrote dimensions.md ({len(WORKFLOW) - len(missing)} workflow + '
      f'{len(topical)} topical tags)')
if tags_added:
    print(f'  {TAGS_FIELD}: added {len(tags_added)} value(s) from {TAGS_FILE}: '
          + ', '.join(tags_added))
if tags_pending:
    print(f'  {TAGS_FIELD}: {len(tags_pending)} value(s) in {TAGS_FILE} are not '
          'in the tracker yet: ' + ', '.join(tags_pending))
    print('  Add them with: yt-pull.sh --dimensions-only --push-tags')
if tags_note:
    print(f'  {TAGS_FIELD}: {tags_note}')
EOF
