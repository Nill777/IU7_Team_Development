import os
import sys

def collect_code(root_dir):
    root_dir = os.path.abspath(root_dir)
    output_file = os.path.join(root_dir, "all_project_code.txt")
    
    ignore_dirs = {'.git', '__pycache__', 'venv', 'env', 'node_modules', '.idea', '.vscode', 'build', 'dist', '.cxx'}
    
    ignore_extensions = {
        '.png', '.jpg', '.jpeg', '.gif', '.svg', '.ico', # Картинки
        '.exe', '.bin', '.pyc', '.o', '.a',              # Бинарники
        '.zip', '.tar', '.gz', '.rar', '.7z',            # Архивы
        '.pdf', '.doc', '.docx', '.xls', '.xlsx',        # Документы
        '.db', '.sqlite', '.sqlite3'                     # Базы данных
    }

    ignore_names = {'licence', 'license', 'copying', 'changelog', 'contributing'}

    with open(output_file, 'w', encoding='utf-8') as outfile:
        for dirpath, dirnames, filenames in os.walk(root_dir):
            dirnames[:] = [d for d in dirnames if d not in ignore_dirs]
            
            for filename in filenames:
                full_path = os.path.join(dirpath, filename)
                
                if full_path == output_file:
                    continue
                
                if any(ignored in filename.lower() for ignored in ignore_names):
                    print(f"Skiping (name): {filename}")
                    continue
                
                _, ext = os.path.splitext(filename)
                if ext.lower() in ignore_extensions:
                    print(f"Skiping (type): {filename}")
                    continue

                try:
                    with open(full_path, 'r', encoding='utf-8') as infile:
                        content = infile.read()
                        
                        outfile.write(f"{full_path}\n")
                        outfile.write("```\n")
                        outfile.write(content)
                        if content and not content.endswith('\n'):
                            outfile.write('\n')
                        outfile.write("```\n\n")
                        
                    print(f"Recorded: {filename}")
                except UnicodeDecodeError:
                    print(f"Skipped due to encoding: {full_path}")
                    continue
                except IOError as e:
                    print(f"I/O error while reading {full_path}: {e}")
                    continue
                except Exception as e:
                    print(f"Critical error while processing {full_path}: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python collect_code.py /path/to/directory")
    else:
        target_dir = sys.argv[1]
        if os.path.isdir(target_dir):
            collect_code(target_dir)
            print("\nDone. The all_project_code.txt file has been created")
        else:
            print("The specified path is not a directory")
