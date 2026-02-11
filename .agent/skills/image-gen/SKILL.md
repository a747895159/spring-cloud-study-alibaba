---
name: baoyu-图像生成
description: 使用 OpenAI 和 Google API 进行 AI 图像生成。支持文生图、参考图片、宽高比和并行生成（推荐 4 个并发子代理）。当用户要求生成、创建或绘制图像时使用。
---

# 图像生成（AI SDK）

基于官方 API 的图像生成。支持 OpenAI 和 Google 提供商。

## 脚本目录

**代理执行**：
1. `SKILL_DIR` = 此 SKILL.md 文件所在目录
2. 脚本路径 = `${SKILL_DIR}/scripts/main.ts`

## 偏好设置（EXTEND.md）

使用 Bash 检查 EXTEND.md 是否存在（优先级顺序）：

```bash
# 首先检查项目级
test -f .baoyu-skills/baoyu-image-gen/EXTEND.md && echo "project"

# 然后检查用户级（跨平台：$HOME 在 macOS/Linux/WSL 上均可使用）
test -f "$HOME/.baoyu-skills/baoyu-image-gen/EXTEND.md" && echo "user"
```

┌──────────────────────────────────────────────────┬───────────────────┐
│                       路径                       │       位置        │
├──────────────────────────────────────────────────┼───────────────────┤
│ .baoyu-skills/baoyu-image-gen/EXTEND.md          │ 项目目录          │
├──────────────────────────────────────────────────┼───────────────────┤
│ $HOME/.baoyu-skills/baoyu-image-gen/EXTEND.md    │ 用户主目录        │
└──────────────────────────────────────────────────┴───────────────────┘

┌───────────┬───────────────────────────────────────────────────────────────────────────┐
│   结果    │                                  操作                                    │
├───────────┼───────────────────────────────────────────────────────────────────────────┤
│ 找到      │ 读取、解析、应用设置                                                      │
├───────────┼───────────────────────────────────────────────────────────────────────────┤
│ 未找到    │ 使用默认值                                                                │
└───────────┴───────────────────────────────────────────────────────────────────────────┘

**EXTEND.md 支持**：默认提供商 | 默认质量 | 默认宽高比

## 用法

```bash
# 基本用法
npx -y bun ${SKILL_DIR}/scripts/main.ts --prompt "A cat" --image cat.png

# 指定宽高比
npx -y bun ${SKILL_DIR}/scripts/main.ts --prompt "A landscape" --image out.png --ar 16:9

# 高质量
npx -y bun ${SKILL_DIR}/scripts/main.ts --prompt "A cat" --image out.png --quality 2k

# 从提示文件读取
npx -y bun ${SKILL_DIR}/scripts/main.ts --promptfiles system.md content.md --image out.png

# 使用参考图片（仅 Google 多模态）
npx -y bun ${SKILL_DIR}/scripts/main.ts --prompt "Make blue" --image out.png --ref source.png

# 指定提供商
npx -y bun ${SKILL_DIR}/scripts/main.ts --prompt "A cat" --image out.png --provider openai
```

## 选项

| 选项 | 描述 |
|------|------|
| `--prompt <text>`, `-p` | 提示文本 |
| `--promptfiles <files...>` | 从文件读取提示（拼接） |
| `--image <path>` | 输出图片路径（必需） |
| `--provider google\|openai` | 强制指定提供商（默认：google） |
| `--model <id>`, `-m` | 模型 ID |
| `--ar <ratio>` | 宽高比（例如 `16:9`、`1:1`、`4:3`） |
| `--size <WxH>` | 尺寸（例如 `1024x1024`） |
| `--quality normal\|2k` | 质量预设（默认：2k） |
| `--imageSize 1K\|2K\|4K` | Google 图片尺寸（默认：根据质量） |
| `--ref <files...>` | 参考图片（仅 Google 多模态） |
| `--n <count>` | 生成图片数量 |
| `--json` | JSON 输出 |

## 环境变量

| 变量 | 描述 |
|------|------|
| `OPENAI_API_KEY` | OpenAI API 密钥 |
| `GOOGLE_API_KEY` | Google API 密钥 |
| `OPENAI_IMAGE_MODEL` | OpenAI 模型覆盖 |
| `GOOGLE_IMAGE_MODEL` | Google 模型覆盖 |
| `OPENAI_BASE_URL` | 自定义 OpenAI 端点 |
| `GOOGLE_BASE_URL` | 自定义 Google 端点 |

**加载优先级**：命令行参数 > 环境变量 > `<cwd>/.baoyu-skills/.env` > `~/.baoyu-skills/.env`

## 提供商选择

1. 指定了 `--provider` → 使用它
2. 仅有一个 API 密钥 → 使用该提供商
3. 两者都有 → 默认使用 Google

## 质量预设

| 预设 | Google 图片尺寸 | OpenAI 尺寸 | 使用场景 |
|------|-----------------|-------------|----------|
| `normal` | 1K | 1024px | 快速预览 |
| `2k`（默认） | 2K | 2048px | 封面、插图、信息图 |

**Google 图片尺寸**：可通过 `--imageSize 1K|2K|4K` 覆盖

## 宽高比

支持：`1:1`、`16:9`、`9:16`、`4:3`、`3:4`、`2.35:1`

- Google 多模态：使用 `imageConfig.aspectRatio`
- Google Imagen：使用 `aspectRatio` 参数
- OpenAI：映射到最接近的支持尺寸

## 并行生成

通过后台子代理支持并发图像生成，适用于批量操作。

| 设置 | 值 |
|------|------|
| 推荐并发数 | 4 个子代理 |
| 最大并发数 | 8 个子代理 |
| 使用场景 | 批量生成（幻灯片、漫画、信息图） |

**代理实现**：
```
# 使用 Task 工具并行启动多个生成任务
# 每个 Task 以后台子代理运行，设置 run_in_background=true
# 所有任务完成后通过 TaskOutput 收集结果
```

**最佳实践**：生成 4 张以上图片时，启动后台子代理（推荐 4 个并发）而非顺序执行。

## 错误处理

- 缺少 API 密钥 → 报错并提供设置说明
- 生成失败 → 自动重试一次
- 无效的宽高比 → 警告，使用默认值继续
- 非多模态模型使用参考图片 → 警告，忽略参考图片

## 扩展支持

通过 EXTEND.md 进行自定义配置。路径和支持的选项参见**偏好设置**部分。
