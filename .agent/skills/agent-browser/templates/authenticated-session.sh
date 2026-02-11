#!/bin/bash
# 模板：认证会话工作流
# 登录一次，保存状态，后续运行复用
#
# 用法：
#   ./authenticated-session.sh <login-url> [state-file]
#
# 设置步骤：
#   1. 首次运行查看表单结构
#   2. 记下表单字段的 @refs
#   3. 取消注释 LOGIN FLOW 部分并更新 refs

set -euo pipefail

LOGIN_URL="${1:?Usage: $0 <login-url> [state-file]}"
STATE_FILE="${2:-./auth-state.json}"

echo "认证工作流目标：$LOGIN_URL"

# ══════════════════════════════════════════════════════════════
# 已保存状态：如果有有效的已保存状态则跳过登录
# ══════════════════════════════════════════════════════════════
if [[ -f "$STATE_FILE" ]]; then
    echo "正在加载已保存的认证状态..."
    agent-browser state load "$STATE_FILE"
    agent-browser open "$LOGIN_URL"
    agent-browser wait --load networkidle

    CURRENT_URL=$(agent-browser get url)
    if [[ "$CURRENT_URL" != *"login"* ]] && [[ "$CURRENT_URL" != *"signin"* ]]; then
        echo "会话恢复成功！"
        agent-browser snapshot -i
        exit 0
    fi
    echo "会话已过期，执行新的登录..."
    rm -f "$STATE_FILE"
fi

# ══════════════════════════════════════════════════════════════
# 发现模式：显示表单结构（设置完成后删除此部分）
# ══════════════════════════════════════════════════════════════
echo "正在打开登录页面..."
agent-browser open "$LOGIN_URL"
agent-browser wait --load networkidle

echo ""
echo "┌─────────────────────────────────────────────────────────┐"
echo "│ 登录表单结构                                             │"
echo "├─────────────────────────────────────────────────────────┤"
agent-browser snapshot -i
echo "└─────────────────────────────────────────────────────────┘"
echo ""
echo "下一步："
echo "  1. 记录 refs：@e? = 用户名, @e? = 密码, @e? = 提交按钮"
echo "  2. 取消注释下方 LOGIN FLOW 部分"
echo "  3. 将 @e1, @e2, @e3 替换为你的 refs"
echo "  4. 删除此发现模式部分"
echo ""
agent-browser close
exit 0

# ══════════════════════════════════════════════════════════════
# 登录流程：发现完成后取消注释并自定义
# ══════════════════════════════════════════════════════════════
# : "${APP_USERNAME:?请设置 APP_USERNAME 环境变量}"
# : "${APP_PASSWORD:?请设置 APP_PASSWORD 环境变量}"
#
# agent-browser open "$LOGIN_URL"
# agent-browser wait --load networkidle
# agent-browser snapshot -i
#
# # 填写凭据（更新 refs 以匹配你的表单）
# agent-browser fill @e1 "$APP_USERNAME"
# agent-browser fill @e2 "$APP_PASSWORD"
# agent-browser click @e3
# agent-browser wait --load networkidle
#
# # 验证登录是否成功
# FINAL_URL=$(agent-browser get url)
# if [[ "$FINAL_URL" == *"login"* ]] || [[ "$FINAL_URL" == *"signin"* ]]; then
#     echo "错误：登录失败 - 仍在登录页面"
#     agent-browser screenshot /tmp/login-failed.png
#     agent-browser close
#     exit 1
# fi
#
# # 保存状态以供后续使用
# echo "正在保存认证状态到：$STATE_FILE"
# agent-browser state save "$STATE_FILE"
# echo "登录成功！"
# agent-browser snapshot -i
