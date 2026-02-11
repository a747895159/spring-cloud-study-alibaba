#!/bin/bash
# 模板：表单自动化工作流
# 填写并提交网页表单，带验证功能

set -euo pipefail

FORM_URL="${1:?Usage: $0 <form-url>}"

echo "正在自动化表单：$FORM_URL"

# 导航到表单页面
agent-browser open "$FORM_URL"
agent-browser wait --load networkidle

# 获取交互快照以识别表单字段
echo "正在分析表单结构..."
agent-browser snapshot -i

# 示例：填写常见表单字段
# 根据快照输出取消注释并修改 refs

# 文本输入框
# agent-browser fill @e1 "John Doe"           # 姓名字段
# agent-browser fill @e2 "user@example.com"   # 邮箱字段
# agent-browser fill @e3 "+1-555-123-4567"    # 电话字段

# 密码字段
# agent-browser fill @e4 "SecureP@ssw0rd!"

# 下拉菜单
# agent-browser select @e5 "Option Value"

# 复选框
# agent-browser check @e6                      # 选中
# agent-browser uncheck @e7                    # 取消选中

# 单选按钮
# agent-browser click @e8                      # 选择单选选项

# 文本域
# agent-browser fill @e9 "多行文本内容"

# 文件上传
# agent-browser upload @e10 /path/to/file.pdf

# 提交表单
# agent-browser click @e11                     # 提交按钮

# 等待响应
# agent-browser wait --load networkidle
# agent-browser wait --url "**/success"        # 或等待重定向

# 验证提交结果
echo "表单提交结果："
agent-browser get url
agent-browser snapshot -i

# 截取结果页面
agent-browser screenshot /tmp/form-result.png

# 清理
agent-browser close

echo "表单自动化完成"
