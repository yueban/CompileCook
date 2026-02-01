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
    # 5. Remove Markdown bold and italic markers
    text = re.sub(r'\*{1,3}(.*?)\*{1,3}', r'\1', text)
    text = re.sub(r'_{1,3}(.*?)_{1,3}', r'\1', text)

    # 6. Cleanup: Remove extra whitespace and newlines
    lines = [line.strip() for line in text.split('\n') if line.strip()]
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
                    raw_markdown = f.read()

                name_without_ext = os.path.splitext(file)[0]
                pinyin_value = "".join(lazy_pinyin(name_without_ext)).lower().replace(" ", "")

                parent_dir_name = os.path.basename(root)
                category = os.path.basename(os.path.dirname(root)) if contains_chinese(parent_dir_name) else parent_dir_name

                # 1. Correct all image links in the entire document
                def replace_image_path(match):
                    image_alt, image_path = match.groups()
                    md_dir_rel_path = os.path.relpath(root, dishes_dir).replace(os.path.sep, '/')
                    full_image_path = os.path.normpath(f"{md_dir_rel_path}/{image_path}").replace(os.path.sep, '/')
                    base_url = "https://media.githubusercontent.com/media/Anduin2017/HowToCook/master/dishes/"
                    return f"![{image_alt}]({base_url}{full_image_path})"

                content_with_fixed_images = re.sub(r"!\[(.*?)\]\((?!https?://)(.*?)\)", replace_image_path, raw_markdown, flags=re.IGNORECASE)

                # 2. Extract and clean description (for summary purposes)
                # Still using your specific regex to capture the block before the first ##
                description_match = re.search(r'#\s*.*?\n([\s\S]*?)(?=\n##)', content_with_fixed_images)
                raw_desc_block = description_match.group(1).strip() if description_match else ""
                description = clean_description(raw_desc_block)

                # 3. Process the new 'content' field:
                # Remove the first H1 heading and its following newlines
                content_field = re.sub(r'^#.*?\n+', '', content_with_fixed_images, count=1).strip()

                # 4. Extract difficulty (from original content to avoid issues with fixed URLs)
                difficulty_match = re.search(r"预估烹饪难度：(★+)", raw_markdown)
                difficulty = len(difficulty_match.group(1)) if difficulty_match else 0

                # 5. Extract the primary image URL (used for thumbnails/previews)
                image_match = re.search(r"!\[.*?\]\((.*?)\)", content_with_fixed_images)
                image = image_match.group(1) if image_match else ""

                dish_data = {
                    "name": name_without_ext,
                    "pinyin": pinyin_value,
                    "description": description,
                    "category": category,
                    "difficulty": difficulty,
                    "image": image,
                    "content": content_field
                }
                all_dishes.append(dish_data)

    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(all_dishes, f, ensure_ascii=False, indent=4)
    print(f"Successfully converted all markdown files to {output_file}")

    # Cleanup
    if os.path.exists(zip_file): os.remove(zip_file)
    if os.path.exists(repo_dir): shutil.rmtree(repo_dir)

if __name__ == "__main__":
    repo_url = "https://github.com/Anduin2017/HowToCook"
    output_file = "dishes.json"
    convert_md_to_json(repo_url, output_file)
