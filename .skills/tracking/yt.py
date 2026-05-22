#!/usr/bin/env python3
import os
import sys
import argparse
import json
import re
from pathlib import Path
from youtrack_client import YouTrackClient

def load_config_and_secrets():
    config_path = Path(".config/project.json")
    if not config_path.exists():
        print("Error: .config/project.json not found in the project root.")
        exit(1)
        
    with open(config_path, "r") as f:
        config = json.load(f)
        
    project_id = config.get("project_id")
    if not project_id:
        print("Error: project_id not found in .config/project.json.")
        exit(1)
        
    # Load secrets
    secret_path = Path.home() / ".secrets" / "agents" / project_id / "youtrack.env"
    if secret_path.exists():
        with open(secret_path, "r") as f:
            for line in f:
                line = line.strip()
                if line and not line.startswith('#') and '=' in line:
                    key, value = line.split('=', 1)
                    os.environ[key.strip()] = value.strip().strip('"').strip("'")
    
    # Optional youtrack.json override
    yt_json_path = Path("youtrack.json")
    if yt_json_path.exists():
        with open(yt_json_path, "r") as f:
            yt_config = json.load(f)
            config.update(yt_config)
            
    config.setdefault("url", os.environ.get("YOUTRACK_URL"))
    config.setdefault("token", os.environ.get("YOUTRACK_TOKEN"))
    
    if project_id:
        specific_token_key = f"{project_id}_YOUTRACK_TOKEN"
        config["token"] = os.environ.get(specific_token_key) or config["token"]
        
    config.setdefault("epic_project", project_id)
    config.setdefault("client_project", project_id)
    config.setdefault("server_project", project_id)
    config.setdefault("kanban_projects", [project_id])
    config.setdefault("temp_id_prefix", ["STORY", "3"])
    config.setdefault("tech_debt_title", "Technical Debt")
    config.setdefault("transition_epics", False)
    
    # Ensure list types
    for list_key in ["kanban_projects", "temp_id_prefix"]:
        if list_key in config and isinstance(config[list_key], str):
            config[list_key] = config[list_key].split(",")
            
    return config

def get_target_state(status_prefix, project_id, config):
    mapping = {
        "TODO": "Open",
        "WIP": "In Progress",
        "DONE": "Done",
        "OBSOLETE": "Canceled"
    }
    state = mapping.get(status_prefix, "Open")
    if project_id in config.get("kanban_projects", []):
        kanban_mapping = {
            "Open": "Backlog",
            "In Progress": "Develop",
            "In Review": "Review",
            "Done": "Done",
            "Canceled": "Done"
        }
        return kanban_mapping.get(state, "Backlog")
    return state

def parse_duration(duration_str):
    minutes = 0
    hours_match = re.search(r"(\d+)h", duration_str)
    mins_match = re.search(r"(\d+)m", duration_str)
    if hours_match:
        minutes += int(hours_match.group(1)) * 60
    if mins_match:
        minutes += int(mins_match.group(1))
    if not hours_match and not mins_match and duration_str.isdigit():
        minutes = int(duration_str)
    return minutes

def update_id_in_index_files(old_id, new_id, file_basename, is_dry_run):
    index_files = list(Path("docs").rglob("README.md")) + list(Path("docs/prd").rglob("*.md")) + list(Path("docs/ac").rglob("*.md"))
    for idx_file in index_files:
        if not idx_file.exists(): continue
        try:
            with open(idx_file, "r") as f:
                content = f.read()
            if file_basename in content and old_id in content:
                lines = content.splitlines()
                modified = False
                for i, line in enumerate(lines):
                    if file_basename in line and old_id in line:
                        lines[i] = line.replace(old_id, new_id)
                        modified = True
                if modified:
                    if not is_dry_run:
                        with open(idx_file, "w") as f:
                            f.write("\n".join(lines) + "\n")
                        print(f"  Updated index {idx_file} -> {new_id}")
                    else:
                        print(f"  [DRY RUN] Would update index {idx_file} -> {new_id}")
        except Exception as e:
            pass

def sync_file(filepath, client, config, is_dry_run, local_ids):
    with open(filepath, "r") as f:
        content = f.read()
        
    id_match = re.search(r"\*\*ID:\*\*\s+\[(.*?)\]", content)
    if not id_match:
        return
        
    issue_id = id_match.group(1).strip()
    status_match = re.search(r"\*\*Status:\*\*\s+(.*)", content)
    type_match = re.search(r"\*\*Type:\*\*\s+(.*)", content)
    parent_match = re.search(r"\*\*Parent:\*\*\s+\[(.*?)\]", content)
    
    status = status_match.group(1).strip() if status_match else "TODO"
    issue_type = type_match.group(1).strip() if type_match else "Story"
    parent_id = parent_match.group(1).strip() if parent_match else None
    
    title_match = re.search(r"^#\s+(.*)", content, re.MULTILINE)
    title = title_match.group(1).strip() if title_match else filepath.stem
    
    abs_path = str(filepath.absolute())
    if "evolyn-server" in abs_path:
        target_project = config["server_project"]
    elif "evolyn-client" in abs_path:
        target_project = config["client_project"]
    else:
        target_project = config["server_project"]
        
    if issue_type.lower() == "epic":
        target_project = config["epic_project"]
        
    target_state = get_target_state(status, target_project, config)
    status_field = "Stage" if target_project in config.get("kanban_projects", []) else "State"
    
    real_id = issue_id
    internal_id = None
    
    if issue_id == "#NEW":
        if not is_dry_run:
            print(f"Creating new {issue_type}: {title}")
            custom_fields = {status_field: {"name": target_state}}
            if issue_type.lower() == "epic":
                custom_fields["Subsystem"] = {"name": "Epic"}
                
            res = client.create_issue(target_project, f"[#NEW] {title}", f"Source: {filepath}\n\n{content}", issue_type=issue_type, custom_fields=custom_fields)
            internal_id = res.get('id')
            real_id = res.get('idReadable', internal_id)
            
            client.update_issue(internal_id, summary=f"[{real_id}] {title}")
            
            new_content = content.replace("**ID:** [#NEW]", f"**ID:** [{real_id}]")
            with open(filepath, "w") as f:
                f.write(new_content)
            print(f"  Updated {filepath} with [{real_id}]")
            update_id_in_index_files("[#NEW]", f"[{real_id}]", filepath.name, is_dry_run)
    elif issue_id.isdigit() or "-" in issue_id:
        if not is_dry_run:
            query = f'project: {target_project} "{issue_id}"'
            existing = client.search_issues(query)
            if existing:
                internal_id = existing[0]['id']
                client.update_issue(internal_id, summary=f"[{issue_id}] {title}", description=f"Source: {filepath}\n\n{content}")
                client.set_issue_field(internal_id, status_field, {"name": target_state})
                print(f"Updated {issue_type} {issue_id} -> {target_state}")
                
    if real_id != "#NEW":
        local_ids.add(real_id)
        
    if parent_id and parent_id != "#NEW" and internal_id and not is_dry_run:
        parent_query = f'"{parent_id}"'
        parent_res = client.search_issues(parent_query)
        if parent_res:
            try:
                client.link_issues(parent_res[0]['id'], internal_id)
            except:
                pass

def orphan_detection(client, config, local_ids):
    print("Checking for orphaned stories and epics in YouTrack...")
    tech_debt_epic_id = None
    try:
        tech_debt_query = f'project: {config["epic_project"]} "{config["tech_debt_title"]}"'
        existing_tech_debt = client.search_issues(tech_debt_query)
        if existing_tech_debt:
            tech_debt_epic_id = existing_tech_debt[0]['id']
        else:
            res = client.create_issue(config["epic_project"], config["tech_debt_title"], "Repository for orphaned or unmapped stories.", issue_type="Epic")
            tech_debt_epic_id = res['id']
    except Exception:
        pass

    projects_to_check = set([config["server_project"], config["client_project"], config["epic_project"]])
    
    for project_id in projects_to_check:
        if not project_id: continue
        query = f"project: {project_id} #{{Unresolved}}"
        
        all_yt_issues = client.search_issues(query)
        for issue in all_yt_issues:
            rid = issue.get('idReadable')
            if rid and rid not in local_ids:
                if project_id == config["epic_project"] and not config.get("transition_epics", False):
                    continue
                summary = issue.get("summary", "")
                if "[" in summary and "]" in summary:
                    print(f"  Orphan detected: {rid}")
                    if project_id != config["epic_project"] and tech_debt_epic_id:
                        try:
                            client.link_issues(tech_debt_epic_id, issue['id'])
                        except: pass

                    success = False
                    for state in ["Obsolete", "Canceled", "Orphaned", "Duplicate"]:
                        try:
                            client.set_issue_field(issue['id'], "State", {"name": state})
                            print(f"    Marked {rid} as {state}.")
                            success = True
                            break
                        except: pass

def main():
    parser = argparse.ArgumentParser(description="Evolyn YouTrack Toolset")
    subparsers = parser.add_subparsers(dest="command")

    subparsers.add_parser("list-projects", help="List available projects")
    
    fields_parser = subparsers.add_parser("fields")
    fields_parser.add_argument("project_id")

    work_parser = subparsers.add_parser("work")
    work_parser.add_argument("issue_id")
    work_parser.add_argument("duration")
    work_parser.add_argument("--comment", default="")
    
    sync_parser = subparsers.add_parser("sync", help="Sync stories from Markdown")
    
    args = parser.parse_args()
    try:
        config = load_config_and_secrets()
    except SystemExit:
        return
        
    API_ENABLED = bool(config.get("url") and config.get("token"))
    if not API_ENABLED:
        print("Note: YOUTRACK_URL or YOUTRACK_TOKEN not configured.")
        return

    client = YouTrackClient(config["url"], config["token"])
    
    if args.command == "list-projects":
        print(json.dumps(client.get_projects(), indent=2))
        
    elif args.command == "fields":
        projects = client.get_projects()
        p_id = next((p['id'] for p in projects if p['shortName'] == args.project_id or p['id'] == args.project_id), None)
        if p_id:
            fields = client._get(f"admin/projects/{p_id}/customFields", params={"fields": "id,field(id,name),bundle(id,name,values(id,name))"})
            for f in fields:
                field_info = f.get('field', {})
                bundle_info = f.get('bundle', {})
                print(f"Field: {field_info.get('name')} (ID: {f.get('id')})")
                if bundle_info and 'values' in bundle_info:
                    print(f"  Allowed Values: {', '.join([v.get('name') for v in bundle_info['values']])}")

    elif args.command == "work":
        res = client.search_issues(f'"{args.issue_id}"')
        internal_id = res[0]['id'] if res else args.issue_id
        minutes = parse_duration(args.duration)
        if minutes > 0:
            client.add_work_item(internal_id, minutes, comment=args.comment)
            print(f"Logged {args.duration} to {args.issue_id}")

    elif args.command == "sync":
        is_dry_run = os.getenv("DRY_RUN", "false").lower() == "true"
        if is_dry_run:
            print("--- RUNNING IN DRY RUN MODE ---")
            
        local_ids = set()
        search_paths = [Path("docs/prd"), Path("docs/ac"), Path("docs/gap")]
        for path in search_paths:
            if not path.exists():
                continue
            for filepath in path.rglob("*.md"):
                if filepath.name == "README.md":
                    continue
                sync_file(filepath, client, config, is_dry_run, local_ids)
                
        if not is_dry_run:
            orphan_detection(client, config, local_ids)

if __name__ == "__main__":
    main()
