import os
import json
import requests
import zipfile
import shutil
import re
from pypinyin import lazy_pinyin

def convert_tips_to_json(repo_url, output_file):
    """
    Downloads a Git repository, processes all Markdown files in the 'tips'
    directory, generates a pinyin field for each, removes the first H1 heading,
    and saves the data to a JSON file.
    """
    repo_owner = "Anduin2017"
    repo_name = "HowToCook"
    branch = "master"
    zip_url = f"https://github.com/{repo_owner}/{repo_name}/archive/refs/heads/{branch}.zip"
    zip_file = f"{repo_name}.zip"
    repo_dir = f"{repo_name}-{branch}"

    # Download the repository zip file if it doesn't exist
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

    # Extract the zip file, removing old extractions first
    if os.path.exists(repo_dir):
        shutil.rmtree(repo_dir)
    with zipfile.ZipFile(zip_file, 'r') as zip_ref:
        zip_ref.extractall()

    tips_dir = os.path.join(repo_dir, "tips")
    all_tips = []

    # Walk through the tips directory
    for root, _, files in os.walk(tips_dir):
        for file in files:
            if file.lower().endswith(".md"):
                md_file_path = os.path.join(root, file)

                # Get the name from the filename
                name = os.path.splitext(file)[0]

                # Generate Pinyin
                pinyin_value = "".join(lazy_pinyin(name)).lower().replace(" ", "")

                # Determine the type based on the parent folder
                parent_folder = os.path.basename(root)
                tip_type = 'basic' # Default type
                if parent_folder == 'learn':
                    tip_type = 'learn'
                elif parent_folder == 'advanced':
                    tip_type = 'advanced'

                # Read the full content of the markdown file
                with open(md_file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                content = re.sub(r'^#.*?\n\s*\n', '', content, count=1, flags=re.MULTILINE)

                tip_data = {
                    "name": name,
                    "pinyin": pinyin_value,
                    "type": tip_type,
                    "content": content.strip(),
                }
                all_tips.append(tip_data)

    # Write the JSON output
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(all_tips, f, ensure_ascii=False, indent=4)
    print(f"Successfully converted all tips to {output_file}")

    # Clean up downloaded files
    if os.path.exists(zip_file):
        os.remove(zip_file)
    if os.path.exists(repo_dir):
        shutil.rmtree(repo_dir)
    print("Cleaned up temporary files.")


# To run the conversion
if __name__ == "__main__":
    repo_url = "https://github.com/Anduin2017/HowToCook"
    output_file = "tips.json"
    convert_tips_to_json(repo_url, output_file)
