from pathlib import Path
import re

def summarize_documents(doc_files: list[Path]) -> dict:
    """
    对文档文件进行摘要，提取关键信息。
    """
    print(f"摘要 {len(doc_files)} 个文档文件...")
    summaries = {
        "files": {}
    }

    for file_path in doc_files:
        try:
            content = file_path.read_text(encoding='utf-8')
            
            # 提取标题 (Markdown 格式)
            title_match = re.search(r'#\s*(.*)', content)
            title = title_match.group(1).strip() if title_match else file_path.name
            
            # 提取前几段作为摘要
            paragraphs = [p.strip() for p in content.split('\n\n') if p.strip()]
            summary_text = " ".join(paragraphs[:3]) # 取前三段
            
            summaries["files"][str(file_path.relative_to(Path.cwd()))] = {
                "title": title,
                "summary": summary_text
            }
        except Exception as e:
            print(f"❌ 摘要文件 {file_path} 失败: {e}")
            continue

    print("✅ 文档摘要完成。")
    return summaries

if __name__ == "__main__":
    # 示例用法
    # 需要先创建一些测试文件
    # test_dir = Path("test_docs")
    # test_dir.mkdir(exist_ok=True)
    # (test_dir / "README.md").write_text("# My Project\n\nThis is a test project. It does X, Y, Z.")
    # files = [test_dir / "README.md"]
    # result = summarize_documents(files)
    # print(result)
    print("这是一个子脚本，通常由 analyze_project.py 调用。")
