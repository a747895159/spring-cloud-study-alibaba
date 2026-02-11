from pathlib import Path
import json

def generate_docs(output_dir: Path, analysis_results: dict):
    """
    根据分析结果和预设模板生成结构化文档。
    """
    print(f"📚 正在生成结构化文档到 {output_dir}...")
    
    # 确保文档输出目录存在
    doc_output_path = output_dir / "docs"
    doc_output_path.mkdir(parents=True, exist_ok=True)

    # 提取项目名称
    project_name = analysis_results.get("project_name", "未命名项目")

    # 1. 生成 README.md (项目总览)
    readme_content = f"""# {project_name} - 项目总览

## 项目摘要
{analysis_results.get("summary", "无项目摘要。")}

## 主要功能与交互
"""
    for func in analysis_results.get("functions_and_interactions", []):
        readme_content += f"- {func}\n"
    readme_content += "\n"

    readme_content += f"## 技术栈概览
"
    for tech in analysis_results.get("tech_stack", []):
        readme_content += f"- {tech}\n"
    readme_content += "\n"
    
    (doc_output_path / "README.md").write_text(readme_content, encoding='utf-8')
    print("✅ 已生成 docs/README.md")

    # 2. 生成 产品介绍.md
    product_intro_content = f"""# 产品介绍 - {project_name}

## 产品概述
这里将详细介绍产品的背景、目标用户、解决的问题和核心价值。

## 主要功能点
"""
    for func in analysis_results.get("functions_and_interactions", []):
        product_intro_content += f"- {func}\n"
    product_intro_content += "\n"
    
    (doc_output_path / "产品介绍.md").write_text(product_intro_content, encoding='utf-8')
    print("✅ 已生成 docs/产品介绍.md")

    # 3. 生成 技术文档/架构.md
    architecture_content = f"""# 技术文档 - 系统架构

## 架构概览
这里将描述系统的整体架构，包括主要组件和它们之间的关系。

### 逻辑分层
"""
    for layer in analysis_results.get("architecture", {}).get("layers", []):
        architecture_content += f"- {layer}\n"
    architecture_content += "\n"
    
    (doc_output_path / "技术文档_架构.md").write_text(architecture_content, encoding='utf-8')
    print("✅ 已生成 docs/技术文档_架构.md")

    # 4. 生成 技术文档/数据模型.md
    data_model_content = f"""# 技术文档 - 数据模型

## 核心实体
这里将描述系统的核心实体（数据表或数据对象）及其属性。

### 实体列表
"""
    for entity in analysis_results.get("data_model", {}).get("entities", []):
        data_model_content += f"- {entity['name']}: {', '.join(entity['fields'])}\n"
    data_model_content += "\n"
    
    (doc_output_path / "技术文档_数据模型.md").write_text(data_model_content, encoding='utf-8')
    print("✅ 已生成 docs/技术文档_数据模型.md")

    # 5. 生成 代码结构分析.md (待code_analyzer实现后填充更详细内容)
    code_structure_content = f"""# 代码结构分析 - {project_name}

## 代码分层与模块职责
这里将详细分析项目的代码分层、模块组织和各模块的职责。

{analysis_results.get("code_structure", {}).get("summary", "无代码结构分析总结。")}

## 关键代码路径
"""
    # for file_path, info in analysis_results.get("code_structure", {}).get("files", {}).items():
    #     code_structure_content += f"### {file_path}\n"
    #     code_structure_content += f"- 主要功能: {info.get('summary', '无')}\n"
    
    (doc_output_path / "代码结构分析.md").write_text(code_structure_content, encoding='utf-8')
    print("✅ 已生成 docs/代码结构分析.md")


    print("✅ 结构化文档生成完成。")

if __name__ == "__main__":
    print("这是一个子脚本，通常由 analyze_project.py 调用。")
    # 示例调用
    # mock_analysis_results = {
    #     "project_name": "示例项目",
    #     "summary": "这是一个模拟的项目总结。",
    #     "functions_and_interactions": ["用户注册", "商品浏览", "订单管理"],
    #     "tech_stack": ["Python", "Django", "Vue.js", "MySQL"],
    #     "architecture": {"layers": ["Web层", "业务逻辑层", "数据访问层"]},
    #     "data_model": {"entities": [{"name": "User", "fields": ["id", "username", "email"]}, {"name": "Product", "fields": ["id", "name", "price"]}]},
    #     "code_structure": {"summary": "模拟的代码结构总结。"}
    # }
    # generate_docs(Path("./test_output"), mock_analysis_results)
