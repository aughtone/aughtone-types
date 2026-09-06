#!/usr/bin/env bash
# story-tools installer.
#
#   ./install.sh              guided setup / settings review (start here)
#   ./install.sh --user [--connection <name>]   NOT recommended - see below
#   ./install.sh --clean-user   remove user-level copies of suite skills
#   ./install.sh --project <dir> [--connection <name>] [--yt-project <KEY>] [--readonly] [--copy]
#                                [--snapshot synced|committed]   override the derived choice
#   ./install.sh --project <dir> --github owner/repo [--gh-project N]   bind to GitHub
#   ./install.sh --github     per-developer GitHub setup: PAT -> connection,
#                             register the 'github' MCP server in agent configs
#   ./install.sh --register [--connection <name>]   re-push the connection's
#                             token into every agent's MCP config - YouTrack
#                             or GitHub, whichever the connection is (run
#                             after rotating a token - registrations embed it)
#   ./install.sh --list | --show | --help
#
# Everything lives under one well-known root:
#   ~/.agents/story-tools/connections/<name>.env   one server + your token for it
#   ~/.agents/skills/                              user-level skills (avoid)
#   <project>/.agents/                             per-project: skills + pointer (commit it)
# Re-running shows every stored value and lets you change any of them.
#
# Teammates onboarding from a clone don't need this repo: every bound
# project ships a copy as <project>/.agents/setup.sh - run that. It
# detects it is the shipped copy and only sets up YOUR credential and
# agent registrations (skills already travel with the repo).
set -euo pipefail

REPO_DIR="$(cd "$(dirname "$0")/.." && pwd)"

# Prerequisites, checked before anything is written. A tool that is missing
# and only discovered halfway through a bind leaves a project half
# installed - skills copied, pointer absent - which looks like success and
# is not. Fail here instead.
command -v python3 >/dev/null 2>&1 \
  || { echo "error: python3 is required" >&2; exit 1; }

# `set -e` exits the whole script the moment any command returns non-zero,
# including a `[[ ... ]] && cmd` whose test simply failed. Without this the
# installer can stop mid-bind and print nothing at all, leaving a project
# that looks installed. Make every abort say so.
on_unexpected_exit() {
  local rc=$? line="${1:-?}"
  [[ $rc -eq 0 ]] && return 0
  echo "" >&2
  echo "error: the installer stopped unexpectedly (exit $rc, line $line)." >&2
  echo "  Nothing after that point ran, so this project may be half set up." >&2
  if [[ -n "${PROJECT_DIR:-}" ]]; then
    [[ -f "$PROJECT_DIR/.agents/config/story-tools.json" ]] \
      || echo "  MISSING: .agents/config/story-tools.json (the pointer)" >&2
    [[ -f "$PROJECT_DIR/.agents/setup.sh" ]] \
      || echo "  MISSING: .agents/setup.sh" >&2
    echo "  Re-run once the cause above is fixed." >&2
  fi
  return 0
}
trap 'on_unexpected_exit $LINENO' ERR
# Shipped copy? (<project>/.agents/setup.sh, written at bind time.) No
# skills tree beside it -> developer onboarding only, never skill copying.
SHIPPED=0
[[ -d "$REPO_DIR/skills/stories/story-workflow" ]] || SHIPPED=1

# Running from the source clone? Wire up the repo's own git hooks. Nobody
# should have to remember `git config core.hooksPath` - and git will not
# do it for you, since a repo cannot enable its own hooks.
if [[ $SHIPPED -eq 0 && -d "$REPO_DIR/.githooks" && -d "$REPO_DIR/.git" ]]; then
  if [[ "$(git -C "$REPO_DIR" config --get core.hooksPath 2>/dev/null)" != ".githooks" ]]; then
    git -C "$REPO_DIR" config core.hooksPath .githooks 2>/dev/null \
      && echo "  (enabled this repo's git hooks)"
  fi
fi
SKILLS=("$REPO_DIR/skills/stories/story-workflow" "$REPO_DIR/skills/stories/story-reconcile"
        "$REPO_DIR/skills/stories/to-issues" "$REPO_DIR/skills/stories/triage"
        "$REPO_DIR/skills/docs/project-docs" "$REPO_DIR/skills/docs/to-prd"
        "$REPO_DIR/skills/docs/to-adr" "$REPO_DIR/skills/docs/to-rad"
        "$REPO_DIR/skills/docs/grill-with-docs" "$REPO_DIR/skills/docs/regulatory-compliance" "$REPO_DIR/skills/docs/to-wiring"
        "$REPO_DIR/skills/sessions/handoff" "$REPO_DIR/skills/sessions/housekeeping"
        "$REPO_DIR/skills/sessions/zoom-out" "$REPO_DIR/skills/engineering/tdd"
        "$REPO_DIR/skills/engineering/improve-codebase-architecture"
        "$REPO_DIR/skills/engineering/to-ux")
# worklog is NOT here on purpose. A developer's working day is personal -
# it spans every project and belongs to the person, not to any repo - so
# installing it into every bound project put a private record in front of
# people who never asked for it. It stays in this repo and attaches à la
# carte: copy skills/sessions/worklog into a project that wants it. Not in
# RETIRED_SKILLS - it is not retired, just not part of the suite.
# to-library-skill has left this suite. It moved to the dependency-skills
# project, where it ships alongside the plugin it teaches so the two cannot
# drift, and it is now in RETIRED_SKILLS so existing copies are pruned on
# refresh. Retired ahead of the replacement being installable on purpose:
# the version here teaches a packaging convention that has since been
# abandoned, and a stale copy of that is worse than none.
# Skills THIS SUITE authored and has since retired. They are pruned from
# projects on refresh - otherwise a retired skill lingers in every repo
# until someone notices.
#
# Only ever list skills we own. A third-party or independent skill is
# never pruned, even if an older installer once shipped it: the developer
# may want it, and it is not ours to remove. Same for anything the
# project added itself.
RETIRED_SKILLS=(grill-me to-ai-skill to-research to-design to-library-skill
                setup-project manage-docs manage-persona manage-skills
                sync-tracking)

# Where the suite lives, for version checks and teammate updates.
SKILLS_REPO="${STORY_TOOLS_REPO:-bpappin/skills}"
SKILLS_BRANCH="${STORY_TOOLS_BRANCH:-master}"

AGENTS_HOME="$HOME/.agents"
CONF_DIR="$AGENTS_HOME/story-tools"
CONN_DIR="$CONF_DIR/connections"

# ---------- output ----------
# Colour only when a human is looking: a tty, NO_COLOR unset, TERM not dumb.
# Escapes in a log file or a pipe are noise nobody asked for.
if [[ -t 1 && -z "${NO_COLOR:-}" && "${TERM:-}" != dumb ]]; then
  C_B=$'\033[1m'; C_D=$'\033[2m'; C_G=$'\033[32m'
  C_Y=$'\033[33m'; C_R=$'\033[31m'; C_C=$'\033[36m'; C_0=$'\033[0m'
else
  C_B=''; C_D=''; C_G=''; C_Y=''; C_R=''; C_C=''; C_0=''
fi

say()   { printf '%s\n' "$*"; }
blank() { printf '\n'; }

# A section. Underlined so the eye finds it when scrolling back.
step() {
  # count characters, not bytes - the rule glyph is multibyte
  local n=${#1} i=0 rule=''
  while (( i < n )); do rule="${rule}─"; i=$((i+1)); done
  printf '\n%s%s%s\n%s%s%s\n' "$C_B" "$*" "$C_0" "$C_D" "$rule" "$C_0"
}

# Results, at one indent.
ok()   { printf '  %s✓%s %s\n' "$C_G" "$C_0" "$*"; }
warn() { printf '  %s!%s %s\n' "$C_Y" "$C_0" "$*"; }
err()  { printf '  %s✗%s %s\n' "$C_R" "$C_0" "$*" >&2; }

# Detail belonging to the line above, at two - so it reads as subordinate
# rather than competing with the next result.
note()  { printf '    %s%s%s\n' "$C_D" "$*" "$C_0"; }
# Something to run or a path to open: worth making copy-able at a glance.
cmd()   { printf '    %s%s%s\n' "$C_C" "$*" "$C_0"; }
# Something that needs a decision or a fix. Yellow and bold, because it is
# an instruction to act rather than a result to read past.
heads_up() { printf '\n  %s%s%s %s%s%s\n\n' "$C_Y" "!" "$C_0" "$C_B$C_Y" "$*" "$C_0"; }
# A pickable option: the letter stands out, the label names it, the
# explanation sits behind in dim so the eye lands on the choices first.
choice()      { printf '    %s%s%s  %s%-12s%s %s%s%s\n' "$C_C" "$1" "$C_0" "$C_B" "$2" "$C_0" "$C_D" "$3" "$C_0"; }
choice_cont() { printf '       %12s %s%s%s\n' "" "$C_D" "$1" "$C_0"; }
# Aligned settings, so values line up down the page.
kv()    { printf '  %s%-24s%s %s\n' "$C_D" "$1" "$C_0" "$2"; }

# A retired skill must not survive in the source tree. RETIRED_SKILLS is
# what prunes it from bound projects, but a directory left behind under
# skills/ or budget-skills/ is still readable, still copyable, and is what
# anyone assembling a set from what is on disk will copy. That is not
# hypothetical: to-research was retired here, left in place, and then
# copied wholesale into budget-skills by someone reading the tree rather
# than this list. Only checked from the source clone - a shipped bundle
# has no skills tree to police.
if [[ $SHIPPED -eq 0 ]]; then
  _stale=''
  for _r in "${RETIRED_SKILLS[@]}"; do
    for _root in "$REPO_DIR/skills" "$REPO_DIR/budget-skills"; do
      [[ -d "$_root" ]] || continue
      while IFS= read -r _d; do
        [[ -n "$_d" ]] || continue
        _stale="$_stale${_stale:+ }${_d#"$REPO_DIR"/}"
      done < <(find "$_root" -maxdepth 2 -type d -name "$_r" 2>/dev/null)
    done
  done
  if [[ -n "$_stale" ]]; then
    warn "retired skill(s) still in the source tree: $_stale"
    say  "    They are pruned from projects but remain here to be copied by mistake."
    say  "    Delete them, or take them off RETIRED_SKILLS if they are not retired."
  fi
  unset _stale _r _root _d
fi

# one-time migration from older layouts
for old in "$HOME/.config/story-tools" "$CONF_DIR/profiles"; do
  if [[ -d "$old" ]] && ls "$old"/*.env >/dev/null 2>&1; then
    mkdir -p "$CONN_DIR"
    mv -n "$old"/*.env "$CONN_DIR"/ 2>/dev/null || true
  fi
done

name_from_url() { echo "$1" | sed -E 's#https?://##; s#[/:.].*##'; }
# only YouTrack connection files (GitHub credentials share the dir but carry GITHUB_TOKEN)
list_connections() { grep -l '^YOUTRACK_URL=' "$CONN_DIR"/*.env 2>/dev/null | sed -E 's#.*/(.*)\.env#\1#' || true; }

load_connection() {  # $1 = name; sets YOUTRACK_URL/TOKEN/PROJECT
  local f="$CONN_DIR/$1.env"
  [[ -f "$f" ]] || return 1
  # shellcheck disable=SC1090
  source "$f"
  [[ -n "${YOUTRACK_URL:-}" && -n "${YOUTRACK_TOKEN:-}" ]]
}

save_connection() {  # $1 name
  umask 077; mkdir -p "$CONN_DIR"
  { echo "YOUTRACK_URL=${YOUTRACK_URL%/}"
    echo "YOUTRACK_TOKEN=$YOUTRACK_TOKEN"
    echo "MCP_SERVER=${MCP_SERVER:-youtrack}"
    [[ -n "${YOUTRACK_PROJECT:-}" ]] && echo "YOUTRACK_PROJECT=$YOUTRACK_PROJECT"
  } > "$CONN_DIR/$1.env"
  chmod 600 "$CONN_DIR/$1.env"
}

resolve_server() {  # always youtrack-<nickname>: explicit per server, no cross-pollination
  # connection names carry the tracker prefix now; do not double it
  MCP_SERVER="youtrack-${PROFILE#youtrack-}"
}

ask() {  # ask "prompt" "current" -> echoes answer (Enter keeps current)
  local v
  read -rp "  $1${2:+ [$2]}: " v
  echo "${v:-$2}"
}

verify_token() {  # $1 url, $2 token -> echoes login or fails
  curl -sfS -m 10 -H "Authorization: Bearer $2" "$1/api/users/me?fields=login" 2>/dev/null \
    | sed -E 's/.*"login":"([^"]*)".*/\1/'
}

yt() {  # yt METHOD PATH [JSON] -> body on stdout; fails on HTTP error
  local method="$1" path="$2" data="${3:-}"
  curl -sfS -m 15 -X "$method" "${YOUTRACK_URL%/}$path" \
    -H "Authorization: Bearer $YOUTRACK_TOKEN" -H "Content-Type: application/json" \
    ${data:+-d "$data"}
}

# ---------- GitHub tracker support ----------

gh_rest() {  # gh_rest METHOD PATH [JSON]
  local method="$1" path="$2" data="${3:-}"
  curl -sfS -m 15 -X "$method" "https://api.github.com$path" \
    -H "Authorization: Bearer $GITHUB_TOKEN" -H "Accept: application/vnd.github+json" \
    ${data:+-d "$data"}
}

load_github() {  # $1 = connection name (optional); sets GITHUB_TOKEN + GH_LOGIN
  GH_LOGIN=""; local name="${1:-}"
  if [[ -z "${GITHUB_TOKEN:-}" && -n "$name" && -f "$CONN_DIR/$name.env" ]]; then
    # shellcheck disable=SC1090
    source "$CONN_DIR/$name.env"
  fi
  if [[ -z "${GITHUB_TOKEN:-}" && -f "$CONN_DIR/github.env" ]]; then   # legacy shared credential
    # shellcheck disable=SC1091
    source "$CONN_DIR/github.env"
  fi
  if [[ -z "${GITHUB_TOKEN:-}" ]] && command -v gh >/dev/null 2>&1; then
    GITHUB_TOKEN="$(gh auth token 2>/dev/null || true)"
  fi
  [[ -n "${GITHUB_TOKEN:-}" ]] || return 1
  GH_LOGIN="$(gh_rest GET /user 2>/dev/null | sed -nE 's/.*"login": *"([^"]*)".*/\1/p' | head -1)"
  [[ -n "$GH_LOGIN" ]]
}

# The MCP server name is derived from the connection name, and the connection
# arrives in two shapes: a project bind builds it as `github-<dir>`, while a
# refresh derives it bare from the directory. Strip before prefixing so both
# land on the same server. Two of the four call sites used to defend against
# this and two did not, which is how a connection already called
# `github-<name>` registered as `github-github-<name>`. The connection name
# still names the credential file; only the server name is derived here.
gh_server_name() {  # $1 = connection name -> MCP server name
  local conn="${1:-github}"
  if [[ "$conn" == "github" ]]; then printf 'github'; else printf 'github-%s' "${conn#github-}"; fi
}

setup_github() {  # $1 = connection name (default github): token -> <name>.env, verify, register
  GH_CONN="${1:-github}"
  step "GitHub connection '$GH_CONN' (your own PAT - one per developer, never shared)"
  say "  Preferred: a FINE-GRAINED token (org-policy friendly)."
  say "    GitHub > Settings > Developer settings > Personal access tokens >"
  say "    Fine-grained tokens > Generate new token."
  say "    Resource owner: the ORG that owns the repo (not your user);"
  say "    Repository access: the project repo(s);"
  say "    Repository permissions: Issues RW, Contents RW, Pull requests R,"
  say "    Metadata R; Organization permissions: Projects RW."
  say "    (Org-owned Projects v2 work with fine-grained tokens; USER-owned"
  say "    project boards still need a classic token with repo + project.)"
  say "  Classic fallback: Tokens (classic), scopes 'repo' + 'project';"
  say "  org SSO: Configure SSO > Authorize the token after creating it."
  local t
  read -rsp "  Token [Enter = use 'gh auth' / keep current] (input hidden): " t; echo
  if [[ -n "$t" ]]; then
    GITHUB_TOKEN="$t"
    umask 077; mkdir -p "$CONN_DIR"
    echo "GITHUB_TOKEN=$GITHUB_TOKEN" > "$CONN_DIR/$GH_CONN.env"; chmod 600 "$CONN_DIR/$GH_CONN.env"
  fi
  if load_github "$GH_CONN"; then
    ok "GitHub token verified - authenticated as '$GH_LOGIN'"
  else
    say "  error: no working GitHub credential (paste a PAT, or 'gh auth login' first)" >&2
    exit 1
  fi
  register_agents_github "$GH_CONN"
  say ""
  say "Done. Restart your agent sessions (Claude Code, Gemini CLI, VS Code)"
  say "so they pick up the '$(gh_server_name "$GH_CONN")' MCP server."
}

register_agents_github() {  # $1 = connection name; GitHub hosted MCP server, PAT header, per agent
  local conn="${1:-github}"
  if [[ ! -f "$CONN_DIR/$conn.env" && ! -f "$CONN_DIR/github.env" ]]; then
    say "  (no stored PAT - skipping MCP registration; scripts will use gh auth)"; return 0
  fi
  local server mcp_url="https://api.githubcopilot.com/mcp/"
  server="$(gh_server_name "$conn")"   # 'github' stays 'github' - legacy shared credential
  local auth="Bearer $GITHUB_TOKEN"
  if command -v claude >/dev/null; then
    claude mcp remove --scope user "$server" >/dev/null 2>&1 || true
    claude mcp add --scope user --transport http "$server" "$mcp_url" \
      --header "Authorization: $auth" >/dev/null \
      && ok "Claude Code: '$server' (user scope)" \
      || warn "Claude Code registration failed - run 'claude mcp add' manually"
  fi
  if [[ -d "$HOME/.gemini" ]] || command -v gemini >/dev/null; then
    merge_json "$HOME/.gemini/settings.json" "mcpServers.$server" \
      '{"httpUrl":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "Gemini CLI: ~/.gemini/settings.json"
  fi
  if [[ -d "$HOME/.gemini/antigravity" || -d "$HOME/.antigravity" ]]; then
    # Antigravity is NOT Gemini CLI: own file, and 'serverUrl' not 'httpUrl'
    merge_json "$HOME/.gemini/antigravity/mcp_config.json" "mcpServers.$server" \
      '{"serverUrl":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "Antigravity: ~/.gemini/antigravity/mcp_config.json"
  fi
  local vsc=""
  case "$(uname -s)" in
    Darwin) vsc="$HOME/Library/Application Support/Code/User";;
    Linux)  vsc="$HOME/.config/Code/User";;
  esac
  if [[ -n "$vsc" && -d "$vsc" ]]; then
    merge_json "$vsc/mcp.json" "servers.$server" \
      '{"type":"http","url":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "VS Code / Copilot: user mcp.json"
  fi
}

ensure_labels() {  # $1 = owner/repo: reserved workflow labels, case-insensitive
  local repo="$1" existing created=0
  if ! existing="$(gh_rest GET "/repos/$repo/labels?per_page=100" 2>/dev/null)"; then
    warn "could not read labels for $repo (check PAT repo scope) - skipped label setup"
    return 0
  fi
  local lower; lower="$(printf '%s' "$existing" | tr '[:upper:]' '[:lower:]')"
  local name color desc
  while IFS='|' read -r name color desc; do
    if grep -qE "\"name\": *\"$(printf '%s' "$name" | tr '[:upper:]' '[:lower:]')\"" <<<"$lower"; then
      continue
    fi
    if gh_rest POST "/repos/$repo/labels" \
        "{\"name\":\"$name\",\"color\":\"$color\",\"description\":\"$desc\"}" >/dev/null 2>&1; then
      created=$((created+1))
    else
      warn "could not create label '$name' - create it in GitHub before the workflow needs it"
    fi
  done <<'LABELS'
needs-triage|d93f0b|Awaiting triage (story-tools)
needs-info|fbca04|Waiting on reporter (story-tools)
ready-for-agent|0e8a16|Triaged; an agent can pick this up (story-tools)
ready-for-human|1d76db|Triaged; needs a human (story-tools)
wontfix|ffffff|Closed as not planned (story-tools)
triaged|c2e0c6|Has been triaged - never comes off (story-tools)
discovered|5319e7|Born from work on another issue (story-tools)
needs-gherkin|f9d0c4|Completion requires a QA Gherkin section (story-tools)
LABELS
  if [[ "$created" -gt 0 ]]; then ok "workflow labels created ($created) in $repo"
  else ok "workflow labels all present in $repo"; fi
}

check_app() {  # sets APP_CHECK = installed | missing | unauthorized | unreachable
  local code body
  body="$(mktemp)"
  code=$(curl -sS --max-time 20 --http1.1 -o "$body" -w "%{http_code}" \
    -X POST "${YOUTRACK_URL%/}/mcp?customToolPackages=story-tools" \
    -H "Authorization: Bearer $YOUTRACK_TOKEN" -H "Content-Type: application/json" \
    -H "Accept: application/json, text/event-stream" \
    -d '{"jsonrpc":"2.0","id":1,"method":"tools/list"}' 2>/dev/null) || code="000"
  APP_VERSION=""
  if grep -q '"story_' "$body"; then
    APP_CHECK="installed"
    APP_VERSION=$(grep -o 'story-tools v[0-9][0-9.]*' "$body" | head -1 | sed 's/story-tools v//' || true)
  elif [[ "$code" == "401" || "$code" == "403" ]]; then APP_CHECK="unauthorized"
  elif [[ "$code" == "200" ]]; then APP_CHECK="missing"
  else APP_CHECK="unreachable"
  fi
  rm -f "$body"
}

merge_json() {  # <file> <dot.path> <json-value>
  mkdir -p "$(dirname "$1")"
  MJ_FILE="$1" MJ_PATH="$2" MJ_VALUE="$3" python3 - <<'MJPY'
import json, os
f, path, value = os.environ["MJ_FILE"], os.environ["MJ_PATH"], os.environ["MJ_VALUE"]
root = {}
if os.path.exists(f):
    raw = open(f).read().strip()
    if raw:
        root = json.loads(raw)
node = root
keys = path.split(".")
for k in keys[:-1]:
    if not isinstance(node.get(k), dict):
        node[k] = {}
    node = node[k]
node[keys[-1]] = json.loads(value)
tmp = f + ".tmp"
with open(tmp, "w") as fh:
    json.dump(root, fh, indent=2)
    fh.write("\n")
os.chmod(tmp, 0o600)
os.replace(tmp, f)
MJPY
}

read_pointer() {  # $1 dir, $2 key -> value or empty
  local f="$1/.agents/config/story-tools.json"
  [[ -f "$f" ]] || f="$1/.agents/youtrack.json"   # pre-rename location
  [[ -f "$f" ]] || return 0
  sed -nE 's/.*"'"$2"'": *"?([^",}]+)"?.*/\1/p' "$f" | head -1
}

# ---------- step: credentials (create or review) ----------

setup_connection() {  # $1 = optional preselected name; sets PROFILE + creds vars
  PROFILE="${1:-}"
  local existing; existing="$(list_connections)"

  if [[ -n "$PROFILE" ]]; then
    say "  This project is bound to connection '$PROFILE'."
    PROFILE="$(ask "Use which connection? (name, or 'new' for another server)" "$PROFILE")"
  elif [[ -n "$existing" ]]; then
    say "  Existing connections: $(echo "$existing" | tr '\n' ' ')"
    say "  Type one of those names to REUSE it (same server, maybe a different"
    say "  project key). A fresh project deliberately defaults to 'new' -"
    say "  binding to an existing server is always an explicit choice."
    PROFILE="$(ask "Use which connection? ('new' = add a server)" "new")"
  fi
  [[ "$PROFILE" == "new" ]] && PROFILE=""

  local cur_url="${PRE_URL:-}" cur_project="" have_token=""
  if [[ -n "$PROFILE" ]] && load_connection "$PROFILE"; then
    cur_url="$YOUTRACK_URL"; cur_project="${YOUTRACK_PROJECT:-}"; have_token="yes"
  fi

  YOUTRACK_URL="$(ask "Full YouTrack URL (e.g. https://yt.example.com or https://acme.youtrack.cloud)" "$cur_url")"
  [[ -n "$YOUTRACK_URL" ]] || { say "error: URL is required" >&2; exit 1; }

  # a different URL is a different server: NEVER overwrite the reviewed connection
  if [[ -n "$PROFILE" && -n "$cur_url" && "${YOUTRACK_URL%/}" != "${cur_url%/}" ]]; then
    say "  That's a different server than connection '$PROFILE' ($cur_url) -"
    say "  creating a NEW connection for it ('$PROFILE' is left untouched)."
    PROFILE=""; have_token=""; unset YOUTRACK_TOKEN MCP_SERVER YOUTRACK_PROJECT
  fi

  say "  Token: YouTrack > Profile > Account Security > New token."
  say "  REQUIRED SCOPE: \"YouTrack\" only (not YouTrack Administration)."
  say "  Your account needs Contributor-level permissions in the projects you work in."
  say "  (Deploying the story-tools app is separate and needs an admin token.)"
  local t
  read -rsp "  Token${have_token:+ [Enter = keep current]} (input hidden): " t; echo
  [[ -n "$t" ]] && YOUTRACK_TOKEN="$t"
  [[ -n "${YOUTRACK_TOKEN:-}" ]] || { say "error: token is required" >&2; exit 1; }

  local login
  if login="$(verify_token "${YOUTRACK_URL%/}" "$YOUTRACK_TOKEN")" && [[ -n "$login" ]]; then
    ok "token verified - authenticated as '$login'"
  else
    say "  error: could not authenticate against ${YOUTRACK_URL} - check URL and token." >&2
    exit 1
  fi

  # One connection per project, not per server. YouTrack tokens are not
  # project-scoped today (JT-97918 is the open RFE to split them), so this
  # buys clarity and independent revocation now - and when scoping lands,
  # each project already has its own file to drop a narrower token into,
  # with nothing to migrate. Falls back to the host when no project is in
  # play (a user-level setup).
  local newname suggested
  if [[ -n "${PROFILE:-}" ]]; then
    suggested="$PROFILE"
  elif [[ -n "${PROJECT_NAME:-}" ]]; then
    suggested="youtrack-$(printf '%s' "$PROJECT_NAME" | tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9-\n' '-')"
  else
    suggested="youtrack-$(name_from_url "$YOUTRACK_URL")"
  fi
  while :; do
    newname="$(ask "Connection nickname (this server + your token; one per project keeps them separately revocable)" "$suggested")"
    # never silently replace a DIFFERENT server's connection that happens to share the name
    if [[ "$newname" != "$PROFILE" && -f "$CONN_DIR/$newname.env" ]]; then
      local other_url; other_url="$(sed -n 's/^YOUTRACK_URL=//p' "$CONN_DIR/$newname.env")"
      if [[ "${other_url%/}" != "${YOUTRACK_URL%/}" ]]; then
        local yn; yn="$(ask "Connection '$newname' already exists for $other_url. Replace it with this server? (y/N)" "n")"
        [[ "$yn" =~ ^[Yy]$ ]] || { suggested=""; continue; }
      fi
    fi
    break
  done
  if [[ -n "$PROFILE" && "$newname" != "$PROFILE" && -f "$CONN_DIR/$PROFILE.env" ]]; then
    rm -f "$CONN_DIR/$PROFILE.env"
    warn "connection renamed '$PROFILE' -> '$newname' (old agent entries are inert)"
  fi
  PROFILE="$newname"
  resolve_server

  save_connection "$PROFILE"
  ok "connection saved: $CONN_DIR/$PROFILE.env (chmod 600)"
}

# ---------- step: server setup (app + link type + tag) ----------

offer_deploy() {  # $1 = reason line already printed by caller
  [[ -f "$REPO_DIR/trackers/youtrack/app/manifest.json" ]] || return 0
  local yn; yn="$(ask "Deploy the app now with this token? (needs admin permission) y/N" "n")"
  if [[ "$yn" =~ ^[Yy]$ ]]; then
    if YOUTRACK_HOST="${YOUTRACK_URL%/}" YOUTRACK_API_TOKEN="$YOUTRACK_TOKEN" "$REPO_DIR/trackers/youtrack/deploy.sh"; then
      ok "app deployed"
    else
      warn "deploy failed - see the output above for the actual cause"
      say "  If it mentions 403/permissions: the token lacks global Update Project /"
      say "  Low-level Admin Write - ask a YouTrack admin to run trackers/youtrack/deploy.sh."
    fi
  else
    say "  An admin can deploy later: cd $REPO_DIR && ./trackers/youtrack/deploy.sh"
  fi
}

setup_server() {
  local repo_ver=""
  [[ -f "$REPO_DIR/trackers/youtrack/app/manifest.json" ]] && \
    repo_ver=$(sed -nE 's/.*"version": *"([^"]+)".*/\1/p' "$REPO_DIR/trackers/youtrack/app/manifest.json" | head -1)
  check_app
  case "$APP_CHECK" in
    installed)
      if [[ -n "$APP_VERSION" && "$APP_VERSION" == "$repo_ver" ]]; then
        ok "story-tools app v$APP_VERSION installed - up to date with this repo"
      elif [[ -n "$APP_VERSION" ]]; then
        warn "story-tools app v$APP_VERSION installed; this repo has v$repo_ver"
        offer_deploy
      else
        warn "story-tools app installed, version unknown (predates v0.3.1); repo has v$repo_ver"
        offer_deploy
      fi;;
    unauthorized)
      warn "the token authenticates but cannot reach the MCP endpoint (HTTP 401/403)"
      say "  Check the token scope is \"YouTrack\" - then re-run this setup.";;
    unreachable)
      warn "could not verify the MCP endpoint (network/timeout) - skipping the app check"
      say "  Verify later with: cd $REPO_DIR && ./trackers/youtrack/smoke.sh";;
    missing)
      warn "MCP endpoint reachable, but the story-tools app is not installed"
      say "  The skills still work (fallback mode, built-in tools only)."
      offer_deploy;;
  esac

  # 'discovered from' link type - create if missing (status-aware)
  local lt
  if lt="$(yt GET "/api/issueLinkTypes?fields=sourceToTarget" 2>/dev/null)"; then
    if grep -qi "discovered from" <<<"$lt"; then
      ok "link type 'discovered from' exists"
    elif yt POST "/api/issueLinkTypes?fields=id" \
        '{"name":"Discovery","sourceToTarget":"discovered from","targetToSource":"discovered","directed":true}' >/dev/null 2>&1; then
      ok "link type 'discovered from' created"
    else
      warn "could not create link type 'discovered from' (needs admin) - until it exists, discovered work links as 'relates to' + tag"
    fi
  else
    warn "could not read link types (check token scope) - skipped link-type setup"
  fi

  # Workflow tags - create the full reserved set if missing, shared with
  # All Users where the token allows (a tag created ad hoc by one account
  # is otherwise invisible to teammates).
  local tags wf_tag created=0 shared=0
  if tags="$(yt GET "/api/tags?fields=name&\$top=500" 2>/dev/null)"; then
    local all_users_id
    all_users_id="$(yt GET "/api/groups?fields=id,name&\$top=50" 2>/dev/null \
      | python3 -c 'import json,sys; gs=json.load(sys.stdin); print(next((g["id"] for g in gs if g.get("name")=="All Users"),""))' 2>/dev/null || true)"
    for wf_tag in needs-triage needs-info ready-for-agent ready-for-human \
                  wontfix triaged discovered needs-gherkin; do
      if grep -qi "\"$wf_tag\"" <<<"$tags"; then
        continue  # exists (any case) - a case variant is the same tag to YouTrack
      fi
      local body="{\"name\":\"$wf_tag\"}"
      if [[ -n "$all_users_id" ]]; then
        body="{\"name\":\"$wf_tag\",\"readSharingSettings\":{\"permittedGroups\":[{\"id\":\"$all_users_id\"}]},\"updateSharingSettings\":{\"permittedGroups\":[{\"id\":\"$all_users_id\"}]}}"
      fi
      if yt POST "/api/tags?fields=id" "$body" >/dev/null 2>&1; then
        created=$((created+1)); [[ -n "$all_users_id" ]] && shared=$((shared+1))
      elif yt POST "/api/tags?fields=id" "{\"name\":\"$wf_tag\"}" >/dev/null 2>&1; then
        created=$((created+1))
      else
        warn "could not create tag '$wf_tag' - create it in YouTrack before the workflow first needs it"
      fi
    done
    if [[ "$created" -gt 0 ]]; then
      if [[ "$shared" -eq "$created" ]]; then
        ok "workflow tags created ($created), all shared with All Users"
      else
        warn "workflow tags created ($created) but only $shared shared - re-run, or share the rest in YouTrack"
      fi
    else
      ok "workflow tags all present"
    fi
    # ensure EXISTING workflow tags are shared too (read + update -> All
    # Users) - covers tags created before this installer or by hand.
    # Best-effort: only a tag's owner (or admin) may change its sharing.
    if [[ -n "$all_users_id" ]]; then
      ALL_USERS_ID="$all_users_id" YOUTRACK_URL="$YOUTRACK_URL" YOUTRACK_TOKEN="$YOUTRACK_TOKEN" python3 <<'PYEOF' || warn "could not verify sharing on existing workflow tags"
import json, os, urllib.request, urllib.error
URL = os.environ['YOUTRACK_URL'].rstrip('/'); TOK = os.environ['YOUTRACK_TOKEN']
GID = os.environ['ALL_USERS_ID']
def api(p, payload=None):
    req = urllib.request.Request(URL + p,
        data=json.dumps(payload).encode() if payload else None,
        headers={'Authorization': 'Bearer ' + TOK, 'Content-Type': 'application/json'})
    b = urllib.request.urlopen(req).read()
    return json.loads(b) if b else None
want = {'needs-triage','needs-info','ready-for-agent','ready-for-human',
        'wontfix','triaged','discovered','needs-gherkin'}
share = {'permittedGroups': [{'id': GID}]}
for t in api('/api/tags?fields=id,name&$top=500'):
    if t['name'].lower() not in want: continue
    try:
        api(f"/api/tags/{t['id']}?fields=id",
            {'readSharingSettings': share, 'updateSharingSettings': share})
    except urllib.error.HTTPError:
        print(f"  ! tag '{t['name']}' sharing not updatable by this token - set 'Updatable by: All Users' in the UI")
PYEOF
    fi
  else
    warn "could not read tags (check token scope) - skipped tag setup"
  fi
}

# ---------- step: agent registration ----------

register_agents() {
  resolve_server
  local server="$MCP_SERVER" mcp_url="${YOUTRACK_URL%/}/mcp?customToolPackages=story-tools"
  local auth="Bearer $YOUTRACK_TOKEN" found=0

  if command -v claude >/dev/null; then
    claude mcp remove --scope user "$server" >/dev/null 2>&1 || true
    claude mcp remove --scope user "youtrack" >/dev/null 2>&1 || true   # pre-nickname-era entry
    claude mcp add --scope user --transport http "$server" "$mcp_url" \
      --header "Authorization: $auth" >/dev/null \
      && ok "Claude Code: '$server' (user scope)" && found=1 \
      || warn "Claude Code registration failed - run 'claude mcp add' manually"
  fi

  if [[ -d "$HOME/.gemini" ]] || command -v gemini >/dev/null; then
    merge_json "$HOME/.gemini/settings.json" "mcpServers.$server" \
      '{"httpUrl":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "Gemini CLI: ~/.gemini/settings.json"; found=1
  fi
  if [[ -d "$HOME/.gemini/antigravity" || -d "$HOME/.antigravity" ]]; then
    merge_json "$HOME/.gemini/antigravity/mcp_config.json" "mcpServers.$server" \
      '{"serverUrl":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "Antigravity: ~/.gemini/antigravity/mcp_config.json"; found=1
  fi

  local vsc=""
  case "$(uname -s)" in
    Darwin) vsc="$HOME/Library/Application Support/Code/User";;
    Linux)  vsc="$HOME/.config/Code/User";;
  esac
  if [[ -n "$vsc" && -d "$vsc" ]]; then
    merge_json "$vsc/mcp.json" "servers.$server" \
      '{"type":"http","url":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "VS Code / Copilot: user mcp.json"; found=1
  fi

  if [[ -d "$HOME/.copilot" ]] || command -v copilot >/dev/null; then
    merge_json "$HOME/.copilot/mcp-config.json" "mcpServers.$server" \
      '{"type":"http","url":"'"$mcp_url"'","headers":{"Authorization":"'"$auth"'"}}'
    ok "Copilot CLI: ~/.copilot/mcp-config.json"; found=1
  fi

  if [[ -d "$HOME/.codex" ]] || command -v codex >/dev/null; then
    local toml="$HOME/.codex/config.toml"; mkdir -p "$HOME/.codex"
    if [[ -f "$toml" ]] && grep -q "^\[mcp_servers\.$server\]" "$toml"; then
      ok "Codex: already configured (edit $toml to rotate the token)"
    else
      printf '\n[mcp_servers.%s]\ncommand = "npx"\nargs = ["-y", "mcp-remote", "%s", "--header", "Authorization:${AUTH_HEADER}"]\nenv = { "AUTH_HEADER" = "%s" }\n' \
        "$server" "$mcp_url" "$auth" >> "$toml"
      chmod 600 "$toml"
      ok "Codex: ~/.codex/config.toml"
    fi
    found=1
  fi
  [[ "$found" == 0 ]] && warn "no supported agents detected on this machine - MCP endpoint: $mcp_url"

  for base in "$AGENTS_HOME/skills" "$HOME/.claude/skills"; do
    mkdir -p "$base"
    for s in "${SKILLS[@]}"; do rm -rf "$base/$(basename "$s")"; cp -R "$s" "$base/"; done
  done
  write_user_manifest
  ok "skills (user-level): ~/.agents/skills + ~/.claude/skills"
  say "  Agents already running need a restart / MCP reload to pick this up."
  blank
  warn "Prefer project-attached skills to these"
  note "Project copies are committed, so the team shares one version and a"
  note "project stays on the suite it was tested against. These follow your"
  note "machine instead, drift silently, and nobody else can see them."
  note "A project copy also overrides the one here, and some agents warn"
  note "about every collision at startup (Gemini prints one line per skill)."
  blank
  cmd "./install.sh --project <dir>    bind a project"
  cmd "./install.sh --clean-user       remove these copies"
}

# Records what the suite put at user level, so --clean-user removes only
# ours and leaves anything you installed yourself alone.
write_user_manifest() {
  local rows="" s name ver
  for s in "${SKILLS[@]}"; do
    name="$(basename "$s")"; ver="$(skill_version "$s")"
    rows="$rows| $name | ${ver:-?} |
"
  done
  mkdir -p "$AGENTS_HOME/skills"
  cat > "$AGENTS_HOME/skills/MANAGED.md" <<UEOF
# Managed skills (story-tools, user-level)

<!-- GENERATED by the story-tools installer. -->

**Prefer project-attached skills.** These user-level copies are not
committed anywhere, so they drift from what your projects were tested
against and nobody else sees them. A project copy of the same name
overrides the one here.

Remove them with \\`./install.sh --clean-user\\`.

| Skill | Version |
|---|---|
$rows
UEOF
}


# After a project install, point out that user-level copies are now
# shadowed - and noisy in agents that report every collision.
warn_user_level_overlap() {
  local overlap="" name
  for name in $(for s in "${SKILLS[@]}"; do basename "$s"; done); do
    [[ -e "$AGENTS_HOME/skills/$name" || -e "$HOME/.claude/skills/$name" ]] || continue
    overlap="$overlap $name"
  done
  [[ -n "$overlap" ]] || return 0
  local n; n="$(printf '%s\n' $overlap | wc -w | tr -d ' ')"
  blank
  warn "$n of these also exist at user level (~/.agents/skills)"
  note "This project's copies win, which is what you want - but some agents"
  note "warn about every collision at startup (Gemini prints one per skill)."
  note "The project copies are the ones worth keeping: committed, shared,"
  note "and pinned to what this project was tested against."
  cmd "./install.sh --clean-user"
}

# ---------- step: remove user-level skills ----------
clean_user_mode() {
  local found="" name d owned="" src=""
  local man="$AGENTS_HOME/skills/MANAGED.md"

  # The manifest is the record of what WE put here, so it is the
  # authority - it still names skills this version has since renamed or
  # dropped. Without one (installed before manifests, or hand-made), fall
  # back to the current suite plus what it has retired. Either way we only
  # ever name our own; anything else in these directories is untouched.
  if [[ -f "$man" ]]; then
    owned="$(sed -nE 's/^\| ([A-Za-z0-9_-]+) \| .*/\1/p' "$man")"
    src="manifest"
  fi
  if [[ -z "$owned" ]]; then
    owned="$(for s in "${SKILLS[@]}"; do basename "$s"; done)"
    src="current suite"
  fi
  owned="$owned
$(printf '%s\n' "${RETIRED_SKILLS[@]}")"

  for name in $owned; do
    [[ -n "$name" ]] || continue
    for d in "$AGENTS_HOME/skills" "$HOME/.claude/skills"; do
      [[ -e "$d/$name" ]] || continue
      case " $found " in *" $name "*) continue;; esac
      found="$found $name"
    done
  done

  if [[ -z "$found" ]]; then
    ok "No user-level copies of suite skills found - nothing to clean."
    return 0
  fi

  step "User-level skills from this suite"
  note "source: $src"
  blank
  for name in $found; do say "    $name"; done
  blank
  note "Removed from ~/.agents/skills and ~/.claude/skills. Nothing else in"
  note "those directories is touched - skills you installed yourself stay."
  note "Projects keep their own committed copies; this is machine-wide only."
  local reply=""
  if [[ -t 0 ]]; then read -rp "  Remove them? [y/N] " reply; else reply="n"; fi
  case "$reply" in
    [yY]*) ;;
    *) note "Left alone."; return 0;;
  esac

  for name in $found; do
    rm -rf "$AGENTS_HOME/skills/$name" "$HOME/.claude/skills/$name"
  done
  rm -f "$AGENTS_HOME/skills/MANAGED.md"
  ok "Removed$found"
  note "Agents already running need a restart to stop seeing them."
}

# ---------- step: project binding (project-attached mode) ----------

version_gt() {  # $1 > $2 ? numeric per component, so 1.10 beats 1.9
  [[ "$1" == "$2" ]] && return 1
  local IFS=.; local -a A=($1) B=($2); local i
  for ((i=0; i<${#A[@]} || i<${#B[@]}; i++)); do
    local a="${A[i]:-0}" b="${B[i]:-0}"
    a="${a//[^0-9]/}"; b="${b//[^0-9]/}"
    (( 10#${a:-0} > 10#${b:-0} )) && return 0
    (( 10#${a:-0} < 10#${b:-0} )) && return 1
  done
  return 1
}

read_managed() {  # $1 dir -> "name version" lines from a project's MANAGED.md
  local f="$1/.agents/skills/MANAGED.md"
  [[ -f "$f" ]] || return 0
  sed -nE 's/^\| ([A-Za-z0-9_-]+) \| ([^ |]+) \|.*/\1 \2/p' "$f"
}

skill_version() {  # $1 = skill dir -> version string or ""
  sed -nE 's/^ *version: *"?([^"]+)"?$/\1/p' "$1/SKILL.md" 2>/dev/null | head -1
}

copy_skills() {  # $1 dir, $2 mode
  local dir="$1" mode="${2:-link}"
  mkdir -p "$dir/.agents/skills"
  local names="" rows="" name ver moved=""
  local prev_versions; prev_versions="$(read_managed "$dir")"
  for s in "${SKILLS[@]}"; do
    name="$(basename "$s")"; ver="$(skill_version "$s")"
    names="$names $name"
    local was; was="$(printf '%s\n' "$prev_versions" | awk -v n="$name" '$1==n{print $2}' | head -1)"
    if [[ -n "$was" && -n "$ver" && "$was" != "$ver" ]]; then
      moved="$moved
    $name $was -> $ver"
    fi
    rows="$rows| $name | ${ver:-?} |
"
    rm -rf "$dir/.agents/skills/$name"
    cp -R "$s" "$dir/.agents/skills/$name"
    for agent_dir in .claude .github; do
      mkdir -p "$dir/$agent_dir/skills"
      rm -rf "$dir/$agent_dir/skills/$name"
      if [[ "$mode" == "copy" ]]; then cp -R "$s" "$dir/$agent_dir/skills/$name"
      else ln -s "../../.agents/skills/$name" "$dir/$agent_dir/skills/$name"; fi
    done
  done

  # prune what we used to manage: previously-listed skills that are no
  # longer in SKILLS, plus anything explicitly retired. Read the OLD
  # manifest before overwriting it - that is the record of what was ours.
  local pruned="" prev old_man="$dir/.agents/skills/MANAGED.md"
  if [[ -f "$old_man" ]]; then
    while IFS= read -r prev; do
      [[ -n "$prev" ]] || continue
      case " $names " in *" $prev "*) continue;; esac        # still managed
      [[ -d "$dir/.agents/skills/$prev" ]] || continue
      rm -rf "$dir/.agents/skills/$prev" \
             "$dir/.claude/skills/$prev" "$dir/.github/skills/$prev"
      pruned="$pruned $prev"
    done < <(sed -nE 's/^\| ([A-Za-z0-9_-]+) \| .*/\1/p' "$old_man")
  fi
  for prev in "${RETIRED_SKILLS[@]}"; do
    case " $names " in *" $prev "*) continue;; esac
    case " $pruned " in *" $prev "*) continue;; esac
    [[ -d "$dir/.agents/skills/$prev" ]] || continue
    rm -rf "$dir/.agents/skills/$prev" \
           "$dir/.claude/skills/$prev" "$dir/.github/skills/$prev"
    pruned="$pruned $prev"
  done

  # manifest: which skills this installer owns (agents and humans read this
  # before editing anything under .agents/skills/)
  cat > "$dir/.agents/skills/MANAGED.md" <<MEOF
# Managed skills (story-tools)

<!-- GENERATED by the story-tools installer - rewritten on every refresh. -->

The skills listed below are **installed copies**. The installer
**overwrites them in place** on every refresh - any edit made here is
lost, silently, at the next run.

| Skill | Version |
|---|---|
$rows
**Improving a managed skill is discovered work.** Do not edit these
files: capture the improvement as an issue (or tell the user) so it
lands in the story-tools source repo and reaches every project. Same
rule for their \`references/\` and \`scripts/\`.

A skill that WAS managed and no longer appears here has been removed
from this project on refresh - retired upstream, not lost. Only skills
this suite owns are ever pruned; independent and project-local skills
are left alone.

Any OTHER skill directory beside these is **yours** - project-local, and
the installer never touches it. Add project-specific skills freely; just
don't name one after a managed skill.
MEOF

  ok "skills updated (managed by story-tools):$names"
  [[ -n "$pruned" ]] && ok "retired skills removed:$pruned"
  [[ -n "$moved" ]] && { ok "versions changed:"; printf '%s\n' "$moved"; }
  say "  .agents/skills/MANAGED.md lists them - installed copies are overwritten on refresh"

  # anything else in the dir belongs to the project: name it, never touch it
  local others="" d base
  for d in "$dir"/.agents/skills/*/; do
    [[ -d "$d" ]] || continue
    base="$(basename "$d")"
    case " $names " in *" $base "*) ;; *) others="$others $base";; esac
  done
  if [[ -n "$others" ]]; then
    say "  project's own skills (untouched):$others"
  fi
}

migrate_docs_layout() {  # $1 dir - offer to clear copies left at the old paths
  # WORKFLOW.md and dimensions.md used to be written under docs/. Both are
  # generated, so a refresh writes the new location and the old file simply
  # lingers - two copies, one of them quietly going stale. Never move or
  # delete anything in someone's repo without asking.
  local dir="$1" stale=() n
  [[ -f "$dir/docs/WORKFLOW.md" ]]   && stale+=("docs/WORKFLOW.md|WORKFLOW.md")
  [[ -f "$dir/docs/dimensions.md" ]] && stale+=("docs/dimensions.md|.agents/config/dimensions.md")
  [[ ${#stale[@]} -eq 0 ]] && return 0

  blank
  warn "these moved in a newer version of story-tools:"
  for n in "${stale[@]}"; do
    say "    ${n%%|*}  ->  ${n##*|}"
  done
  say "  Both are generated, so the copy under docs/ is now dead weight and"
  say "  will drift from the real one."

  if [[ ! -t 0 ]]; then
    say "  Not interactive - left alone. Re-run the installer to be asked."
    return 0
  fi
  local yn; read -rp "  Clear the old copies? [Y/n] " yn
  [[ "$yn" =~ ^[Nn] ]] && { say "  left as-is - you now have two of each."; return 0; }

  local old new
  for n in "${stale[@]}"; do
    old="$dir/${n%%|*}"; new="$dir/${n##*|}"
    if [[ -f "$new" ]]; then
      rm -f "$old" && ok "removed ${n%%|*} (superseded by ${n##*|})"
    else
      mkdir -p "$(dirname "$new")"
      mv "$old" "$new" && ok "moved ${n%%|*} -> ${n##*|}"
    fi
  done
  say "  These are tracked files - review the diff before committing."
}

# ---------- step: KB section directories -> plain lowercase names ----------
# taxonomy.md used to say section names are "spelled out - Architecture
# Decision Records, never adr" without separating the KB title from the
# path, so tracker-less projects grew directories with spaces in them.
# Titles are still spelled out; they live in each section's README H1,
# which is what the sync reads. The directory is now a plain word.
KB_RENAMES=""

migrate_kb_dirs() {  # $1 dir - offer to rename KB section directories
  local dir="$1"
  local kb="$dir/docs/knowledge"
  [[ -d "$kb" ]] || return 0

  # The scan runs as its own statement, not inside $( ): bash 3.2 - which is
  # what macOS ships as /bin/bash - cannot parse a here-document nested in a
  # command substitution, and fails somewhere else entirely when it tries.
  local plan tmp
  tmp="${TMPDIR:-/tmp}/story-tools-kb.$$"
  KB="$kb" python3 - > "$tmp" <<'MKPY'
import os, re
kb = os.environ["KB"]
MAP = {
    "architecture-decision-records": "decisions", "decision-records": "decisions",
    "adr": "decisions", "adrs": "decisions",
    "product-requirements": "requirements", "prd": "requirements",
    "prds": "requirements",
    "specifications": "specifications", "spec": "specifications",
    "specs": "specifications",
    "research": "research",
    "reference": "reference", "references": "reference",
    "developer-guides": "guides", "dev-guides": "guides", "guides": "guides",
    "quality-assurance": "testing", "qa": "testing",
    "tests": "testing", "testing": "testing",
    "mandates-compliance": "compliance", "mandates-and-compliance": "compliance",
    "mandates": "compliance", "compliance": "compliance",
    "regulations": "compliance",
    "support": "support",
}
# Design direction moved to DESIGN.md at the repo root and accessibility
# split between DESIGN.md and compliance/. Nothing to rename to - a person
# decides where each file goes.
FLAG = {"design-accessibility", "design-and-accessibility", "design",
        "accessibility", "a11y"}

def norm(s):
    s = s.lower().replace("&", " ").replace("_", " ").replace("-", " ")
    return re.sub(r"\s+", "-", s.strip())

for name in sorted(os.listdir(kb)):
    p = os.path.join(kb, name)
    if not os.path.isdir(p) or name.startswith("."):
        continue
    # ID-prefixed directories are the sync's own naming, not a misnamed
    # section - nothing to report about them.
    if re.match(r"^[A-Z][A-Z0-9]*-A-[0-9]+_", name):
        continue
    n = norm(name)
    if n in FLAG:
        print("FLAG\t%s\t" % name)
    elif n in MAP and MAP[n] != name:
        print("MOVE\t%s\t%s" % (name, MAP[n]))
    elif n not in MAP and name != n:
        print("ODD\t%s\t%s" % (name, n))
MKPY
  plan="$(cat "$tmp")"
  rm -f "$tmp"
  [[ -z "$plan" ]] && return 0

  # Under a sync the directory name is derived, not chosen: structure flows
  # DOWN only, and the sync moves anything it finds to <ID>_<title-slug>.
  # So a local rename is futile before it is dangerous - the next sync undoes
  # it. Say that rather than going quiet: doing nothing without a word is how
  # someone concludes the installer did not look.
  if [[ -d "$kb/.yt-sync" || -d "$kb/.gh-wiki-sync" ]] \
     || ls "$kb" 2>/dev/null | grep -qE '^[A-Z][A-Z0-9]*-A-[0-9]+_'; then
    blank
    heads_up "  knowledge sections here are named by the sync, not by hand:"
    say "  <ID>_<title-slug>, derived from the article. Renaming one locally does"
    say "  nothing - structure flows down only, and the next sync moves it back."
    say "  To change a section name, rename the ARTICLE in the tracker and the"
    say "  tree follows."
    return 0
  fi

  local kind old new moves=0 flags=0
  blank
  warn "knowledge sections use directory names this version no longer writes:"
  while IFS=$'\t' read -r kind old new; do
    [[ -n "$kind" ]] || continue
    case "$kind" in
      MOVE) say "    $old  ->  $new"; moves=$((moves+1));;
      ODD)  say "    $old  ->  $new   (not a known section - best guess)"; moves=$((moves+1));;
      FLAG) flags=$((flags+1));;
    esac
  done <<< "$plan"

  if [[ $flags -gt 0 ]]; then
    heads_up "  design/accessibility sections are NOT renamed - they were split:"
    say "    design direction and tokens  ->  DESIGN.md at the repo root"
    say "    accessibility standards      ->  DESIGN.md (rules) + compliance/ (the legal floor)"
    say "    records with mockups         ->  docs/design/ (unchanged)"
    say "  Move those files yourself; there is no single target to rename to."
  fi
  [[ $moves -eq 0 ]] && return 0

  say "  Section TITLES do not change - they live in each README's H1."
  if [[ ! -t 0 ]]; then
    say "  Not interactive - left alone. Re-run the installer to be asked."
    return 0
  fi
  local yn; read -rp "  Rename them? [y/N] " yn
  [[ "$yn" =~ ^[Yy] ]] || { say "  left as-is."; return 0; }

  local git_ok=0
  git -C "$dir" rev-parse --git-dir >/dev/null 2>&1 && git_ok=1
  KB_RENAMES=""
  while IFS=$'\t' read -r kind old new; do
    case "$kind" in MOVE|ODD) ;; *) continue;; esac
    if [[ -e "$kb/$new" ]]; then
      warn "  $new already exists - skipped $old (merge it by hand)"
      continue
    fi
    if [[ $git_ok -eq 1 ]] && git -C "$dir" mv "docs/knowledge/$old" "docs/knowledge/$new" 2>/dev/null; then
      ok "$old -> $new (git mv, history kept)"
    elif mv "$kb/$old" "$kb/$new"; then
      ok "$old -> $new"
    else
      warn "  could not rename $old - do it by hand"
      continue
    fi
    KB_RENAMES="$KB_RENAMES $old"
  done <<< "$plan"

  # Links are the half that breaks quietly. Report them; never rewrite -
  # a bad sweep across a docs tree is discovered months later.
  local hits=""
  for old in $KB_RENAMES; do
    if grep -rlF "$old" "$dir/docs" --include='*.md' 2>/dev/null | head -1 | grep -q .; then
      hits="$hits $old"
    fi
  done
  if [[ -n "$hits" ]]; then
    blank
    heads_up "  markdown still referring to the old paths:"
    for old in $hits; do
      say "    $old"
      grep -rlF "$old" "$dir/docs" --include='*.md' 2>/dev/null | sed "s|^$dir/|      |"
    done
    say "  Not rewritten on purpose - check each one."
  fi
  say "  Tracked files: review the diff before committing."
}

# ---------- step: is the tracker snapshot committed, or local? ----------
# Committed is valuable when you cannot reach the tracker: a clone carries
# the stories with it. It is also a shared file that every pull rewrites,
# so on a team two people regenerating the same derived data collide. The
# choice is the project's, not the person's, so it lives in the pointer.
GITIGNORE_BEGIN="# BEGIN story-tools (generated - do not edit between markers)"
GITIGNORE_END="# END story-tools"

set_gitignore_block() {  # $1 dir, $2 body ("" removes the block)
  local dir="$1" body="$2"
  local f="$dir/.gitignore"
  BODY="$body" B="$GITIGNORE_BEGIN" E="$GITIGNORE_END" F="$f" python3 - <<'GIPY'
import os, re
f, body = os.environ["F"], os.environ["BODY"]
b, e = os.environ["B"], os.environ["E"]
cur = open(f).read() if os.path.exists(f) else ""
cur = re.sub(re.escape(b) + r".*?" + re.escape(e) + r"\n?", "", cur, flags=re.S)
if body:
    if cur and not cur.endswith("\n"):
        cur += "\n"
    cur += f"{b}\n{body}\n{e}\n"
open(f, "w").write(cur)
GIPY
}

untrack_snapshot() {  # $1 dir - offer to stop tracking a snapshot committed before
  local dir="$1" tracked=""
  git -C "$dir" rev-parse --git-dir >/dev/null 2>&1 || return 0
  tracked="$(git -C "$dir" ls-files docs/stories .agents/config/dimensions.md 2>/dev/null | head -1)"
  [[ -n "$tracked" ]] || return 0

  heads_up "The snapshot is still tracked in git - the setting alone will not help"
  say "  Git keeps tracking whatever it already tracks, ignore rules or not."
  say "  So every pull still rewrites these files, everyone still gets the"
  say "  diff, and two people pulling on different days still collide."
  blank
  say "  ${C_B}Fix it once, here:${C_0}"
  cmd "git rm -r --cached docs/stories .agents/config/dimensions.md"
  cmd "git commit -m 'untrack the generated tracker snapshot'"
  cmd "git push"
  blank
  say "  ${C_B}Then everyone else, once, after pulling that:${C_0}"
  cmd "git checkout -- docs/stories     # discard local edits - regenerable"
  cmd "git pull                         # the files vanish; that is correct"
  note "then a normal snapshot pull regenerates their own copy, ignored"
  blank

  if [[ ! -t 0 ]]; then
    say "  (not interactive - run the first block yourself)"
    return 0
  fi
  local yn; read -rp "  Run the untrack step now? Files stay on disk. [Y/n] " yn
  [[ "$yn" =~ ^[Nn] ]] && { say "  left tracked - nothing changes until you do this."; return 0; }

  # Never claim this worked without checking. Hooks, permissions and managed
  # checkouts all refuse `git rm`, and a false "done" here is worse than not
  # trying: the setting looks applied and the conflicts keep happening.
  local out="" rc=0 did=0
  out="$(git -C "$dir" rm -r --cached --quiet docs/stories 2>&1)" || rc=$?
  [[ $rc -eq 0 ]] && did=1
  rc=0
  if git -C "$dir" ls-files --error-unmatch .agents/config/dimensions.md >/dev/null 2>&1; then
    local out2=""
    out2="$(git -C "$dir" rm --cached --quiet .agents/config/dimensions.md 2>&1)" || rc=$?
    [[ -z "$out" ]] && out="$out2"   # keep the first error, not both concatenated
    [[ $rc -eq 0 ]] && did=1
  fi

  if [[ $did -eq 1 ]] && [[ -z "$(git -C "$dir" ls-files docs/stories 2>/dev/null | head -1)" ]]; then
    ok "untracked - files untouched on disk, deletions staged"
    say "  Commit and push, then tell the others to run the second block."
    return 0
  fi

  heads_up "Could not untrack it here - please run it yourself"
  [[ -n "$out" ]] && note "git said: $(printf '%s' "$out" | head -1)"
  say "  Some setups refuse this from a script - hooks, permissions, or a"
  say "  managed checkout. Nothing has changed. Run the first block above by"
  say "  hand, or do it in whatever git client you normally use."
  return 0
}

set_snapshot_mode() {  # $1 dir - derived, not asked
  local dir="$1" cur mode ttype
  cur="$(read_pointer "$dir" snapshot)"
  ttype="$(read_pointer "$dir" type)"
  # Committed exists for the case where you CANNOT reach the tracker. If the
  # project has one configured, you can - so this follows from the binding
  # and there is nothing to ask. Set `snapshot` in the pointer by hand to
  # override; a value already there is always respected.
  case "${SNAPSHOT_ARG:-$cur}" in
    s|synced|local) mode="synced";;      # 'local' accepted from earlier runs
    c|committed)    mode="committed";;
    "") # An already-committed snapshot IS a choice - somebody put it in git,
        # and for one person working alone that is a good one: no conflicts
        # to have, and a copy readable with no tracker. Never flip it
        # silently. Only a project with nothing tracked gets the new default.
        if git -C "$dir" ls-files docs/stories 2>/dev/null | head -1 | grep -q .; then
          mode="committed"
        else
          case "$ttype" in
            github|youtrack) mode="synced";;
            *)               mode="committed";;
          esac
        fi;;
    *) warn "unknown snapshot mode '${SNAPSHOT_ARG:-$cur}' - leaving as-is"; return 0;;
  esac
  merge_json "$dir/.agents/config/story-tools.json" "snapshot" '"'"$mode"'"'
  if [[ "$mode" == "synced" ]]; then
    set_gitignore_block "$dir" "docs/stories/
.agents/config/dimensions.md"
    ok "snapshot: synced from the tracker, gitignored - no two people regenerating the same files"
    untrack_snapshot "$dir"
  else
    set_gitignore_block "$dir" ""
    if [[ -n "$ttype" && "$ttype" != "none" ]]; then
      ok "snapshot: committed - already in git, left that way (set snapshot to 'synced' in the pointer if a team starts colliding on it)"
    else
      ok "snapshot: committed - no tracker to sync from, so it travels with the repo"
    fi
  fi
}

write_pages_config() {  # $1 dir - keep GitHub Pages off the internal docs tree
  # GitHub offers "deploy from a branch" with a /docs source, and picking it
  # publishes everything under docs/ - including the knowledge tree and the
  # story snapshot. Nothing else claims docs/; this is one host's shortcut
  # landing on a directory that already means something here. Generated, so
  # it stays in step with what the suite actually writes.
  local dir="$1"
  local f="$dir/docs/_config.yml"
  mkdir -p "$dir/docs"
  if [[ -f "$f" ]] && ! grep -q "GENERATED by the story-tools installer" "$f"; then
    warn "docs/_config.yml exists and is not ours - leaving it alone."
    say  "  If this project publishes with GitHub Pages, add to its exclude list:"
    say  "    knowledge/"
    say  "    stories/"
    return 0
  fi
  cat > "$f" <<'PGEOF'
# GENERATED by the story-tools installer - regenerated on every refresh.
#
# GitHub Pages can publish from a /docs branch source. These directories are
# internal: the knowledge tree is the project's own record, and the story
# snapshot is generated from the tracker. Neither is a public website.
exclude:
  - knowledge/
  - stories/
PGEOF
  ok "docs/_config.yml written (keeps the docs tree out of a GitHub Pages build)"
}

write_workflow_doc() {  # $1 dir, $2 tracker (youtrack|github|none), $3 detail (project key or owner/repo)
  local dir="$1" tracker="$2" detail="$3"
  local stage_note capture_note tracker_line docs_row
  case "$tracker" in
    github)
      tracker_line="GitHub Issues + Project board ($detail)"
      stage_note="Picking up a story moves it to the board's in-progress column; completing moves it to the review column when the board has one, else Done (and closes the issue)."
      capture_note="Issues carry everything: the story narrative, the \`## Acceptance Criteria\` task list, and labels."
      docs_row='| "sync docs" | Two-way sync `docs/knowledge/` with the repo wiki (when the wiki is enabled) |'
      ;;
    youtrack)
      tracker_line="YouTrack ($detail)"
      stage_note="Picking up a story moves Stage to the in-progress column; completing moves Stage to the testing/review column (a human promotes to Done) and sets State to the resolution."
      capture_note="Issues carry everything: the story narrative, the \`## Acceptance Criteria\` task list, and tags."
      docs_row='| "sync docs" | Two-way sync `docs/knowledge/` with the YouTrack knowledge base |'
      ;;
    *)
      tracker_line="none yet - agents work offline and reconcile later"
      stage_note="Without a tracker, agents keep a local worklog and replay it when one is adopted."
      capture_note="Captured items live in the pending log until a tracker exists."
      docs_row=""
      ;;
  esac
  cat > "$dir/WORKFLOW.md" <<WFEOF
# How Work Flows Here

<!-- GENERATED by the story-tools installer - regenerated on every refresh.
     Edit the skills repo, not this file. -->

Tracker: $tracker_line

Work moves through one loop: **capture -> triage -> ready -> in progress ->
done**. Agents (Claude Code, Gemini, Antigravity, Copilot) enforce it; you
steer it with plain language.

## What to say to your agent

| You say | What happens |
|---|---|
| "record this for later" | Cheap capture: an issue with what's known + open questions, labeled \`needs-triage\`. No AC yet - never block a capture |
| "show me what needs attention" | The triage queue: untriaged, needs-triage, answered needs-info |
| "triage #123" | Reproduce (bugs), grill if needed, set priority, then disposition |
| "work on #123" / "start the next story" | Focus it, restate scope, move it onto the board |
| "that's done, check it off" | One AC item checked - only when verifiably complete |
| "is this done?" | Completion check: all AC? QA if required? open discovered work? |
| "we worked offline, reconcile" | Replay a local worklog into the tracker |
$docs_row

## The rules that keep scope honest

- **The AC checklist IS the scope.** $capture_note
- **Discovered work never expands the current story.** Anything out of
  scope becomes a NEW linked issue (labeled \`discovered\`) and you return
  to the story. Quick wins included.
- **Triage owns priority.** Discovered work arrives at default priority on
  purpose - urgency is decided at triage, not inherited.
- **AC is authored at triage,** not at capture. A capture is a problem
  statement, not a story yet.
- $stage_note
- **Time is sessions.** One human-approved entry at session close - agents
  never log time silently.

## Who does what

Each person answers one question when they run \`.agents/setup.sh\`, and it
is remembered for them - not for the repo. Nothing here is stored in the
project, because the project is shared: the skills and this file are
committed, so a fresh clone looks fully set up even when the person is not.

| Role | Does |
|---|---|
| \`developer\` | Implements features. Works ready stories, files issues and bugs, routes anything unclear back to triage |
| \`lead\` | Manages the work. Triage routing: priority, subsystem, deciding a story is ready |
| \`architect\` | Makes the technical calls. Architecture decisions, ADRs, research records |
| \`product\` | Decides what the product does. PRDs and requirements |

They are not exclusive - most people hold several, and working solo means
holding all of them, which is what pressing Enter at the prompt gives you.

**It is a hint, not a permission.** Agents use it to decide what to offer
you; the tracker decides what you can actually do. The point is that nobody
gets steered into work that is not theirs to do - a developer handed a story
with no acceptance criteria hands it back to triage rather than inventing
the requirements, and nobody ends up writing a PRD because that was where
the workflow happened to point.

**Capture is never gated.** Anyone can file an issue or a bug, whatever
their roles. That is how work reaches the person who can decide about it.

## The tracker snapshot

\`docs/stories/\` is generated from the tracker - never edit it, and never
hand-merge a conflict in it. If neither side has a change the tracker does
not already have, take either side and re-run the pull; if one does, get
that change into the tracker first, then re-pull. The offline pending log
is the opposite case: it is append-only, so a conflict there keeps **both**
sides. Whether
it is committed to the repo or gitignored and regenerated per developer is
a project setting; if you are hitting conflicts on files nobody wrote, that
setting is the fix.

## Label / tag legend

\`needs-triage\` awaiting triage - \`triaged\` has been dispositioned (never
comes off) - \`ready-for-agent\` an agent can pick it up - \`ready-for-human\`
needs a person - \`needs-info\` waiting on the reporter - \`wontfix\` closed,
reason recorded - \`discovered\` born from other work - \`needs-gherkin\`
completion requires a QA section. Everything else is topical grouping
(Title Case) - human-added labels are data, agents never remove them.

## Managed vs yours

The skills under \`.agents/skills/\` listed in
\`.agents/skills/MANAGED.md\` are installed copies - the installer
overwrites them on every refresh, so an edit there is lost silently. A
skill improvement is discovered work: capture it as an issue so it
reaches the source repo. Any other skill directory is the project's own
and is never touched.

## Local mirrors (generated - never hand-edit)

- \`docs/stories/\` - issue snapshot for agent context (refresh: the
  story-reconcile skill's pull script)
- \`.agents/config/dimensions.md\` - the real board columns, fields, and labels

## Skill updates

This project records whether it should check for newer versions of the
story-tools skills (\`updates.check\` in
\`.agents/config/story-tools.json\`). When on, \`.agents/setup.sh\`
compares what is installed here against what the suite publishes and
OFFERS to update - it never changes anything without asking. Skills are
tracked files, so an update shows up as a diff for you to review and
commit. Set it to false to stay pinned.

## New developer?

Clone, then run \`.agents/setup.sh\` once - it sets up your own tracker
credential (or lets you opt out and work offline) and registers the
tracker in your agents. Nothing else to download. Restart your agent and
say "what am I working on?". Details:
.agents/skills/story-workflow/references/.
WFEOF
  ok "WORKFLOW.md written at the repo root (human-facing guide, regenerated on refresh)"
}

verify_bind() {  # $1 dir - post-condition: did the bind actually land?
  local dir="$1" missing=""
  [[ -f "$dir/.agents/config/story-tools.json" ]] \
    || missing="$missing
    .agents/config/story-tools.json  (the pointer - update checks read it)"
  [[ -f "$dir/.agents/setup.sh" ]] \
    || missing="$missing
    .agents/setup.sh                 (teammate onboarding)"
  [[ -d "$dir/.agents/skills" ]] \
    || missing="$missing
    .agents/skills/                  (the skills themselves)"
  [[ -z "$missing" ]] && return 0
  say ""
  warn "BIND INCOMPLETE - this project is NOT set up, despite what is above."
  say  "  Missing:$missing"
  say  "  Nothing above should be trusted. Re-run and watch for an error:"
  cmd  "./install.sh --project $dir"
  return 1
}

attach_project_github() {  # $1 dir, $2 owner/repo, $3 project number|"", $4 readonly, $5 mode
  local dir="$1" gh_repo="$2" gh_proj="$3" readonly_flag="$4" mode="${5:-link}"
  local conn="${GH_CONN:-github}" srv
  srv="$(gh_server_name "$conn")"
  copy_skills "$dir" "$mode"
  warn_user_level_overlap
  rm -f "$dir/.agents/youtrack.json" "$dir/.agents/config/youtrack.json" "$dir/.agents/config/story-tools.json"
  merge_json "$dir/.agents/config/story-tools.json" "tracker" '{
    "type": "github",
    "connection": "'"$conn"'",
    "mcpServer": "'"$srv"'",
    "repo": "'"$gh_repo"'",
    "prefix": "'"${GH_PREFIX:-$(printf '%s' "${gh_repo##*/}" | tr -cd 'A-Za-z' | cut -c1-2 | tr '[:lower:]' '[:upper:]')}"'"'"${gh_proj:+,
    \"project\": \"$gh_proj\"}${readonly_flag:+,
    \"readOnly\": true}"'
  }'
  ok "pointer: .agents/config/story-tools.json (github: $gh_repo${gh_proj:+, project $gh_proj}) - commit .agents/ .claude/ .github/ with the repo"
  write_workflow_doc "$dir" github "$gh_repo"
  write_pages_config "$dir"
  set_snapshot_mode "$dir"
  migrate_docs_layout "$dir"
  migrate_kb_dirs "$dir"
  ask_roles "$dir"
  write_updates_config "$dir"
  write_topical_tags "$dir" github
  ship_setup "$dir"
  # seed .agents/config/dimensions.md right away so triage can prompt real values
  # before any snapshot pull has ever run (best-effort, needs the token)
  local ghpull="$REPO_DIR/skills/stories/story-reconcile/scripts/gh-pull.sh"
  [[ -x "$ghpull" ]] || ghpull="$REPO_DIR/skills/story-reconcile/scripts/gh-pull.sh"
  if [[ -n "${GITHUB_TOKEN:-}" && -x "$ghpull" ]]; then
    if (cd "$dir" && GITHUB_TOKEN="$GITHUB_TOKEN" "$ghpull" "$gh_repo" --dimensions-only >/dev/null 2>&1); then
      ok ".agents/config/dimensions.md seeded (board columns, fields, labels)"
    else
      warn "could not seed .agents/config/dimensions.md - run the story-reconcile pull later"
    fi
  fi
  # wiki capability check - docs two-way sync (project-docs skill)
  if [[ -n "${GITHUB_TOKEN:-}" ]]; then
    local has_wiki
    has_wiki="$(curl -sfS -m 10 -H "Authorization: Bearer $GITHUB_TOKEN" \
      -H "Accept: application/vnd.github+json" \
      "https://api.github.com/repos/$gh_repo" 2>/dev/null \
      | grep -oE '"has_wiki": *(true|false)' | grep -oE 'true|false' || true)"
    if [[ "$has_wiki" == "true" ]]; then
      if command -v git >/dev/null 2>&1 \
         && git ls-remote "https://x-access-token:${GITHUB_TOKEN}@github.com/${gh_repo}.wiki.git" >/dev/null 2>&1; then
        ok "wiki detected - docs two-way sync available (project-docs skill, 'sync docs')"
      else
        warn "wiki enabled but uninitialized - create the Home page once in the web UI to enable docs sync"
      fi
    elif [[ "$has_wiki" == "false" ]]; then
      say "  wiki disabled on $gh_repo - docs/knowledge stays git-native (enable the wiki to sync docs)"
    fi
  fi
}

attach_project() {  # $1 dir, $2 yt_project, $3 readonly(true|""), $4 mode
  local dir="$1" yt_project="$2" readonly_flag="$3" mode="${4:-link}"
  copy_skills "$dir" "$mode"
  warn_user_level_overlap

  rm -f "$dir/.agents/youtrack.json" "$dir/.agents/config/youtrack.json" "$dir/.agents/config/story-tools.json"   # regenerate cleanly
  merge_json "$dir/.agents/config/story-tools.json" "tracker" '{
    "type": "youtrack",
    "connection": "'"$PROFILE"'",
    "mcpServer": "'"${MCP_SERVER:-youtrack}"'",
    "url": "'"${YOUTRACK_URL%/}"'"'"${yt_project:+,
    \"project\": \"$yt_project\"}${readonly_flag:+,
    \"readOnly\": true}"'
  }'
  ok "pointer: .agents/config/story-tools.json - commit .agents/ .claude/ .github/ with the repo"
  write_workflow_doc "$dir" youtrack "${yt_project:-$YOUTRACK_URL}"
  write_pages_config "$dir"
  set_snapshot_mode "$dir"
  migrate_docs_layout "$dir"
  migrate_kb_dirs "$dir"
  ask_roles "$dir"
  # refresh .agents/config/dimensions.md so agents see the current fields, versions
  # and the full tag list - the GitHub path already does this
  local ytpull="$REPO_DIR/skills/stories/story-reconcile/scripts/yt-pull.sh"
  [[ -f "$ytpull" ]] || ytpull="$REPO_DIR/skills/story-reconcile/scripts/yt-pull.sh"
  if [[ -n "${yt_project:-}" && -f "$ytpull" ]]; then
    if (cd "$dir" && YOUTRACK_URL="$YOUTRACK_URL" YOUTRACK_TOKEN="$YOUTRACK_TOKEN" \
        bash "$ytpull" "$yt_project" --dimensions-only >/dev/null 2>&1); then
      ok ".agents/config/dimensions.md refreshed (fields, versions, workflow + topical tags)"
    else
      warn "could not refresh .agents/config/dimensions.md - run the story-reconcile pull later"
    fi
  fi
  write_updates_config "$dir"
  write_topical_tags "$dir" youtrack
  ship_setup "$dir"
}

pick_project() {  # sets PROJECT_DIR/PROJECT_NAME (may be empty = user-level only)
  PROJECT_DIR=""; PROJECT_NAME=""
  # default to the directory we're standing in when it looks like a project;
  # never prefill some OTHER project from history
  local here=""
  if [[ -d .git || -d .agents || -f AGENTS.md ]]; then here="$PWD"; fi
  local dir; dir="$(ask "Project repo to set up (path; Enter for user-level setup only)" "$here")"
  [[ -z "$dir" ]] && { say "  user-level setup only - bind a project later with --project <dir>"; return; }
  dir="${dir/#\~/$HOME}"
  [[ -d "$dir" ]] || { warn "$dir is not a directory - continuing with user-level setup only"; return; }
  PROJECT_DIR="$(cd "$dir" && pwd)"
  PROJECT_NAME="$(basename "$PROJECT_DIR")"
  ok "project: $PROJECT_NAME ($PROJECT_DIR)"
  if [[ -f "$PROJECT_DIR/.agents/config/story-tools.json" || -f "$PROJECT_DIR/.agents/youtrack.json" ]]; then
    say "  existing story-tools config found - its values prefill the next steps"
  fi
}

enable_time_tracking() {  # $1 = YouTrack project key; best-effort, never fatal
  local key="$1" pid settings enabled est spent
  pid=$(curl -sS -m 15 -H "Authorization: Bearer $YOUTRACK_TOKEN" \
      "${YOUTRACK_URL%/}/api/admin/projects?fields=id,shortName&query=$key" 2>/dev/null \
    | sed -nE 's/.*\{"id":"([^"]+)","shortName":"'"$key"'".*/\1/p' | head -1)
  # tolerate field-order differences
  [[ -z "$pid" ]] && pid=$(curl -sS -m 15 -H "Authorization: Bearer $YOUTRACK_TOKEN" \
      "${YOUTRACK_URL%/}/api/admin/projects?fields=shortName,id&query=$key" 2>/dev/null \
    | sed -nE 's/.*"shortName":"'"$key"'","id":"([^"]+)".*/\1/p' | head -1)
  if [[ -z "$pid" ]]; then
    say "  time tracking: could not look up project $key (needs admin read) - enable it"
    say "  manually if you want spent-time logging: Project Settings > Time Tracking."
    return 0
  fi
  settings=$(curl -sS -m 15 -H "Authorization: Bearer $YOUTRACK_TOKEN" \
      "${YOUTRACK_URL%/}/api/admin/projects/$pid/timeTrackingSettings?fields=enabled,estimate(field(name)),timeSpent(field(name))" 2>/dev/null)
  if [[ "$settings" == *'"enabled":true'* ]]; then
    say "  time tracking: already enabled on $key"
  else
    local code
    code=$(curl -sS -m 15 -o /dev/null -w "%{http_code}" -X POST \
      -H "Authorization: Bearer $YOUTRACK_TOKEN" -H "Content-Type: application/json" \
      -d '{"enabled":true}' \
      "${YOUTRACK_URL%/}/api/admin/projects/$pid/timeTrackingSettings?fields=enabled" 2>/dev/null)
    if [[ "$code" == 200 ]]; then
      say "  time tracking: enabled on $key (story_log_work records session time)"
    else
      say "  time tracking: could not enable on $key (HTTP $code - usually needs project"
      say "  admin). Enable manually: Project Settings > Time Tracking. Skipping."
      return 0
    fi
    settings=$(curl -sS -m 15 -H "Authorization: Bearer $YOUTRACK_TOKEN" \
      "${YOUTRACK_URL%/}/api/admin/projects/$pid/timeTrackingSettings?fields=enabled,estimate(field(name)),timeSpent(field(name))" 2>/dev/null)
  fi
  [[ "$settings" != *'"estimate":{"field"'* ]] && \
    say "  note: no Estimation field attached yet - add one under Project Settings >"
  [[ "$settings" != *'"estimate":{"field"'* ]] && \
    say "  Time Tracking if you want estimates on stories (the to-issues skill sets them)."
  return 0
}

bind_project_interactive() {  # uses PROJECT_DIR/PROJECT_NAME from pick_project
  [[ -z "$PROJECT_DIR" ]] && { say "  no project selected - skipped"; return; }
  # collision check: already bound to a DIFFERENT connection?
  local bound_conn bound_url
  bound_conn="$(read_pointer "$PROJECT_DIR" connection)"
  [[ -z "$bound_conn" ]] && bound_conn="$(read_pointer "$PROJECT_DIR" profile)"
  bound_url="$(read_pointer "$PROJECT_DIR" url)"
  if [[ -n "$bound_conn" && "$bound_conn" != "$PROFILE" ]]; then
    warn "'$PROJECT_NAME' is currently bound to connection '$bound_conn'${bound_url:+ ($bound_url)}"
    local yn; yn="$(ask "Rebind it to '$PROFILE' (${YOUTRACK_URL%/})? (y/N)" "n")"
    [[ "$yn" =~ ^[Yy]$ ]] || { say "  binding left unchanged - re-run and pick connection '$bound_conn' to update it"; return; }
  fi
  local cur_key cur_ro yt_project ro
  # default ONLY from this project's own pointer - never from the
  # connection's stored key (that is how a fresh repo lands on another
  # project's board). Reused connections state their server explicitly.
  cur_key="$(read_pointer "$PROJECT_DIR" project)"
  say "  Binding '$PROJECT_NAME' to ${YOUTRACK_URL%/} (connection '$PROFILE')"
  yt_project="$(ask "Project ID in YouTrack for '$PROJECT_NAME' (short key, e.g. EVO)" "$cur_key")"
  [[ -z "$yt_project" ]] && { say "  error: a project key is required (create the project in YouTrack first)" >&2; exit 1; }
  BOUND_KEY="$yt_project"
  ro="$(ask "Read-only? Agents propose changes but never write (y/N)" "${cur_ro:+y}")"
  [[ "$ro" =~ ^[Yy] ]] && ro="true" || ro=""
  attach_project "$PROJECT_DIR" "$yt_project" "$ro" link
  enable_time_tracking "$yt_project"
  verify_bind "$PROJECT_DIR" || true
  # remember for next run (most recent first, deduped)
  mkdir -p "$CONF_DIR"
  { echo "$PROJECT_DIR"; grep -vxF "$PROJECT_DIR" "$CONF_DIR/recent-projects" 2>/dev/null || true; } \
    > "$CONF_DIR/recent-projects.tmp" && mv "$CONF_DIR/recent-projects.tmp" "$CONF_DIR/recent-projects"
}

# ---------- flows ----------

github_wizard() {
  step "1/3 Project"
  pick_project
  local conn="github"
  # same shape as YouTrack: <tracker>-<project>, so a project that switches
  # tracker gets the parallel name and the type is visible in the filename
  [[ -n "$PROJECT_DIR" ]] && conn="github-$(basename "$PROJECT_DIR" | tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9-\n' '-')"
  step "2/3 GitHub credential"
  conn="$(ask "Connection name for this token" "$conn")"
  if [[ -f "$CONN_DIR/$conn.env" ]] && load_github "$conn"; then
    ok "using existing '$conn' credential - authenticated as '$GH_LOGIN'"
    register_agents_github "$conn"
  else
    setup_github "$conn"
  fi
  step "3/3 Bind"
  [[ -z "$PROJECT_DIR" ]] && { say "  no project chosen - bind later: ./install.sh --project <dir> --github owner/repo [--gh-project N]"; return; }
  local guess="" remote=""
  remote="$(git -C "$PROJECT_DIR" config --get remote.origin.url 2>/dev/null || true)"
  [[ "$remote" == *github.com* ]] && guess="$(printf '%s' "$remote" | sed -E 's#.*github\.com[:/]##; s#\.git$##')"
  local gh_repo gh_proj
  gh_repo="$(ask "GitHub repo (owner/name)" "$guess")"
  [[ -z "$gh_repo" ]] && { say "error: a repo is required" >&2; exit 1; }
  gh_proj="$(ask "Project (board) number - Enter for issues-only" "")"
  local pfx_default; pfx_default="$(printf '%s' "${gh_repo##*/}" | tr -cd 'A-Za-z' | cut -c1-2 | tr '[:lower:]' '[:upper:]')"
  local gh_prefix; gh_prefix="$(ask "Snapshot file prefix (e.g. ${pfx_default:-GH}-0001_...)" "${pfx_default:-GH}")"
  [[ -n "${GITHUB_TOKEN:-}" ]] && ensure_labels "$gh_repo"
  GH_CONN="$conn" GH_PREFIX="$gh_prefix" attach_project_github "$PROJECT_DIR" "$gh_repo" "$gh_proj" "" "link"
  check_github_drift
  verify_bind "$PROJECT_DIR" || true
  step "Done"
  note "Refresh any time by running this installer inside the project, no flags."
  note "Teammates: clone, then run this once on their machine:"
  cmd "./install.sh --github"
}

# The tracker-less bind, shared by the wizard and by a plain refresh of an
# already-bound project. A refresh reaches project_mode, which knows how to
# resolve a YouTrack or GitHub connection and - before this existed - went
# looking for one on a project that has neither.
attach_project_none() {  # $1 dir, $2 mode (link|copy)
  local dir="$1"
  local mode="${2:-link}"
  copy_skills "$dir" "$mode"
  # legacy pointers from before story-tools.json existed. The pointer itself
  # is merged, not replaced: it also carries snapshot, updates and roles, and
  # a refresh must not throw those away.
  rm -f "$dir/.agents/youtrack.json" "$dir/.agents/config/youtrack.json"
  merge_json "$dir/.agents/config/story-tools.json" "tracker" '{"type":"none"}'
  ok "pointer: tracker type 'none' - skills run tracker-less (offline mode)"
  # the doc has always had a tracker-less variant; it was simply never called
  write_workflow_doc "$dir" none ""
  write_updates_config "$dir"
  write_topical_tags "$dir" none
  write_pages_config "$dir"
  set_snapshot_mode "$dir"
  migrate_docs_layout "$dir"
  migrate_kb_dirs "$dir"
  ship_setup "$dir"
  verify_bind "$dir" || true
}

none_wizard() {
  step "1/1 Project (skills only - no tracker)"
  pick_project
  [[ -z "$PROJECT_DIR" ]] && { say "  no project chosen - nothing to do"; return; }
  attach_project_none "$PROJECT_DIR" "link"
  say "  Re-run this wizard when the project adopts YouTrack or GitHub."
}

wizard() {
  local ttype cur_type
  cur_type="$(read_pointer "$PWD" type)"
  ttype="$(ask "Which tracker does this project use? (youtrack / github / none)" "${cur_type:-youtrack}")"
  case "$ttype" in
    github|gh) github_wizard; return;;
    none|no|skills) none_wizard; return;;
  esac
  say "story-tools setup - credentials, server, agents, project. Re-run any time:"
  say "current values are shown in [brackets]; Enter keeps them. No secrets ever"
  say "land in a repo."
  step "1/4 Project"
  pick_project
  local pre_profile=""
  if [[ -n "$PROJECT_DIR" ]]; then
    pre_profile="$(read_pointer "$PROJECT_DIR" connection)"
    [[ -z "$pre_profile" ]] && pre_profile="$(read_pointer "$PROJECT_DIR" profile)"
    PRE_URL="$(read_pointer "$PROJECT_DIR" url)"
  fi
  step "2/4 YouTrack connection (server + your token)"; setup_connection "$pre_profile"
  step "3/4 Server setup";           setup_server
  step "4/4 Agent registration";     register_agents
  [[ -n "$PROJECT_DIR" ]] && { step "Attach to $PROJECT_NAME"; bind_project_interactive; }
  step "Done - next steps"
  if [[ -n "$PROJECT_DIR" ]]; then
    say "  1. Commit the setup so teammates inherit it:"
    say "       cd $PROJECT_DIR && git add .agents .claude .github && git commit -m 'story-tools'"
    say "  2. Restart your agentic coding environment (Claude Code, Gemini CLI,"
    say "     VS Code/Copilot, Codex ...) so it loads the '$MCP_SERVER' MCP server"
    say "     and the project skills."
    say "  3. The skills, in the order most existing projects use them:"
    say ""
    say "     project-docs     \"where should this doc go\" / \"sync the docs\""
    say "                      (two-way sync with the tracker knowledge base)"
    say "     story-reconcile  \"reconcile the gaps with the tracker\""
    say "                      (migrates legacy GAP/AC files into stories, in approved batches)"
    say "     story-workflow   \"work on ${BOUND_KEY:-ABC}-123\" / \"what am I working on?\""
    say "                      \"that's done, check it off\" / \"is this story done?\""
    say ""
    say "     Also installed: to-adr, to-rad, to-prd, to-issues, triage,"
    say "     grill-with-docs, to-wiring, regulatory-compliance, handoff, housekeeping."
  else
    say "  1. Restart your agentic coding environment so it loads the '$MCP_SERVER' MCP server."
    say "  2. Bind a repo when ready: ./scripts/install.sh --project <dir>"
  fi
  say ""
  show
}

check_github_drift() {  # $1 = connection; stale PAT in agent configs
  local conn="${1:-github}" srv
  srv="$(gh_server_name "$conn")"
  [[ -n "${GITHUB_TOKEN:-}" ]] || return 0
  [[ -f "$CONN_DIR/$conn.env" || -f "$CONN_DIR/github.env" ]] || return 0
  local stale="" vsc=""
  if [[ -f "$HOME/.gemini/settings.json" ]] && grep -q "\"$srv\"" "$HOME/.gemini/settings.json" \
     && ! grep -qF "$GITHUB_TOKEN" "$HOME/.gemini/settings.json"; then stale="$stale Gemini"; fi
  if [[ -f "$HOME/.gemini/antigravity/mcp_config.json" ]] && grep -q "\"$srv\"" "$HOME/.gemini/antigravity/mcp_config.json" \
     && ! grep -qF "$GITHUB_TOKEN" "$HOME/.gemini/antigravity/mcp_config.json"; then stale="$stale Antigravity"; fi
  case "$(uname -s)" in
    Darwin) vsc="$HOME/Library/Application Support/Code/User/mcp.json";;
    Linux)  vsc="$HOME/.config/Code/User/mcp.json";;
  esac
  if [[ -n "$vsc" && -f "$vsc" ]] && grep -q "\"$srv\"" "$vsc" \
     && ! grep -qF "$GITHUB_TOKEN" "$vsc"; then stale="$stale VS-Code"; fi
  if [[ -f "$HOME/.claude.json" ]] && grep -q "\"$srv\"" "$HOME/.claude.json" \
     && ! grep -qF "$GITHUB_TOKEN" "$HOME/.claude.json"; then stale="$stale Claude-Code"; fi
  if [[ -n "$stale" ]]; then
    warn "STALE TOKEN in agent MCP registration for '$srv':$stale"
    say  "  Fix: re-run the installer's github setup for connection '$conn', then restart agents."
  fi
}

check_registration_drift() {  # warn when an agent's MCP config holds a different token
  local stale="" vsc=""
  if [[ -f "$HOME/.gemini/settings.json" ]] && grep -q "\"$MCP_SERVER\"" "$HOME/.gemini/settings.json" \
     && ! grep -qF "$YOUTRACK_TOKEN" "$HOME/.gemini/settings.json"; then stale="$stale Gemini"; fi
  if [[ -f "$HOME/.gemini/antigravity/mcp_config.json" ]] && grep -q "\"$MCP_SERVER\"" "$HOME/.gemini/antigravity/mcp_config.json" \
     && ! grep -qF "$YOUTRACK_TOKEN" "$HOME/.gemini/antigravity/mcp_config.json"; then stale="$stale Antigravity"; fi
  case "$(uname -s)" in
    Darwin) vsc="$HOME/Library/Application Support/Code/User/mcp.json";;
    Linux)  vsc="$HOME/.config/Code/User/mcp.json";;
  esac
  if [[ -n "$vsc" && -f "$vsc" ]] && grep -q "\"$MCP_SERVER\"" "$vsc" \
     && ! grep -qF "$YOUTRACK_TOKEN" "$vsc"; then stale="$stale VS-Code"; fi
  if [[ -f "$HOME/.claude.json" ]] && grep -q "\"$MCP_SERVER\"" "$HOME/.claude.json" \
     && ! grep -qF "$YOUTRACK_TOKEN" "$HOME/.claude.json"; then stale="$stale Claude-Code"; fi
  if [[ -n "$stale" ]]; then
    warn "STALE TOKEN in agent MCP registration for '$MCP_SERVER':$stale"
    say  "  Those agents act as the OLD token's user (permission errors, wrong audit"
    say  "  trail). Fix: ./install.sh --register --connection $PROFILE  then restart them."
  fi
}

check_skill_updates() {  # $1 dir; honours the project's updates.check setting
  local dir="$1"
  local want; want="$(read_pointer "$dir" check)"
  [[ "$want" == "true" ]] || return 0
  local repo branch
  repo="$(read_pointer "$dir" skillsRepo)"; repo="${repo:-$SKILLS_REPO}"
  branch="$(read_pointer "$dir" skillsBranch)"; branch="${branch:-$SKILLS_BRANCH}"

  local remote; remote="$(curl -fsSL -m 10 \
    "https://raw.githubusercontent.com/$repo/$branch/VERSIONS.json" 2>/dev/null)" || return 0
  [[ -n "$remote" ]] || return 0        # offline, private, or not published: silent

  local behind="" name have want_v
  while read -r name have; do
    [[ -n "$name" ]] || continue
    want_v="$(printf '%s' "$remote" | sed -nE 's/.*"'"$name"'": *"([^"]+)".*/\1/p' | head -1)"
    [[ -n "$want_v" ]] || continue
    if version_gt "$want_v" "$have"; then
      behind="$behind
    $name $have -> $want_v"
    fi
  done < <(read_managed "$dir")

  [[ -n "$behind" ]] || return 0
  say ""
  warn "newer skills published in $repo:"
  printf '%s\n' "$behind"
  [[ -t 0 ]] || { say "  run interactively to update"; return 0; }
  local yn; read -rp "  Update this project's skills from $repo? [y/N] " yn
  [[ "$yn" =~ ^[Yy] ]] || { say "  left as-is."; return 0; }
  update_skills_from_repo "$dir" "$repo" "$branch"
}

update_skills_from_repo() {  # $1 dir, $2 owner/repo, $3 branch
  local dir="$1" repo="$2" branch="$3" tmp
  tmp="$(mktemp -d)" || return 1
  if ! curl -fsSL -m 60 "https://codeload.github.com/$repo/tar.gz/refs/heads/$branch" \
       | tar xz -C "$tmp" 2>/dev/null; then
    warn "could not download $repo ($branch) - skills unchanged"; rm -rf "$tmp"; return 1
  fi
  local src; src="$(find "$tmp" -maxdepth 1 -mindepth 1 -type d | head -1)"
  [[ -d "$src/skills" ]] || { warn "unexpected repo layout - skills unchanged"; rm -rf "$tmp"; return 1; }

  # rebuild the managed set from what the download actually contains,
  # limited to what this project already has managed
  local d n; SKILLS=()
  while IFS= read -r n; do
    for d in "$src"/skills/*/"$n"; do
      [[ -f "$d/SKILL.md" ]] && SKILLS+=("$d")
    done
  done < <(read_managed "$dir" | awk '{print $1}')
  if [[ ${#SKILLS[@]} -eq 0 ]]; then
    warn "nothing to update"; rm -rf "$tmp"; return 0
  fi
  copy_skills "$dir" "link"
  rm -rf "$tmp"
  say ""
  say "  Skills updated from $repo. These are tracked files - review the diff"
  say "  and commit them, or 'git checkout .agents .claude .github' to undo."
}


# The agents' topical-tag list. Seeded, never overwritten - the content
# belongs to them. A missing file degrades cleanly, but then the reuse rule
# lives only in the skill, and the header is where someone about to add a
# tag actually reads it.
write_topical_tags() {  # $1 dir, $2 tracker type (github|youtrack|none)
  local f="$1/.agents/config/topical-tags.md"
  [[ -f "$f" ]] && return 0
  local puller="yt-pull.sh"
  [[ "${2:-}" == "github" ]] && puller="gh-pull.sh"
  mkdir -p "$(dirname "$f")"
  cat > "$f" <<'TTEOF'
# Topical tags

Groupings this project uses to collect related work - the subcomponent or
concern a piece of work is about. One per line; blank lines and `#` lines
are ignored.

**Read the list before adding.** Reuse an existing tag if it fits; a near
duplicate splits a grouping in two and leaves neither half complete.
Nothing here dictates what a tag looks like - name it so it groups the work
usefully for this project.

Adding one is a normal part of working. Append it here, then
`.agents/skills/story-reconcile/scripts/PULLER --dimensions-only --push-tags`
carries it into the tracker so it becomes filterable. Values are never
removed automatically - issues already carry them.

---

TTEOF
  # substituted rather than interpolated: the heredoc stays quoted, so the
  # markdown above can hold backticks and $ without the shell touching it
  sed -i.bak "s/PULLER/$puller/" "$f" 2>/dev/null || true
  rm -f "$f.bak"
  ok "topical-tags.md seeded (the agents' list - theirs to add to)"
}

write_updates_config() {  # $1 dir - ask once, preserve thereafter
  local dir="$1" cur want
  cur="$(read_pointer "$dir" check)"
  if [[ -n "$cur" ]]; then
    want="$cur"                                  # already decided; keep it
  elif [[ -t 0 ]]; then
    local yn; read -rp "  Check for skill updates from $SKILLS_REPO on setup? [Y/n] " yn
    [[ "${yn:-y}" =~ ^[Nn] ]] && want="false" || want="true"
  else
    want="true"
  fi
  merge_json "$dir/.agents/config/story-tools.json" "updates" '{
    "check": '"$want"',
    "skillsRepo": "'"$SKILLS_REPO"'",
    "skillsBranch": "'"$SKILLS_BRANCH"'"
  }'
  [[ "$want" == "true" ]] \
    && ok "update check: on (set updates.check false in the pointer to disable)" \
    || ok "update check: off"
}

ship_setup() {  # copy this installer into the project as .agents/setup.sh
  local dir="$1"
  mkdir -p "$dir/.agents"
  if [[ ! "$0" -ef "$dir/.agents/setup.sh" ]]; then
    cp "$0" "$dir/.agents/setup.sh"
  fi
  chmod +x "$dir/.agents/setup.sh"
  ok ".agents/setup.sh shipped (teammates onboard from the clone - no skills repo needed)"
}

offline_note() {
  say "  Offline it is - the workflow still runs in full: agents keep a"
  say "  worklog at .agents/offline/pending.md and a connected teammate"
  say "  reconciles it into the tracker. Re-run .agents/setup.sh any time"
  say "  to connect."
}

# ---------- step: this developer's role on this project ----------
# Role is a property of the PERSON, not the project: installed skills are
# tracked files, so everyone who clones gets the same set and the repo
# cannot carry it. It lives beside the credentials, user-side, and it is a
# hint that shapes what an agent offers - never a permission. The tracker
# enforces; this only stops someone being pushed into work that is not
# theirs to do. See docs/rad/0003-more-than-one-person.md.
DEV_FILE="$AGENTS_HOME/story-tools/developer.json"
ROLE_ARG="${ROLE_ARG:-}"
SNAPSHOT_ARG="${SNAPSHOT_ARG:-}"

project_ident() {  # $1 dir -> stable key for this project (tracker identity)
  local dir="$1" k
  k="$(read_pointer "$dir" repo)"; [[ -n "$k" ]] && { printf '%s' "$k"; return; }
  k="$(read_pointer "$dir" project)"; [[ -n "$k" ]] && { printf '%s' "$k"; return; }
  basename "$dir"
}

read_role() {  # $1 dir -> role or empty
  local key; key="$(project_ident "$1")"
  [[ -f "$DEV_FILE" ]] || return 0
  KEY="$key" F="$DEV_FILE" python3 - <<'RPY' 2>/dev/null || true
import json, os
try:
    d = json.load(open(os.environ["F"]))
except Exception:
    raise SystemExit
e = d.get("projects", {}).get(os.environ["KEY"], {}) or {}
r = e.get("roles") or ([e["role"]] if e.get("role") else [])
print(" ".join(r))
RPY
}

ask_roles() {  # $1 dir - what does this person DO here? asked once, remembered
  local dir="$1" key cur pick roles
  key="$(project_ident "$dir")"; cur="$(read_role "$dir")"
  pick="${ROLE_ARG:-}"
  if [[ -z "$pick" ]]; then
    if [[ -n "$cur" ]]; then
      # already answered - keep it, this is not a question worth re-asking
      ok "roles: $cur on $key"
      return 0
    elif [[ -t 0 ]]; then
      blank
      printf '  %sWhat do you do on this project?%s\n' "$C_B" "$C_0"
      note "Agents use this to decide what to offer you."
      note "It grants and removes no permissions."
      blank
      choice d "developer"    "implement features"
      choice l "team lead"    "manage the work: triage, priorities,"
      choice_cont             "deciding a story is ready"
      choice a "architect"    "make the technical calls: architecture,"
      choice_cont             "ADRs, research (senior dev / architect)"
      choice p "product"      "decide what the product does: PRDs"
      blank
      pick="$(ask "Letters, any order - Enter for all of them (working solo)" "dlap")"
      blank
    else
      return 0
    fi
  fi
  # accept letters or names, in any order
  roles=""
  case "$pick" in
    all|ALL) pick="dlap";;
  esac
  [[ "$pick" == *[dD]* || "$pick" == *developer* ]] && roles="$roles developer"
  [[ "$pick" == *[lL]* || "$pick" == *lead* ]]      && roles="$roles lead"
  [[ "$pick" == *[aA]* || "$pick" == *architect* ]] && roles="$roles architect"
  [[ "$pick" == *[pP]* || "$pick" == *product* ]]   && roles="$roles product"
  roles="${roles# }"
  if [[ -z "$roles" ]]; then
    warn "no roles recognised in '$pick' - use any of d, l, a, p (or Enter for all)"
    return 0
  fi
  local json; json="$(printf '%s' "$roles" | tr ' ' '\n' | sed 's/.*/"&"/' | paste -sd, -)"
  merge_json "$DEV_FILE" "projects.$key" '{"roles": ['"$json"']}'
  ok "roles: $roles on $key (~/.agents/story-tools/developer.json)"
}

developer_setup() {  # shipped-copy flow: credential + agent registration only
  local dir; dir="$(cd "$(dirname "$0")/.." && pwd)"
  local ptype conn
  ptype="$(read_pointer "$dir" type)"; conn="$(read_pointer "$dir" connection)"
  step "story-tools developer setup: $(basename "$dir")"
  ask_roles "$dir"
  say "  Skills and workflow docs already travel with this repo. This sets up"
  say "  YOUR tracker credential and registers it in YOUR agents - secrets"
  say "  live in ~/.agents/story-tools/, never in the repo."
  local go="y"
  case "$ptype" in
    github)
      local repo; repo="$(read_pointer "$dir" repo)"
      say "  Tracker: GitHub (${repo:-unknown repo})"
      [[ -t 0 ]] || { say "  run interactively to connect"; return 0; }
      read -rp "  Connect to the tracker now? (n = work offline, reconcile later) [Y/n] " go
      [[ "${go:-y}" =~ ^[Nn] ]] && { offline_note; return 0; }
      setup_github "${conn:-github}"
      check_github_drift "${conn:-github}"
      check_skill_updates "$dir"
      ok "done - restart your agent, then say: \"what am I working on?\""
      ;;
    youtrack)
      local url; url="$(read_pointer "$dir" url)"
      say "  Tracker: YouTrack (${url:-server URL not in pointer - ask a teammate})"
      [[ -t 0 ]] || { say "  run interactively to connect"; return 0; }
      read -rp "  Connect to the tracker now? (n = work offline, reconcile later) [Y/n] " go
      [[ "${go:-y}" =~ ^[Nn] ]] && { offline_note; return 0; }
      PRE_URL="$url" setup_connection "$conn"
      register_agents
      check_registration_drift
      check_skill_updates "$dir"
      ok "done - restart your agent, then say: \"what am I working on?\""
      ;;
    none|"")
      say "  Tracker: none - nothing to connect. Agents work tracker-less"
      say "  (offline pending log) out of the box."
      check_skill_updates "$dir"
      ;;
  esac
}

user_mode() {
  local profile="${1:-}"
  if [[ -n "$profile" ]] && load_connection "$profile"; then
    PROFILE="$profile"
  else
    setup_connection "$profile"
  fi
  register_agents
}

project_mode() {
  local dir="" profile="" mode="link" yt_project="" readonly_flag="" gh_repo="" gh_proj=""
  dir="$(cd "$1" && pwd)"; shift
  while [[ $# -gt 0 ]]; do
    case "$1" in
      --connection|--profile) profile="$2"; shift 2;;
      --yt-project) yt_project="$2"; shift 2;;
      --github) gh_repo="$2"; shift 2;;
      --gh-project) gh_proj="$2"; shift 2;;
      --readonly) readonly_flag="true"; shift;;
      --snapshot) SNAPSHOT_ARG="$2"; shift 2;;
      --copy) mode="copy"; shift;;
      *) say "unknown option: $1" >&2; exit 1;;
    esac
  done

  local ptype; ptype="$(read_pointer "$dir" type)"

  # Tracker-less project: nothing to connect, so never go looking for a
  # connection. Without this a refresh fell through to the YouTrack path and
  # demanded --profile on a project that has no tracker at all.
  if [[ -z "$gh_repo" && -z "$yt_project" && "$ptype" == "none" ]]; then
    attach_project_none "$dir" "$mode"
    return
  fi

  # GitHub-tracked project? (explicit flag, or the pointer says so)
  if [[ -n "$gh_repo" || "$ptype" == "github" ]]; then
    [[ -z "$gh_repo" ]] && gh_repo="$(read_pointer "$dir" repo)"
    [[ -z "$gh_proj" ]] && gh_proj="$(read_pointer "$dir" project)"
    [[ -z "$gh_repo" ]] && { say "error: no GitHub repo - use --github owner/repo" >&2; exit 1; }
    GH_CONN="$(read_pointer "$dir" connection)"
    [[ -z "$GH_CONN" ]] && GH_CONN="$(basename "$dir" | tr '[:upper:]' '[:lower:]' | tr -c 'a-z0-9-\n' '-')"
    # snapshot file prefix: keep the pointer's; first time, ask (interactive)
    GH_PREFIX="$(read_pointer "$dir" prefix)"
    if [[ -z "$GH_PREFIX" ]]; then
      local pfx_default; pfx_default="$(printf '%s' "${gh_repo##*/}" | tr -cd 'A-Za-z' | cut -c1-2 | tr '[:lower:]' '[:upper:]')"
      if [[ -t 0 ]]; then
        GH_PREFIX="$(ask "Snapshot file prefix (e.g. ${pfx_default:-GH}-0001_...)" "${pfx_default:-GH}")"
      else
        GH_PREFIX="${pfx_default:-GH}"
      fi
    fi
    if load_github "$GH_CONN"; then
      ok "GitHub: authenticated as '$GH_LOGIN'"
      ensure_labels "$gh_repo"
      register_agents_github "$GH_CONN"   # self-heal: cover agents added since setup
    else
      warn "no GitHub credential yet - run ./install.sh --github (skills/pointer installed anyway)"
    fi
    attach_project_github "$dir" "$gh_repo" "$gh_proj" "$readonly_flag" "$mode"
    check_github_drift "$GH_CONN"
    return
  fi

  if [[ -z "$profile" ]]; then
    profile="$(read_pointer "$dir" connection)"
    [[ -z "$profile" ]] && profile="$(read_pointer "$dir" profile)"
    if [[ -z "$profile" ]]; then
      local profiles; profiles="$(list_connections)"
      if [[ "$(echo "$profiles" | grep -c .)" == "1" ]]; then profile="$profiles"
      else say "Pick an instance with --profile <name>. Configured:"; say "${profiles:-  (none - run ./scripts/install.sh first)}"; exit 1; fi
    fi
  fi
  load_connection "$profile" || { say "error: connection '$profile' not found - run ./scripts/install.sh" >&2; exit 1; }
  PROFILE="$profile"
  resolve_server
  # keep the server in step with the workflow on every project refresh:
  # app version check/deploy offer, link type, workflow tags
  setup_server
  # ...and the agent registrations: newly supported agents (or a fresh
  # agent install on this machine) get the server entry on refresh, not
  # only at first setup
  register_agents
  check_registration_drift
  local prev; prev="$(read_pointer "$dir" connection)"; [[ -z "$prev" ]] && prev="$(read_pointer "$dir" profile)"
  if [[ -n "$prev" && "$prev" != "$PROFILE" ]]; then
    warn "rebinding: this project was bound to connection '$prev', now '$PROFILE'"
    [[ -d "$dir/docs/knowledge/.yt-sync" ]] && \
      warn "docs/knowledge sync state still references the OLD server - run yt-sync.sh --dry-run before any push; if the plan looks wrong, re-bootstrap (see the project-docs binding)"
    [[ -d "$dir/docs/stories" ]] && \
      warn "docs/stories snapshot is from the old server - refresh it (yt-pull) once the new server is confirmed"
  fi
  [[ -z "$yt_project" ]] && yt_project="$(read_pointer "$dir" project)"
  [[ -z "$yt_project" ]] && yt_project="${YOUTRACK_PROJECT:-}"
  attach_project "$dir" "$yt_project" "$readonly_flag" "$mode"
}

show() {
  cat <<EOF
Where everything lives:
  Connections:      $CONN_DIR/<name>.env  (one server + your token, chmod 600)
  User skills:      ~/.agents/skills (+ ~/.claude/skills copy)
  Per project:      <repo>/.agents/skills + <repo>/.agents/config/story-tools.json (committed;
                    .claude/ and .github/ symlink into .agents/ - the project carries
                    its own workflow, teammates only ever run this installer once)
  Agent MCP config: each agent's own user config (claude mcp / gemini settings /
                    VS Code mcp.json / copilot mcp-config / codex config.toml)
EOF
}

if [[ $SHIPPED -eq 1 ]]; then
  # project-shipped copy: developer onboarding only
  case "${1:-}" in
    ""|--setup) shift 2>/dev/null || true
      [[ "${1:-}" == "--role" ]] && ROLE_ARG="${2:-}"
      developer_setup;;
    --github) shift
      if [[ -n "${1:-}" && "${1:-}" != -* ]]; then
        say "error: --github takes no repo here - per-developer credential setup only." >&2; exit 1
      fi
      setup_github;;
    --register) shift; profile=""
      [[ "${1:-}" == "--connection" || "${1:-}" == "--profile" ]] && profile="${2:-}"
      [[ -z "$profile" ]] && profile="$(read_pointer "$(cd "$(dirname "$0")/.." && pwd)" connection)"
      [[ -z "$profile" ]] && { say "usage: setup.sh --register [--connection <name>]" >&2; exit 1; }
      if load_connection "$profile"; then PROFILE="$profile"; register_agents
      elif load_github "$profile"; then register_agents_github "$profile"
      else say "error: connection '$profile' not found - run setup.sh first" >&2; exit 1; fi;;
    --list) list_connections;;
    --show) show;;
    --help|-h) sed -n '2,24p' "$0" | sed 's/^# \{0,1\}//';;
    *) say "This is the project-shipped setup (developer onboarding)." >&2
       say "Binding, refreshing, and skill updates run from the story-tools repo's scripts/install.sh." >&2
       exit 1;;
  esac
  exit 0
fi

case "${1:-}" in
  "") if [[ -f "./.agents/config/story-tools.json" ]]; then
        say "Bound project detected here (.agents/config/story-tools.json)."
        refresh="y"
        if [[ -t 0 ]]; then
          read -rp "  Refresh with current settings? [Y/n] " refresh
          refresh="${refresh:-y}"
        fi
        if [[ "$refresh" =~ ^[Nn] ]]; then
          wizard
        else
          project_mode "$PWD"
        fi
      elif [[ -t 0 ]]; then wizard; else sed -n '2,24p' "$0" | sed 's/^# \{0,1\}//'; fi;;
  --setup) wizard;;
  --clean-user) clean_user_mode;;
  --user) shift; profile=""; [[ "${1:-}" == "--connection" || "${1:-}" == "--profile" ]] && profile="$2"; user_mode "$profile";;
  --github) shift
    if [[ -n "${1:-}" && "${1:-}" != -* ]]; then
      say "error: --github at top level takes no repo - per-developer setup only." >&2
      say "  To bind a project: ./install.sh --project <dir> --github owner/repo [--gh-project N]" >&2
      exit 1
    fi
    setup_github;;
  --register) shift; profile=""
    [[ "${1:-}" == "--connection" || "${1:-}" == "--profile" ]] && profile="${2:-}"
    if [[ -z "$profile" && -f "./.agents/config/story-tools.json" ]]; then
      profile="$(read_pointer "$PWD" connection)"
      [[ -z "$profile" ]] && profile="$(read_pointer "$PWD" profile)"
    fi
    if [[ -z "$profile" ]]; then
      profiles="$(list_connections)"
      [[ "$(grep -c . <<<"$profiles")" == "1" ]] && profile="$profiles"
    fi
    [[ -z "$profile" ]] && { say "usage: install.sh --register [--connection <name>]  (several connections exist - name one)" >&2; exit 1; }
    # Tracker-agnostic, matching the shipped .agents/setup.sh copy: a
    # connection is a YouTrack one or a GitHub one, and --register re-pushes
    # whichever it is. This arm was YouTrack-only while the shipped copy
    # already fell back, so the same flag did different things depending on
    # which script you ran - and the GitHub binding documents this flag for
    # rotating a GitHub token.
    if load_connection "$profile"; then PROFILE="$profile"; register_agents
    elif load_github "$profile"; then register_agents_github "$profile"
    else say "error: connection '$profile' not found" >&2; exit 1; fi
    say ""
    say "Registrations updated. Restart your agent sessions so they reconnect"
    say "with the new token.";;
  --project) shift; [[ $# -ge 1 ]] || { say "usage: install.sh --project <dir> [--connection <name>] [--yt-project <KEY>] [--readonly] [--copy] [--snapshot synced|committed]" >&2; exit 1; }; project_mode "$@";;
  --list) list_connections;;
  --show) show;;
  --help|-h|*) sed -n '2,24p' "$0" | sed 's/^# \{0,1\}//';;
esac
