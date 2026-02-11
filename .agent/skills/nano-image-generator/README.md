# Nano Image Generator 技能

[简体中文](./README_CN.md)

一个使用 Gemini 3 Pro Preview (Nano Banana Pro) 生成图像的 Claude Code 技能。

## 功能特点

- **文字生成图像** - 描述你想要的内容，即可获得图像
- **参考图像支持** - 风格迁移、角色一致性（最多 14 张图像）
- **多种宽高比** - 正方形、竖版、横版、电影比例
- **分辨率选项** - 1K、2K、4K 输出

## 与 [livelabs-ventures/nano-skills](https://github.com/livelabs-ventures/nano-skills) 的区别

| 功能 | 原版 | 本版本 |
|------|------|--------|
| 参考图像 | ❌ 不支持 | ✅ 最多 14 张 |
| API 密钥配置 | 环境变量 | 直接编辑代码 |

## 配置

### 1. 获取 Gemini API 密钥

访问：https://aistudio.google.com/apikey

### 2. 配置 API 密钥

编辑 `scripts/generate_image.py`，找到 `get_api_key()` 函数（约第 37 行）：

```python
def get_api_key():
    """
    Get API key.

    ⚠️ SETUP REQUIRED: Replace the placeholder below with your Gemini API key.

    Get your API key from: https://aistudio.google.com/apikey
    """
    return "YOUR_GEMINI_API_KEY_HERE"  # <-- Replace this with your API key
```

将 `YOUR_GEMINI_API_KEY_HERE` 替换为你的实际 API 密钥：

```python
    return "AIzaSy..."  # 你的实际密钥
```

## 安装

### 从本地路径安装

```bash
git clone https://github.com/YOUR_USERNAME/nano-image-generator-skill.git
```

将以下内容添加到你的 `~/.claude/settings.json`：

```json
{
  "skills": [
    "/path/to/nano-image-generator-skill"
  ]
}
```

### 从 GitHub 安装

```bash
claude skill add github:YOUR_USERNAME/nano-image-generator-skill
```

## 插件结构

```
nano-image-generator-skill/
├── SKILL.md                    # Claude Code 的技能定义
├── README.md                   # 本文件
├── README_CN.md                # 中文文档
└── scripts/
    └── generate_image.py       # 图像生成脚本（在此编辑 API 密钥）
```

## 使用方法

安装后，当你要求以下操作时，Claude Code 将自动使用此技能：

- "生成一张...的图像"
- "创建一个...的图标"
- "设计一个 logo..."
- "制作一个横幅..."
- "和这张图片相同风格..."

## 直接使用脚本

### 基本用法

```bash
python scripts/generate_image.py "一个可爱的机器人吉祥物" --output ./robot.png
```

### 指定宽高比

```bash
python scripts/generate_image.py "网站横幅" --aspect 16:9 --output ./banner.png
```

### 使用参考图像

```bash
python scripts/generate_image.py "同一角色在森林中" --ref ./character.png --output ./forest.png
```

### 多张参考图像

```bash
python scripts/generate_image.py "融合风格" --ref ./img1.png --ref ./img2.png --output ./combined.png
```

## 选项

| 选项 | 可选值 | 默认值 | 描述 |
|------|--------|--------|------|
| `--aspect`, `-a` | `1:1`, `2:3`, `3:2`, `3:4`, `4:3`, `4:5`, `5:4`, `9:16`, `16:9`, `21:9` | `1:1` | 宽高比 |
| `--size`, `-s` | `1K`, `2K`, `4K` | `2K` | 分辨率 |
| `--ref`, `-r` | 图像路径 | - | 参考图像（可重复，最多 14 张） |

## 许可证

MIT
