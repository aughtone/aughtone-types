#!/usr/bin/env python3
import os
import shutil
import hashlib
import argparse
import sys
import json
import difflib

def get_file_hash(filepath):
    """Calculate MD5 hash of a file."""
    hasher = hashlib.md5()
    try:
        with open(filepath, 'rb') as f:
            buf = f.read(65536)
            while len(buf) > 0:
                hasher.update(buf)
                buf = f.read(65536)
        return hasher.hexdigest()
    except Exception:
        return None

def compare_directories(dir1, dir2):
    """
    Compare two directories recursively.
    Returns True if they are identical in structure and file content, otherwise False.
    """
    if not os.path.exists(dir1) or not os.path.exists(dir2):
        return False
        
    for root, dirs, files in os.walk(dir1):
        rel_path = os.path.relpath(root, dir1)
        target_dir = os.path.join(dir2, rel_path)
        
        if not os.path.exists(target_dir):
            return False
            
        for file in files:
            if file == '.DS_Store':
                continue
            file1 = os.path.join(root, file)
            file2 = os.path.join(target_dir, file)
            
            if not os.path.exists(file2):
                return False
                
            if get_file_hash(file1) != get_file_hash(file2):
                return False
                
    # Check if dir2 has extra files
    for root, dirs, files in os.walk(dir2):
        rel_path = os.path.relpath(root, dir2)
        source_dir = os.path.join(dir1, rel_path)
        if not os.path.exists(source_dir):
            return False
        for file in files:
            if file == '.DS_Store':
                continue
            if not os.path.exists(os.path.join(source_dir, file)):
                return False
                
    return True

def update_frontmatter(skill_md_path, new_name, extends_skill=None):
    """Updates the frontmatter of SKILL.md with a new name and optional extends field."""
    if not os.path.exists(skill_md_path):
        return False
    try:
        with open(skill_md_path, 'r', encoding='utf-8') as f:
            lines = f.readlines()
        
        in_frontmatter = False
        name_updated = False
        extends_added = False
        
        new_lines = []
        for line in lines:
            stripped = line.strip()
            if stripped == '---':
                if not in_frontmatter:
                    in_frontmatter = True
                    new_lines.append(line)
                else:
                    # Closing frontmatter
                    if extends_skill and not extends_added:
                        new_lines.append(f"extends: {extends_skill}\n")
                        extends_added = True
                    in_frontmatter = False
                    new_lines.append(line)
            elif in_frontmatter and stripped.startswith('name:'):
                new_lines.append(f"name: {new_name}\n")
                name_updated = True
            elif in_frontmatter and stripped.startswith('extends:'):
                if extends_skill:
                    new_lines.append(f"extends: {extends_skill}\n")
                    extends_added = True
                else:
                    new_lines.append(line)
            else:
                new_lines.append(line)
                
        with open(skill_md_path, 'w', encoding='utf-8') as f:
            f.writelines(new_lines)
        return True
    except Exception as e:
        print(f"⚠️ Failed to update frontmatter in {skill_md_path}: {e}", file=sys.stderr)
    return False

def extract_custom_additions(legacy_content, master_content):
    """
    Compare legacy_content to master_content and extract contiguous blocks of
    lines that were added or modified in legacy_content.
    """
    legacy_lines = [line.rstrip() for line in legacy_content.splitlines()]
    master_lines = [line.rstrip() for line in master_content.splitlines()]
    
    matcher = difflib.SequenceMatcher(None, master_lines, legacy_lines)
    additions = []
    
    for tag, i1, i2, j1, j2 in matcher.get_opcodes():
        if tag in ('insert', 'replace'):
            block = []
            for idx in range(j1, j2):
                line = legacy_lines[idx]
                # Skip frontmatter markers or name/extends fields if they appear in diff
                if line.strip() == '---' or line.strip().startswith('name:') or line.strip().startswith('extends:'):
                    continue
                block.append(line)
            if block:
                additions.append('\n'.join(block))
                
    return '\n\n'.join(additions)

def scan_legacy_skills(legacy_dir, master_dir):
    """
    Scan the legacy .skills/ directory and compare against master_dir.
    Returns a dict with categorization of each skill.
    """
    report = {
        "unmodified": [],
        "modified": [],
        "custom": []
    }
    
    if not os.path.exists(legacy_dir):
        return report
        
    for skill_name in sorted(os.listdir(legacy_dir)):
        skill_path = os.path.join(legacy_dir, skill_name)
        if not os.path.isdir(skill_path) or skill_name.startswith('.'):
            continue
            
        master_skill_path = os.path.join(master_dir, skill_name)
        
        if not os.path.exists(master_skill_path):
            report["custom"].append(skill_name)
        else:
            if compare_directories(skill_path, master_skill_path):
                report["unmodified"].append(skill_name)
            else:
                report["modified"].append(skill_name)
                
    return report

def run_migration(project_root, master_dir, backup_dir, decisions=None, dry_run=False, perform_backup=False, perform_migrate=False, rename_custom=True):
    legacy_dir = os.path.join(project_root, ".skills")
    new_skills_dir = os.path.join(project_root, ".agents", "skills")
    
    if not os.path.exists(legacy_dir):
        print("ℹ️ No legacy '.skills/' directory found in the project root.")
        return 0
        
    if decisions is None:
        decisions = {}
        
    print(f"🔍 Scanning legacy skills in: {legacy_dir}")
    print(f"📂 Master skills reference: {master_dir}")
    
    report = scan_legacy_skills(legacy_dir, master_dir)
    
    print("\n📋 Customization Detection Report:")
    print(f"  ✨ Unmodified (Identical to master): {len(report['unmodified'])}")
    for s in report['unmodified']:
        print(f"    - {s}")
        
    print(f"  📝 Modified (Has local changes): {len(report['modified'])}")
    for s in report['modified']:
        decision = decisions.get(s, {})
        action = decision.get("action", "rename" if rename_custom else "keep")
        new_name = decision.get("new_name", f"custom-{s}" if rename_custom else s)
        print(f"    - {s} -> Decision: {action.upper()} (Target name: {new_name})")
        
    print(f"  🚀 Custom (New skill not in master): {len(report['custom'])}")
    for s in report['custom']:
        decision = decisions.get(s, {})
        action = decision.get("action", "rename" if rename_custom else "keep")
        new_name = decision.get("new_name", f"custom-{s}" if rename_custom else s)
        print(f"    - {s} -> Decision: {action.upper()} (Target name: {new_name})")
        
    if dry_run:
        print("\nℹ️ Dry run completed. No changes were made.")
        return 0
        
    # Handle Backup
    if perform_backup:
        print(f"\n📦 Backing up '.skills/' to: {backup_dir} ...")
        if os.path.exists(backup_dir):
            print(f"⚠️ Backup directory already exists. Removing older backup...")
            shutil.rmtree(backup_dir)
        shutil.copytree(legacy_dir, backup_dir)
        print("✅ Backup complete!")
        
    # Handle Migration
    if perform_migrate:
        print(f"\n🚀 Migrating skills to: {new_skills_dir} ...")
        os.makedirs(new_skills_dir, exist_ok=True)
        
        # 1. Migrate Unmodified
        for skill_name in report['unmodified']:
            src = os.path.join(legacy_dir, skill_name)
            dst = os.path.join(new_skills_dir, skill_name)
            if os.path.exists(dst):
                shutil.rmtree(dst)
            shutil.copytree(src, dst)
            print(f"  ➡️  Migrated unmodified: {skill_name}")
            
        # 2. Migrate Modified and Custom based on decisions
        for skill_name in report['modified'] + report['custom']:
            src_dir = os.path.join(legacy_dir, skill_name)
            decision = decisions.get(skill_name, {})
            action = decision.get("action", "rename" if rename_custom else "keep")
            new_name = decision.get("new_name", f"custom-{skill_name}" if rename_custom else skill_name)
            
            dst_dir = os.path.join(new_skills_dir, new_name)
            if os.path.exists(dst_dir):
                shutil.rmtree(dst_dir)
                
            if action == "overwrite":
                # Discard customizations, use master template
                master_src = os.path.join(master_dir, skill_name)
                if os.path.exists(master_src):
                    shutil.copytree(master_src, dst_dir)
                    print(f"  ➡️  Overwrote with fresh master version: {skill_name}")
                else:
                    print(f"  ⚠️  Cannot overwrite {skill_name}, not found in master. Keeping legacy as-is.")
                    shutil.copytree(src_dir, dst_dir)
                    
            elif action == "keep":
                # Copy legacy as-is without name change
                shutil.copytree(src_dir, dst_dir)
                print(f"  ➡️  Kept local legacy version as-is: {skill_name}")
                
            elif action == "rename":
                # Copy legacy and update name in frontmatter
                shutil.copytree(src_dir, dst_dir)
                skill_md_path = os.path.join(dst_dir, "SKILL.md")
                update_frontmatter(skill_md_path, new_name)
                print(f"  ➡️  Migrated and renamed customized skill: {skill_name} -> {new_name}")
                
            elif action == "extend":
                # Merging/Extending:
                # 1. Copy the fresh master version to the new folder
                master_src = os.path.join(master_dir, skill_name)
                if os.path.exists(master_src):
                    shutil.copytree(master_src, dst_dir)
                    
                    # 2. Extract customizations from legacy SKILL.md
                    legacy_skill_md = os.path.join(src_dir, "SKILL.md")
                    master_skill_md = os.path.join(master_src, "SKILL.md")
                    
                    try:
                        with open(legacy_skill_md, 'r', encoding='utf-8') as f:
                            legacy_content = f.read()
                        with open(master_skill_md, 'r', encoding='utf-8') as f:
                            master_content = f.read()
                            
                        customizations = extract_custom_additions(legacy_content, master_content)
                        
                        # 3. Update frontmatter with new name and extends:
                        dst_skill_md = os.path.join(dst_dir, "SKILL.md")
                        update_frontmatter(dst_skill_md, new_name, extends_skill=skill_name)
                        
                        # 4. Append customizations under ## Local Extensions
                        if customizations.strip():
                            with open(dst_skill_md, 'a', encoding='utf-8') as f:
                                f.write(f"\n\n## Local Extensions\n\n{customizations}\n")
                            print(f"  ➡️  Extended existing skill: {skill_name} -> {new_name} (with local customizations merged)")
                        else:
                            print(f"  ➡️  Extended existing skill: {skill_name} -> {new_name} (no local edits detected in SKILL.md)")
                    except Exception as e:
                        print(f"  ⚠️  Failed to merge extensions for {skill_name}: {e}. Falling back to rename.")
                        shutil.rmtree(dst_dir)
                        shutil.copytree(src_dir, dst_dir)
                        update_frontmatter(os.path.join(dst_dir, "SKILL.md"), new_name)
                else:
                    # If it's not in master, we can't extend it. Just copy as custom.
                    shutil.copytree(src_dir, dst_dir)
                    update_frontmatter(os.path.join(dst_dir, "SKILL.md"), new_name)
                    print(f"  ➡️  Migrated as custom skill (cannot extend, missing in master): {skill_name} -> {new_name}")
                    
            # Also copy any extra files from legacy folder that don't exist in destination folder
            for root, dirs, files in os.walk(src_dir):
                rel_path = os.path.relpath(root, src_dir)
                dest_sub_dir = os.path.join(dst_dir, rel_path)
                os.makedirs(dest_sub_dir, exist_ok=True)
                for file in files:
                    if file == '.DS_Store' or file == 'SKILL.md':
                        continue
                    src_file = os.path.join(root, file)
                    dst_file = os.path.join(dest_sub_dir, file)
                    if not os.path.exists(dst_file):
                        shutil.copy2(src_file, dst_file)
                        
        # 3. Remove old .skills directory
        print("🧹 Cleaning up old '.skills/' directory...")
        shutil.rmtree(legacy_dir)
        print("✅ Migration complete!")
        
    return 0

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Migrate legacy .skills/ to .agents/skills/ with backup and customization detection.")
    parser.add_argument("--project-root", default=".", help="Project root directory containing .skills/")
    parser.add_argument("--master-dir", required=True, help="Directory containing master templates (src/skills/)")
    parser.add_argument("--backup-dir", default=None, help="Directory to back up .skills/ to")
    parser.add_argument("--dry-run", action="store_true", help="Print report of customizations without executing backup or migration")
    parser.add_argument("--backup", action="store_true", help="Perform backup to skills-backup/")
    parser.add_argument("--migrate", action="store_true", help="Migrate files to .agents/skills/ and remove old .skills/")
    parser.add_argument("--no-rename", action="store_true", help="Do not rename customized skills to custom-[name]")
    parser.add_argument("--decisions", default=None, help="JSON string representing explicit decisions per customized skill")
    
    args = parser.parse_args()
    
    # Resolve absolute paths
    project_root = os.path.abspath(args.project_root)
    master_dir = os.path.abspath(args.master_dir)
    
    if args.backup_dir:
        backup_dir = os.path.abspath(args.backup_dir)
    else:
        backup_dir = os.path.join(project_root, "skills-backup")
        
    decisions_dict = None
    if args.decisions:
        try:
            decisions_dict = json.loads(args.decisions)
        except Exception as e:
            print(f"❌ Error parsing --decisions JSON: {e}", file=sys.stderr)
            sys.exit(1)
            
    sys.exit(run_migration(
        project_root=project_root,
        master_dir=master_dir,
        backup_dir=backup_dir,
        decisions=decisions_dict,
        dry_run=args.dry_run,
        perform_backup=args.backup,
        perform_migrate=args.migrate,
        rename_custom=not args.no_rename
    ))
