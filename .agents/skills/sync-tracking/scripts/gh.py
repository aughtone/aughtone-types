#!/usr/bin/env python3
import os
import re
import requests
import json
import sys
from pathlib import Path

def load_config_and_secrets():
    config_path = Path(".agents/config/project.json")
    if not config_path.exists():
        config_path = Path(".config/project.json")
    if not config_path.exists():
        print("Error: project.json not found in .agents/config/ or .config/.")
        exit(1)
        
    with open(config_path, "r") as f:
        config = json.load(f)
        
    project_id = config.get("project_id")
    if not project_id:
        print("Error: project_id not found in .config/project.json.")
        exit(1)
        
    # Load secrets
    secret_path = Path.home() / ".secrets" / "agents" / project_id / "github.env"
    if secret_path.exists():
        with open(secret_path, "r") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    os.environ[key.strip()] = value.strip().strip('"').strip("'")
    
    return config

try:
    config = load_config_and_secrets()
except SystemExit:
    config = {}

GITHUB_TOKEN = os.getenv("GITHUB_TOKEN")
REPO = os.getenv("GITHUB_REPOSITORY")
PROJECT_ID = config.get("project_id")
GITHUB_PROJECT_URL = config.get("github_project")

if not REPO and "github_repo" in config:
    # Extract "owner/repo" from "https://github.com/owner/repo"
    repo_url = config["github_repo"].rstrip("/")
    REPO = "/".join(repo_url.split("/")[-2:])

if PROJECT_ID:
    specific_token_key = f"{PROJECT_ID}_GITHUB_TOKEN"
    GITHUB_TOKEN = os.getenv(specific_token_key) or GITHUB_TOKEN

SYNC_ENABLED = bool(GITHUB_TOKEN and REPO)
DRY_RUN = os.getenv("DRY_RUN", "false").lower() == "true"

def graphql_query(query, variables=None):
    """Executes a GitHub GraphQL query."""
    if not GITHUB_TOKEN:
        return None
    url = "https://api.github.com/graphql"
    headers = {"Authorization": f"Bearer {GITHUB_TOKEN}"}
    response = requests.post(url, headers=headers, json={"query": query, "variables": variables})
    if response.status_code == 200:
        return response.json()
    else:
        print(f"GraphQL Error: {response.status_code} - {response.text}")
        return None

def get_project_metadata(url):
    """Retrieves Node ID and Status field metadata (options) for a Project."""
    org_match = re.search(r"orgs/([^/]+)/projects/(\d+)", url)
    user_match = re.search(r"users/([^/]+)/projects/(\d+)", url)
    
    if org_match:
        owner, num = org_match.groups()
        owner_type = "organization"
    elif user_match:
        owner, num = user_match.groups()
        owner_type = "user"
    else:
        return None

    query = f"""
    query($owner: String!, $num: Int!) {{
      {owner_type}(login: $owner) {{
        projectV2(number: $num) {{
          id
          fields(first: 20) {{
            nodes {{
              ... on ProjectV2SingleSelectField {{
                id
                name
                options {{
                  id
                  name
                }}
              }}
            }}
          }}
        }}
      }}
    }}
    """
    result = graphql_query(query, {"owner": owner, "num": int(num)})
    if result:
        data = result.get("data", {}).get(owner_type, {}).get("projectV2", {})
        return data
    return None

def find_item_id_in_project(project_node_id, content_node_id):
    """Finds the Project Item ID for a specific content (issue) in a project."""
    query = """
    query($project: ID!) {
      node(id: $project) {
        ... on ProjectV2 {
          items(first: 100) {
            nodes {
              id
              content {
                ... on Issue {
                  id
                }
              }
            }
          }
        }
      }
    }
    """
    result = graphql_query(query, {"project": project_node_id})
    if result:
        items = result.get("data", {}).get("node", {}).get("items", {}).get("nodes", [])
        for item in items:
            content = item.get("content")
            if content and content.get("id") == content_node_id:
                return item["id"]
    return None

def move_project_item(project_node_id, item_node_id, status_name, metadata):
    """Moves a project item to a specific status column."""
    if DRY_RUN:
        print(f"[DRY RUN] Would move item {item_node_id} to status: {status_name}")
        return True

    # Find the Status field and the specific option ID
    status_field = next((f for f in metadata.get("fields", {}).get("nodes", []) if f.get("name") == "Status"), None)
    if not status_field:
        print("Error: 'Status' field not found in project.")
        return False
    
    option = next((o for o in status_field.get("options", []) if o.get("name").lower() == status_name.lower()), None)
    if not option:
        # If exact match fails, try partial or notify
        print(f"Error: Status option '{status_name}' not found. Available: {[o['name'] for o in status_field['options']]}")
        return False

    query = """
    mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
      updateProjectV2ItemFieldValue(input: {
        projectId: $project,
        itemId: $item,
        fieldId: $field,
        value: { singleSelectOptionId: $option }
      }) {
        projectV2Item { id }
      }
    }
    """
    variables = {
        "project": project_node_id,
        "item": item_node_id,
        "field": status_field["id"],
        "option": option["id"]
    }
    result = graphql_query(query, variables)
    return result is not None

def add_issue_to_project(issue_node_id, project_node_id):
    """Adds an issue to a GitHub Project (V2) and returns the Item ID."""
    if DRY_RUN:
        print(f"[DRY RUN] Would add issue {issue_node_id} to project {project_node_id}")
        return "MOCK_ITEM_ID"

    query = """
    mutation($project: ID!, $item: ID!) {
      addProjectV2ItemById(input: {projectId: $project, contentId: $item}) {
        item {
          id
        }
      }
    }
    """
    variables = {"project": project_node_id, "item": issue_node_id}
    result = graphql_query(query, variables)
    if result:
        return result.get("data", {}).get("addProjectV2ItemById", {}).get("item", {}).get("id")
    return None

def update_github_issue(issue_number, state=None, labels=None):
    """Updates an existing GitHub issue."""
    if DRY_RUN:
        print(f"[DRY RUN] Would update issue #{issue_number} with state={state}, labels={labels}")
        return True

    if not GITHUB_TOKEN or not REPO:
        return False

    url = f"https://api.github.com/repos/{REPO}/issues/{issue_number}"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    payload = {}
    if state:
        payload["state"] = state
    if labels:
        payload["labels"] = labels
    
    try:
        response = requests.patch(url, headers=headers, json=payload)
        return response.status_code == 200
    except Exception as e:
        print(f"Error updating issue #{issue_number}: {e}")
        return False

def get_issue_node_id(issue_number):
    """Retrieves the Node ID for a specific issue number."""
    owner, repo_name = REPO.split("/")
    query = """
    query($owner: String!, $repo: String!, $num: Int!) {
      repository(owner: $owner, name: $repo) {
        issue(number: $num) {
          id
        }
      }
    }
    """
    result = graphql_query(query, {"owner": owner, "repo": repo_name, "num": int(issue_number)})
    if result:
        return result.get("data", {}).get("repository", {}).get("issue", {}).get("id")
    return None

def create_github_issue(title, body, labels=None):
    """Creates a GitHub issue and returns (issue_number, node_id)."""
    if DRY_RUN:
        print(f"[DRY RUN] Would create issue: {title}")
        return ("MOCK_ID", "MOCK_NODE_ID")

    if not GITHUB_TOKEN or not REPO:
        return (None, None)

    url = f"https://api.github.com/repos/{REPO}/issues"
    headers = {
        "Authorization": f"Bearer {GITHUB_TOKEN}",
        "Accept": "application/vnd.github.v3+json"
    }
    payload = {
        "title": title,
        "body": body,
        "labels": labels or ["documentation-sync"]
    }
    
    try:
        response = requests.post(url, headers=headers, json=payload)
        if response.status_code == 201:
            data = response.json()
            return (data.get("number"), data.get("node_id"))
        else:
            print(f"Failed to create issue: {response.status_code} - {response.text}")
            return (None, None)
    except Exception as e:
        print(f"Error connecting to GitHub: {e}")
        return (None, None)

def sync_requirement_file(filepath, project_metadata=None):
    """Parses a PRD, AC, or GAP markdown file and syncs it with GitHub."""
    with open(filepath, "r") as f:
        content = f.read()
    
    fm_match = re.match(r"^---\s*\n(.*?)\n---\s*\n", content, re.DOTALL)
    if not fm_match:
        return
        
    fm_text = fm_match.group(1)
    fm = {}
    for line in fm_text.splitlines():
        if ":" in line:
            k, v = line.split(":", 1)
            fm[k.strip().lower()] = v.strip().strip('"').strip("'")
            
    issue_id = fm.get("id")
    if not issue_id:
        return
        
    status = fm.get("status", "TODO")
    
    title_match = re.search(r"^#\s+(.*)", content, re.MULTILINE)
    title = title_match.group(1).strip() if title_match else filepath.stem
    
    status_val = status.strip().upper() if status else "TODO"
    gh_state = "closed" if status_val in ["DONE", "COMPLETED", "CLOSED", "OBSOLETE"] else "open"
    
    # Map PRD/GAP status to Project Column names
    status_map = {
        "TODO": "Backlog",
        "WIP": "In progress",
        "IN_PROGRESS": "In progress",
        "IN-PROGRESS": "In progress",
        "DEVELOP": "In progress",
        "REVIEW": "In progress",
        "IN_REVIEW": "In progress",
        "TEST": "In progress",
        "STAGING": "In progress",
        "DONE": "Done",
        "COMPLETED": "Done",
        "CLOSED": "Done",
        "OBSOLETE": "Done",
        "DEFERRED": "Backlog"
    }
    target_column = status_map.get(status_val, "Backlog")

    default_labels = ["documentation-sync"]
    if "gap" in filepath.parts:
        default_labels.append("technical-debt")
    
    project_node_id = project_metadata.get("id") if project_metadata else None

    if issue_id == "#NEW":
        if SYNC_ENABLED or DRY_RUN:
            print(f"Syncing new issue: {title}")
            new_id, node_id = create_github_issue(title, f"Sync source: {filepath}\n\n{content}", labels=default_labels)
            if new_id:
                real_id = f"{new_id}"
                new_content = re.sub(r"^id:\s*['\"]?#NEW['\"]?\b", f"id: {real_id}", content, flags=re.MULTILINE)
                if not DRY_RUN:
                    with open(filepath, "w") as f:
                        f.write(new_content)
                    print(f"  Updated {filepath} with new ID: [{real_id}]")
                
                if node_id and project_node_id:
                    item_id = add_issue_to_project(node_id, project_node_id)
                    if item_id:
                        print(f"  Added issue #{real_id} to project")
                        if target_column != "Todo":
                            move_project_item(project_node_id, item_id, target_column, project_metadata)
    elif issue_id.isdigit():
        if SYNC_ENABLED or DRY_RUN:
            print(f"Updating issue #{issue_id} status to {gh_state} for: {title}")
            update_github_issue(issue_id, state=gh_state)
            
            if project_node_id:
                node_id = get_issue_node_id(issue_id)
                if node_id:
                    item_id = find_item_id_in_project(project_node_id, node_id)
                    if not item_id:
                        item_id = add_issue_to_project(node_id, project_node_id)
                    
                    if item_id:
                        move_project_item(project_node_id, item_id, target_column, project_metadata)

def sync_project_docs():
    """Scans relevant directories for requirement files to sync."""
    project_metadata = None
    if GITHUB_PROJECT_URL and (SYNC_ENABLED or DRY_RUN):
        project_metadata = get_project_metadata(GITHUB_PROJECT_URL)
        if project_metadata:
            print(f"Syncing with GitHub Project: {GITHUB_PROJECT_URL}")

    search_paths = [Path("docs/prd"), Path("docs/ac"), Path("docs/gap")]
    for path in search_paths:
        if not path.exists():
            continue
        for filepath in path.rglob("*.md"):
            if filepath.name == "README.md":
                continue
            sync_requirement_file(filepath, project_metadata)

if __name__ == "__main__":
    import argparse
    import webbrowser

    parser = argparse.ArgumentParser(description="GitHub Synchronization Tool")
    subparsers = parser.add_subparsers(dest="command")
    subparsers.add_parser("sync", help="Sync local requirements to GitHub")
    subparsers.add_parser("open", help="Open the GitHub repository or project board in a browser")
    
    args = parser.parse_args()
    command = args.command or "sync"

    if command == "open":
        url = GITHUB_PROJECT_URL
        if not url:
            if REPO:
                url = f"https://github.com/{REPO}"
            elif config and "github_repo" in config:
                repo_val = config["github_repo"]
                if repo_val.startswith("http"):
                    url = repo_val
                else:
                    url = f"https://github.com/{repo_val}"
        
        if not url:
            print("Error: GitHub Project or Repository not configured.")
            sys.exit(1)
        
        print(f"Opening GitHub URL: {url}")
        webbrowser.open(url)
    elif command == "sync":
        if DRY_RUN:
            print("--- RUNNING IN DRY RUN MODE ---")
        
        if not SYNC_ENABLED and not DRY_RUN:
            print("Note: GitHub Token or Repository not configured. Issue synchronization is disabled.")
        
        if config:
            sync_project_docs()
