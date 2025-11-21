import os
import json
import re
import requests
import zipfile
import shutil

def convert_tips_to_json(repo_url, output_file):
    """
    Downloads a Git repository, processes all Markdown files in the 'tips'
    directory and its subdirectories, and saves the data to a JSON file.

    Args:
        repo_url (str): The URL of the Git repository.
        output_file (str): The path to the output JSON file.
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

                # Determine the type based on the parent folder
                parent_folder = os.path.basename(root)
                tip_type = 'basic' # Default type
                if parent_folder == 'learn':
                    tip_type = 'learn'
                elif parent_folder == 'advanced':
                    tip_type = 'advanced'
                # If the parent is 'tips', it remains 'basic'

                # Read the full content of the markdown file
                with open(md_file_path, "r", encoding="utf-8") as f:
                    content = f.read()

                tip_data = {
                    "name": name,
                    "type": tip_type,
                    "content": content,
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