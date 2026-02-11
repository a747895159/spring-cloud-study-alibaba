---
name: nano-image-generator
description: "使用 Nano Banana Pro（Gemini 3 Pro Preview）生成图像。当需要创建应用图标、logo、UI 图形、营销横幅、社交媒体图像、插图、示意图或任何视觉素材时使用。支持参考图像进行风格迁移和角色一致性。触发短语包括'生成图像'、'创建图形'、'制作图标'、'设计 logo'、'创建横幅'、'相同风格'、'保持风格'或任何需要视觉内容的请求。"
---

# Nano Image Generator

使用 Nano Banana Pro（Gemini 3 Pro Preview）为任何视觉素材需求生成图像。支持**参考图像**进行风格迁移和角色一致性。

## 快速开始

```bash
# 基本生成
python scripts/generate_image.py "一个友好的挥手机器人吉祥物" --output ./mascot.png

# 使用风格参考（保持相同视觉风格）
python scripts/generate_image.py "相同风格，新内容" --ref ./reference.jpg --output ./new.png
```

## 脚本使用

```bash
python scripts/generate_image.py <提示词> --output <路径> [选项]
```

**必填参数：**
- `prompt` - 图像描述
- `--output, -o` - 输出文件路径

**可选参数：**
- `--aspect, -a` - 宽高比（默认：`1:1`）
  - 正方形：`1:1`
  - 竖版：`2:3`, `3:4`, `4:5`, `9:16`
  - 横版：`3:2`, `4:3`, `5:4`, `16:9`, `21:9`
- `--size, -s` - 分辨率：`1K`, `2K`（默认）, `4K`
- `--ref, -r` - 参考图像（可多次使用，最多 14 张）

## 参考图像

Gemini 支持最多 **14 张参考图像**，用于：

### 风格迁移
从参考图像中保持视觉风格（色彩、纹理、氛围）：
```bash
python scripts/generate_image.py "新的山景场景，使用与参考相同的视觉风格" \
  --ref ./style-reference.jpg --output ./styled-mountains.png
```

### 角色一致性
在多张图像中保持角色外观一致：
```bash
python scripts/generate_image.py "相同角色，现在在森林场景中" \
  --ref ./character.png --output ./character-forest.png
```

### 多图融合
组合多张参考图像中的元素：
```bash
python scripts/generate_image.py "将第一张图的风格与第二张图的主体结合" \
  --ref ./style.png --ref ./subject.png --output ./combined.png
```

### 系列图像生成（批量工作流）
用于生成风格一致的系列图像：
1. 生成第一张图像
2. 将第一张图像作为后续图像的 `--ref`
3. 每张新图像继承已建立的风格

```bash
# 生成封面
python scripts/generate_image.py "技术知识卡片封面" -o ./01-cover.png

# 使用风格参考生成后续卡片
python scripts/generate_image.py "卡片 2 内容，相同风格" --ref ./01-cover.png -o ./02-card.png
python scripts/generate_image.py "卡片 3 内容，相同风格" --ref ./01-cover.png -o ./03-card.png
```

## 工作流程

1. **确定输出位置** - 将图像放置在上下文合适的位置：
   - 应用图标 → `./assets/icons/`
   - 营销素材 → `./marketing/`
   - UI 元素 → `./src/assets/`
   - 通用 → `./generated/`

2. **编写有效的提示词** - 要具体且描述详细：
   - 包含风格："扁平设计"、"3D 渲染"、"水彩画"、"极简主义"
   - 包含上下文："用于移动应用"、"网站主图"
   - 包含细节：颜色、氛围、构图
   - 对于参考图像：提及"与参考相同风格"或"保持视觉风格"

3. **选择合适的设置：**
   - 图标/logo → `--aspect 1:1`
   - 横幅/头图 → `--aspect 16:9` 或 `21:9`
   - 手机屏幕 → `--aspect 9:16`
   - 小红书卡片 → `--aspect 3:4`
   - 照片 → `--aspect 3:2` 或 `4:3`

## 示例

**应用图标：**
```bash
python scripts/generate_image.py "极简扁平设计应用图标，闪电造型，紫色渐变背景，现代 iOS 风格" \
  --output ./assets/app-icon.png --aspect 1:1
```

**营销横幅：**
```bash
python scripts/generate_image.py "专业网站主图横幅，生产力应用，抽象几何图形，蓝白配色" \
  --output ./public/images/hero-banner.png --aspect 16:9
```

**小红书知识卡片：**
```bash
python scripts/generate_image.py "技术知识卡片，深蓝紫色渐变，霓虹青色点缀，代码块风格，中文文字'标题'" \
  --output ./xiaohongshu/card.png --aspect 3:4
```

**风格迁移：**
```bash
python scripts/generate_image.py "将这张照片转换为水彩画风格" \
  --ref ./photo.jpg --output ./watercolor.png
```

**角色场景变换：**
```bash
python scripts/generate_image.py "参考中的相同角色，现在坐在咖啡馆里，温暖的灯光" \
  --ref ./character.png --output ./character-cafe.png --aspect 3:2
```

## 提示词技巧

- **具体描述** - "木桌上的一个红苹果" 优于 "一个苹果"
- **包含风格** - "像素艺术风格" 或 "照片级真实"
- **说明用途** - "用于儿童读物" 会影响输出风格
- **描述构图** - "居中"、"三分法则"、"特写"
- **指定颜色** - 明确的色彩方案会产生更好的结果
- **参考提示** - 使用"与参考相同风格"、"保持视觉美感"、"匹配色彩方案"
- **避免** - 不要要求图像中包含复杂文字（改用叠加文字）

## 限制

- 每次请求最多 14 张参考图像
- 文字渲染可能不太完美（建议单独叠加文字）
- 非常具体的品牌 logo 可能无法精确复制
