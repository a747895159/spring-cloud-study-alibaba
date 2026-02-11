#!/bin/bash
# 模板：内容捕获工作流
# 从网页提取内容，支持可选认证

set -euo pipefail

TARGET_URL="${1:?Usage: $0 <url> [output-dir]}"
OUTPUT_DIR="${2:-.}"

echo "正在从以下地址捕获内容：$TARGET_URL"
mkdir -p "$OUTPUT_DIR"

# 可选：如需认证则加载认证状态
# if [[ -f "./auth-state.json" ]]; then
#     agent-browser state load "./auth-state.json"
# fi

# 导航到目标页面
agent-browser open "$TARGET_URL"
agent-browser wait --load networkidle

# 获取页面元数据
echo "页面标题：$(agent-browser get title)"
echo "页面 URL：$(agent-browser get url)"

# 捕获整页截图
agent-browser screenshot --full "$OUTPUT_DIR/page-full.png"
echo "截图已保存：$OUTPUT_DIR/page-full.png"

# 获取页面结构
agent-browser snapshot -i > "$OUTPUT_DIR/page-structure.txt"
echo "结构已保存：$OUTPUT_DIR/page-structure.txt"

# 提取主要内容
# 根据目标网站结构调整选择器
# agent-browser get text @e1 > "$OUTPUT_DIR/main-content.txt"

# 提取特定元素（按需取消注释）
# agent-browser get text "article" > "$OUTPUT_DIR/article.txt"
# agent-browser get text "main" > "$OUTPUT_DIR/main.txt"
# agent-browser get text ".content" > "$OUTPUT_DIR/content.txt"

# 获取整页文本
agent-browser get text body > "$OUTPUT_DIR/page-text.txt"
echo "文本内容已保存：$OUTPUT_DIR/page-text.txt"

# 可选：保存为 PDF
agent-browser pdf "$OUTPUT_DIR/page.pdf"
echo "PDF 已保存：$OUTPUT_DIR/page.pdf"

# 可选：对无限滚动页面进行滚动捕获
# scroll_and_capture() {
#     local count=0
#     while [[ $count -lt 5 ]]; do
#         agent-browser scroll down 1000
#         agent-browser wait 1000
#         ((count++))
#     done
#     agent-browser screenshot --full "$OUTPUT_DIR/page-scrolled.png"
# }
# scroll_and_capture

# 清理
agent-browser close

echo ""
echo "捕获完成！文件已保存到：$OUTPUT_DIR"
ls -la "$OUTPUT_DIR"
