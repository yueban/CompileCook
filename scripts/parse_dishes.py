import os
import json
import re
import requests
import zipfile
import shutil
from pypinyin import lazy_pinyin

def contains_chinese(text):
    """Checks if a string contains any Chinese characters."""
    return re.search(r'[\u4e00-\u9fa5]', text)

def clean_description(text):
    """
    Cleans the description text by removing images, difficulty ratings,
    quotes, HTML comments, and markdown formatting markers.
    """
    # 1. Remove HTML comments
    text = re.sub(r'<!--[\s\S]*?-->', '', text)

    # 2. Remove Markdown images: ![alt](url)
    text = re.sub(r'!\[.*?\]\(.*?\)', '', text)

    # 3. Remove Blockquotes: lines starting with >
    text = re.sub(r'^>.*$', '', text, flags=re.MULTILINE)

    # 4. Remove Difficulty line: 预估烹饪难度：★
    text = re.sub(r'预估烹饪难度：.*', '', text)

    # 5. Remove Markdown bold and italic markers but keep the text inside
    # Remove ***bold italic***, **bold**, *italic*
    text = re.sub(r'\*{1,3}(.*?)\*{1,3}', r'\1', text)
    text = re.sub(r'_{1,3}(.*?)_{1,3}', r'\1', text)

    # 6. Cleanup: Remove extra whitespace and newlines
    # Split into lines, strip them, and join back with double newlines
    lines = [line.strip() for line in text.split('\n') if line.strip()]

    # Note: If you only want the VERY first paragraph (to match your example 2 exactly),
    # you could return lines[0] if lines else "".
    # Otherwise, joining them is safer for multi-paragraph descriptions.
    return '\n\n'.join(lines)

def convert_md_to_json(repo_url, output_file):
    repo_owner = "Anduin2017"
    repo_name = "HowToCook"
    branch = "master"
    zip_url = f"https://github.com/{repo_owner}/{repo_name}/archive/refs/heads/{branch}.zip"
    zip_file = f"{repo_name}.zip"
    repo_dir = f"{repo_name}-{branch}"

    if not os.path.exists(zip_file):
        print(f"Downloading repository from {zip_url}...")
        try:
            r = requests.get(zip_url, stream=True, timeout=30)
            r.raise_for_status()
            with open(zip_file, 'wb') as f:
                for chunk in r.iter_content(chunk_size=8192):
                    if chunk:
                        f.write(chunk)
        except requests.exceptions.RequestException as e:
            print(f"Error downloading repository: {e}")
            return

    if os.path.exists(repo_dir):
        shutil.rmtree(repo_dir)
    with zipfile.ZipFile(zip_file, 'r') as zip_ref:
        zip_ref.extractall()

    dishes_dir = os.path.join(repo_dir, "dishes")
    template_dir_to_skip = os.path.join(dishes_dir, 'template')
    all_dishes = []

    for root, _, files in os.walk(dishes_dir):
        if root.startswith(template_dir_to_skip):
            continue

        for file in files:
            if file.lower().endswith(".md"):
                md_file_path = os.path.join(root, file)
                with open(md_file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                name_without_ext = os.path.splitext(file)[0]
                pinyin_value = "".join(lazy_pinyin(name_without_ext)).lower().replace(" ", "")

                parent_dir_name = os.path.basename(root)
                category = os.path.basename(os.path.dirname(root)) if contains_chinese(parent_dir_name) else parent_dir_name

                # Create version with fixed images
                def replace_image_path(match):
                    image_alt, image_path = match.groups()
                    md_dir_rel_path = os.path.relpath(root, dishes_dir).replace(os.path.sep, '/')
                    full_image_path = os.path.normpath(f"{md_dir_rel_path}/{image_path}").replace(os.path.sep, '/')
                    base_url = "https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/dishes/"
                    return f"![{image_alt}]({base_url}{full_image_path})"

                content_with_fixed_images = re.sub(r"!\[(.*?)\]\((?!https?://)(.*?)\)", replace_image_path, content, flags=re.IGNORECASE)

                # Extract raw description
                description_match = re.search(r'#\s*.*?\n([\s\S]*?)(?=\n##)', content_with_fixed_images)
                raw_description = description_match.group(1).strip() if description_match else ""

                # OPTIMIZATION: Clean the description
                description = clean_description(raw_description)

                difficulty_match = re.search(r"预估烹饪难度：(★+)", content)
                difficulty = len(difficulty_match.group(1)) if difficulty_match else 0

                # Extract primary image (before cleaning markers)
                image_match = re.search(r"!\[.*?\]\((.*?)\)", raw_description)
                if not image_match:
                     image_match = re.search(r"!\[.*?\]\((.*?)\)", content_with_fixed_images)
                image = image_match.group(1) if image_match else ""

                def get_section_content(heading, source_content):
                    match = re.search(fr"## {heading}\n\n(.*?)(?=\n##|\Z)", source_content, re.DOTALL)
                    return match.group(1).strip() if match else ""

                ingredient = get_section_content("必备原料和工具", content_with_fixed_images)
                calculation = get_section_content("计算", content_with_fixed_images)
                operation = get_section_content("操作", content_with_fixed_images)
                addition = get_section_content("附加内容", content_with_fixed_images)

                addition_to_remove = "如果您遵循本指南的制作流程而发现有问题或可以改进的流程，请提出 Issue 或 Pull request 。"
                addition = addition.replace(addition_to_remove, "").strip()

                dish_data = {
                    "name": name_without_ext,
                    "pinyin": pinyin_value,
                    "description": description,
                    "category": category,
                    "difficulty": difficulty,
                    "image": image,
                    "ingredient": ingredient,
                    "calculation": calculation,
                    "operation": operation,
                    "addition": addition,
                }
                all_dishes.append(dish_data)

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(all_dishes, f, ensure_ascii=False, indent=4)
    print(f"Successfully converted all markdown files to {output_file}")

    if os.path.exists(zip_file): os.remove(zip_file)
    if os.path.exists(repo_dir): shutil.rmtree(repo_dir)

if __name__ == "__main__":
    repo_url = "https://github.com/Anduin2017/HowToCook"
    output_file = "dishes.json"
    convert_md_to_json(repo_url, output_file)
