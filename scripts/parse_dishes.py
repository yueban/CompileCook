import os
import json
import re
import requests
import zipfile
import shutil
from urllib.parse import unquote
from pypinyin import lazy_pinyin

def contains_chinese(text):
    return re.search(r'[\u4e00-\u9fa5]', text)

def clean_description(text):
    text = re.sub(r'<!--[\s\S]*?-->', '', text)
    text = re.sub(r'!\[.*?\]\(.*?\)', '', text)
    text = re.sub(r'^>.*$', '', text, flags=re.MULTILINE)
    text = re.sub(r'预估烹饪难度：.*', '', text)
    text = re.sub(r'\*{1,3}(.*?)\*{1,3}', r'\1', text)
    text = re.sub(r'_{1,3}(.*?)_{1,3}', r'\1', text)
    lines = [line.strip() for line in text.split('\n') if line.strip()]
    return '\n\n'.join(lines)

def resolve_internal_url(target_path, current_file_dir_rel_to_repo, base_web_url):
    """
    Normalizes a link to be a decoded, absolute GitHub master branch URL.
    """
    aiursoft_prefix = "https://cook.aiursoft.com/"
    github_prefix = "https://github.com/Anduin2017/HowToCook/blob/master/"
    is_internal = False
    clean_path = target_path

    # Case 1: External deployment link
    if target_path.startswith(aiursoft_prefix):
        clean_path = target_path.replace(aiursoft_prefix, "")
        is_internal = True
        base_rel = "" # Aiursoft links are relative to repo root
    # Case 2: Already absolute GitHub link (needs decoding/cleaning)
    elif target_path.startswith(github_prefix):
        clean_path = target_path.replace(github_prefix, "")
        is_internal = True
        base_rel = "" # Relative to repo root
    # Case 3: Truly relative link
    elif not target_path.startswith(("http://", "https://")):
        is_internal = True
        base_rel = current_file_dir_rel_to_repo

    if not is_internal: return target_path

    # 1. Strip Query params (?) and Anchors (#)
    path_only = clean_path.split('?')[0].split('#')[0]

    # 2. Decode URL encoding
    decoded = unquote(path_only)

    # 3. Resolve ".." and "."
    full_path = os.path.normpath(os.path.join(base_rel, decoded))

    # 4. Handle directory-style links or missing extensions
    # If it's a file in the repo and doesn't have an extension, it's likely a .md
    if not os.path.splitext(full_path)[1]:
        full_path = full_path.rstrip(os.path.sep) + ".md"

    # Convert Windows slashes to URL slashes
    web_path = full_path.replace(os.path.sep, '/')
    return f"{base_web_url}{web_path}"

def convert_md_to_json(output_file):
    repo_owner = "Anduin2017"
    repo_name = "HowToCook"
    branch = "master"
    zip_url = f"https://github.com/{repo_owner}/{repo_name}/archive/refs/heads/{branch}.zip"
    zip_file = f"{repo_name}.zip"
    repo_dir = f"{repo_name}-{branch}"

    if not os.path.exists(zip_file):
        print("Downloading repository...")
        r = requests.get(zip_url, stream=True)
        with open(zip_file, 'wb') as f:
            for chunk in r.iter_content(chunk_size=8192): f.write(chunk)

    if os.path.exists(repo_dir): shutil.rmtree(repo_dir)
    with zipfile.ZipFile(zip_file, 'r') as zip_ref: zip_ref.extractall()

    dishes_dir = os.path.join(repo_dir, "dishes")
    all_dishes = []

    # Regex for footer removal (0-3 newlines + footer text)
    footer_pattern = r'\n{0,3}如果您遵循本指南的制作流程而发现有问题或可以改进的流程，请提出 Issue 或 Pull request 。\s*$'

    for root, _, files in os.walk(dishes_dir):
        if os.path.join(dishes_dir, 'template') in root: continue

        for file in files:
            if file.lower().endswith(".md"):
                md_file_path = os.path.join(root, file)
                with open(md_file_path, "r", encoding="utf-8") as f:
                    raw_markdown = f.read()

                name = os.path.splitext(file)[0]
                pinyin_value = "".join(lazy_pinyin(name)).lower().replace(" ", "")
                current_file_dir_rel_to_repo = os.path.relpath(root, repo_dir)

                # Fix Images
                content_fixed = re.sub(r"!\[(.*?)\]\((.*?)\)",
                    lambda m: f"![{m.group(1)}]({resolve_internal_url(m.group(2), current_file_dir_rel_to_repo, 'https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/')})",
                    raw_markdown, flags=re.IGNORECASE)

                # Fix Normal Links
                content_fixed = re.sub(r"(?<!\!)\[(.*?)\]\((.*?)\)",
                    lambda m: f"[{m.group(1)}]({resolve_internal_url(m.group(2), current_file_dir_rel_to_repo, 'https://github.com/Anduin2017/HowToCook/blob/master/')})" if not m.group(2).startswith("#") else m.group(0),
                    content_fixed)

                # Description Extraction
                description_match = re.search(r'#\s*.*?\n([\s\S]*?)(?=\n##)', content_fixed)
                description = clean_description(description_match.group(1).strip()) if description_match else ""

                # Content field: Remove H1, then remove footer
                content_field = re.sub(r'^#.*?\n+', '', content_fixed, count=1).strip()
                content_field = re.sub(footer_pattern, '', content_field).strip()

                difficulty_match = re.search(r"预估烹饪难度：(★+)", raw_markdown)
                image_match = re.search(r"!\[.*?\]\((.*?)\)", content_fixed)

                # Logic for category with hyphen to underscore conversion
                raw_cat = os.path.basename(os.path.dirname(root)) if contains_chinese(os.path.basename(root)) else os.path.basename(root)
                category = raw_cat.replace('-', '_')

                all_dishes.append({
                    "name": name,
                    "pinyin": pinyin_value,
                    "description": description,
                    "category": category,
                    "difficulty": len(difficulty_match.group(1)) if difficulty_match else 0,
                    "image": image_match.group(1) if image_match else "",
                    "content": content_field
                })

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(all_dishes, f, ensure_ascii=False, indent=4)
    shutil.rmtree(repo_dir); os.remove(zip_file)

if __name__ == "__main__":
    convert_md_to_json("dishes.json")
