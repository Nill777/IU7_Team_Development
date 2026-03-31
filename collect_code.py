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
                    print(f"Пропущен (имя): {filename}")
                    continue
                
                _, ext = os.path.splitext(filename)
                if ext.lower() in ignore_extensions:
                    print(f"Пропущен (тип): {filename}")
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
                        
                    print(f"Записан: {filename}")
                except UnicodeDecodeError:
                    continue
                except Exception as e:
                    print(f"Ошибка чтения {full_path}: {e}")

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Использование: python collect_code.py /путь/к/директории")
    else:
        target_dir = sys.argv[1]
        if os.path.isdir(target_dir):
            collect_code(target_dir)
            print("\nГотово. Файл all_project_code.txt создан.")
        else:
            print("Указанный путь не является директорией.")
