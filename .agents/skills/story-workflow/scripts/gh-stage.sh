#!/usr/bin/env bash
# Move a GitHub issue's Project (v2) item to a Status column - the Stage
# transition for GitHub-tracked projects. Adds the issue to the project
# first if it isn't on the board yet.
#
# Usage: gh-stage.sh ISSUE_NUMBER "Column Name" [options]
#   --repo owner/name   repo (default: tracker.repo from the pointer)
#   --gh-project N      Project v2 number (default: tracker.project)
#   --owner LOGIN       project owner (default: the repo owner)
#   --dry-run           print the plan; change nothing
#   --help              this text
#
# Token: $GITHUB_TOKEN, else `gh auth token`, else GITHUB_TOKEN from
# ~/.agents/story-tools/connections/github.env. Column names match
# case-insensitively; a miss lists the real columns.
# Exit codes: 0 ok, 1 error, 3 = no project configured (issues-only mode).
set -euo pipefail

ISSUE=""; COLUMN=""; REPO=""; PROJ=""; OWNER=""; DRY=0
while [[ $# -gt 0 ]]; do
  case "$1" in
    --repo) REPO="$2"; shift 2;;
    --gh-project) PROJ="$2"; shift 2;;
    --owner) OWNER="$2"; shift 2;;
    --dry-run) DRY=1; shift;;
    --help) awk 'NR>1 && !/^#/{exit} NR>1{sub(/^# ?/,""); print}' "$0"; exit 0;;
    *) if [[ -z "$ISSUE" ]]; then ISSUE="$1"; else COLUMN="$1"; fi; shift;;
  esac
done
[[ -n "$ISSUE" && -n "$COLUMN" ]] || { echo "usage: gh-stage.sh ISSUE_NUMBER \"Column Name\" [--repo owner/name] [--gh-project N]" >&2; exit 1; }

read_pointer() {
  local f="./.agents/config/story-tools.json"
  [[ -f "$f" ]] || return 0
  sed -nE 's/.*"'"$1"'": *"?([^",}]+)"?.*/\1/p' "$f" | head -1
}
[[ -z "$REPO" ]] && REPO="$(read_pointer repo)"
[[ -z "$PROJ" ]] && PROJ="$(read_pointer project)"
[[ -z "$REPO" ]] && { echo "error: no repo (--repo or tracker.repo in .agents/config/story-tools.json)" >&2; exit 1; }
if [[ -z "$PROJ" ]]; then
  echo "no Project configured (tracker.project) - issues-only mode has no Stage; nothing to do" >&2
  exit 3
fi
[[ -z "$OWNER" ]] && OWNER="${REPO%%/*}"

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

export GITHUB_TOKEN REPO PROJ OWNER ISSUE COLUMN DRY
python3 <<'EOF'
import json, os, sys, urllib.request

TOKEN = os.environ['GITHUB_TOKEN']; REPO = os.environ['REPO']
PROJ = int(os.environ['PROJ']); OWNER = os.environ['OWNER']
ISSUE = int(os.environ['ISSUE']); COLUMN = os.environ['COLUMN']
DRY = os.environ['DRY'] == '1'

def gql(query, variables=None):
    req = urllib.request.Request('https://api.github.com/graphql',
        data=json.dumps({'query': query, 'variables': variables or {}}).encode(),
        headers={'Authorization': 'Bearer ' + TOKEN, 'Content-Type': 'application/json'})
    with urllib.request.urlopen(req) as r:
        out = json.loads(r.read())
    if out.get('errors'):
        sys.exit('GraphQL error: ' + '; '.join(e.get('message','?') for e in out['errors']))
    return out['data']

# project node + single-select fields (works for user OR org owners)
meta_q = """
query($owner: String!, $num: Int!) {
  repositoryOwner(login: $owner) {
    ... on ProjectV2Owner {
      projectV2(number: $num) {
        id
        fields(first: 30) {
          nodes { ... on ProjectV2SingleSelectField { id name options { id name } } }
        }
      }
    }
  }
}"""
proj = (gql(meta_q, {'owner': OWNER, 'num': PROJ}).get('repositoryOwner') or {}).get('projectV2')
if not proj:
    sys.exit(f'error: project {PROJ} not found for owner {OWNER} (check number, owner, and PAT project scope)')

status = next((f for f in proj['fields']['nodes'] if f and f.get('name') == 'Status'), None)
if not status:
    sys.exit("error: the project has no 'Status' single-select field")
option = next((o for o in status['options'] if o['name'].lower() == COLUMN.lower()), None)
if not option:
    sys.exit(f'error: no column "{COLUMN}". Available: ' + ', '.join(o['name'] for o in status['options']))

owner_login, repo_name = REPO.split('/', 1)
issue_q = """
query($owner: String!, $repo: String!, $num: Int!) {
  repository(owner: $owner, name: $repo) {
    issue(number: $num) {
      id title
      projectItems(first: 20) { nodes { id project { id } } }
    }
  }
}"""
issue = gql(issue_q, {'owner': owner_login, 'repo': repo_name, 'num': ISSUE})['repository']['issue']
if not issue:
    sys.exit(f'error: issue #{ISSUE} not found in {REPO}')

item = next((n for n in issue['projectItems']['nodes'] if n['project']['id'] == proj['id']), None)
if DRY:
    verb = 'move' if item else 'add to board, then move'
    print(f'(dry run) would {verb} #{ISSUE} "{issue["title"]}" -> {option["name"]}')
    sys.exit(0)

if not item:
    added = gql("""
mutation($project: ID!, $content: ID!) {
  addProjectV2ItemById(input: {projectId: $project, contentId: $content}) { item { id } }
}""", {'project': proj['id'], 'content': issue['id']})
    item = added['addProjectV2ItemById']['item']
    print(f'added #{ISSUE} to the board')

gql("""
mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
  updateProjectV2ItemFieldValue(input: {
    projectId: $project, itemId: $item, fieldId: $field,
    value: { singleSelectOptionId: $option }
  }) { projectV2Item { id } }
}""", {'project': proj['id'], 'item': item['id'], 'field': status['id'], 'option': option['id']})
print(f'#{ISSUE} "{issue["title"]}" -> {option["name"]}')
EOF
