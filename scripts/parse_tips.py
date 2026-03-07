import os
import json
import requests
import zipfile
import shutil
import re
from urllib.parse import unquote
from pypinyin import lazy_pinyin

def resolve_internal_url(target_path, current_file_dir_rel_to_repo, base_web_url):
    aiursoft_prefix = "https://cook.aiursoft.com/"
    github_prefix = "https://github.com/Anduin2017/HowToCook/blob/master/"
    is_internal = False
    clean_path = target_path

    if target_path.startswith(aiursoft_prefix):
        clean_path = target_path.replace(aiursoft_prefix, "")
        is_internal = True
        base_rel = ""
    elif target_path.startswith(github_prefix):
        clean_path = target_path.replace(github_prefix, "")
        is_internal = True
        base_rel = ""
    elif not target_path.startswith(("http://", "https://")):
        is_internal = True
        base_rel = current_file_dir_rel_to_repo

    if not is_internal: return target_path

    path_only = clean_path.split('?')[0].split('#')[0]
    decoded = unquote(path_only)
    full_path = os.path.normpath(os.path.join(base_rel, decoded))
    if not os.path.splitext(full_path)[1]: full_path = full_path.rstrip(os.path.sep) + ".md"
    return f"{base_web_url}{full_path.replace(os.path.sep, '/')}"

def convert_tips_to_json(output_file):
    repo_name = "HowToCook"
    zip_url = f"https://github.com/Anduin2017/{repo_name}/archive/refs/heads/master.zip"
    zip_file = "tips_repo.zip"
    repo_dir = f"{repo_name}-master"

    r = requests.get(zip_url, stream=True)
    with open(zip_file, 'wb') as f:
        for chunk in r.iter_content(chunk_size=8192): f.write(chunk)

    with zipfile.ZipFile(zip_file, 'r') as zip_ref: zip_ref.extractall()

    tips_dir = os.path.join(repo_dir, "tips")
    all_tips = []

    # Regex for footer removal
    footer_pattern = r'\n{0,3}如果您遵循本指南的制作流程而发现有问题或可以改进的流程，请提出 Issue 或 Pull request 。\s*$'

    for root, _, files in os.walk(tips_dir):
        for file in files:
            if file.lower().endswith(".md"):
                md_file_path = os.path.join(root, file)
                with open(md_file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                name = os.path.splitext(file)[0]
                current_file_dir_rel_to_repo = os.path.relpath(root, repo_dir)

                # Fix Links
                content = re.sub(r"!\[(.*?)\]\((.*?)\)",
                    lambda m: f"![{m.group(1)}]({resolve_internal_url(m.group(2), current_file_dir_rel_to_repo, 'https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/')})",
                    content, flags=re.IGNORECASE)

                content = re.sub(r"(?<!\!)\[(.*?)\]\((.*?)\)",
                    lambda m: f"[{m.group(1)}]({resolve_internal_url(m.group(2), current_file_dir_rel_to_repo, 'https://github.com/Anduin2017/HowToCook/blob/master/')})" if not m.group(2).startswith("#") else m.group(0),
                    content)

                # Remove first H1, then remove footer
                content = re.sub(r'^#.*?\n\s*\n', '', content, count=1, flags=re.MULTILINE)
                content = re.sub(footer_pattern, '', content).strip()

                all_tips.append({
                    "name": name,
                    "pinyin": "".join(lazy_pinyin(name)).lower().replace(" ", ""),
                    "type": os.path.basename(root) if os.path.basename(root) in ['learn', 'advanced'] else 'basic',
                    "content": content,
                })

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(all_tips, f, ensure_ascii=False, indent=4)
    shutil.rmtree(repo_dir); os.remove(zip_file)

if __name__ == "__main__":
    convert_tips_to_json("tips.json")
