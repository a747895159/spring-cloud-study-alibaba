from pathlib import Path
import ast
import re

def parse_code_files(code_files: list[Path]) -> dict:
    """
    解析代码文件，提取函数、类、模块等结构信息。
    """
    print(f"解析 {len(code_files)} 个代码文件...")
    parsed_data = {
        "files": {}
    }

    for file_path in code_files:
        try:
            content = file_path.read_text(encoding='utf-8')
            tree = ast.parse(content)
            
            file_info = {
                "classes": [],
                "functions": [],
                "imports": [],
                "comments": []
            }

            for node in ast.walk(tree):
                if isinstance(node, ast.ClassDef):
                    file_info["classes"].append({
                        "name": node.name,
                        "lineno": node.lineno,
                        "methods": [n.name for n in node.body if isinstance(n, ast.FunctionDef)]
                    })
                elif isinstance(node, ast.FunctionDef):
                    file_info["functions"].append({
                        "name": node.name,
                        "lineno": node.lineno,
                        "args": [arg.arg for arg in node.args.args]
                    })
                elif isinstance(node, (ast.Import, ast.ImportFrom)):
                    for n in node.names:
                        file_info["imports"].append(n.name)
            
            # 提取行注释
            comments = re.findall(r'^\s*#\s*(.*)$', content, re.MULTILINE)
            if comments:
                file_info["comments"] = comments

            parsed_data["files"][str(file_path.relative_to(Path.cwd()))] = file_info
        except Exception as e:
            print(f"❌ 解析文件 {file_path} 失败: {e}")
            continue

    print("✅ 代码文件解析完成。")
    return parsed_data

if __name__ == "__main__":
    # 示例用法
    # 需要先创建一些测试文件
    # test_dir = Path("test_project")
    # test_dir.mkdir(exist_ok=True)
    # (test_dir / "module1.py").write_text("def func1(): pass")
    # files = [test_dir / "module1.py"]
    # result = parse_code_files(files)
    # print(json.dumps(result, indent=2, ensure_ascii=False))
    print("这是一个子脚本，通常由 analyze_project.py 调用。")
