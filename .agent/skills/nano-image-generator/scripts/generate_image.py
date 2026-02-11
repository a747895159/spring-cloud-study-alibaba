#!/usr/bin/env python3
"""
Nano Image Generator - 使用 Google Gemini 3 Pro Preview API 生成图像。

前置要求：在下方 get_api_key() 函数中设置你的 GEMINI_API_KEY。

用法：
    python generate_image.py "一个可爱的机器人吉祥物" --output ./mascot.png
    python generate_image.py "应用发布横幅" --aspect 16:9 --output ./banner.png
    python generate_image.py "高分辨率 Logo" --size 4K --output ./logo.png

    # 使用参考图像（风格迁移、角色一致性）：
    python generate_image.py "同一角色在森林中" --ref ./character.png --output ./forest.png
    python generate_image.py "转换为水彩风格" --ref ./photo.jpg --output ./watercolor.png
    python generate_image.py "合并这两张" --ref ./img1.png --ref ./img2.png --output ./combined.png
"""

import argparse
import base64
import json
import os
import sys
import urllib.request
import urllib.error
from pathlib import Path


# Gemini 3 Pro Preview - "Nano Banana Pro" 模型
MODEL_ID = "gemini-3-pro-image-preview"

ASPECT_RATIOS = ["1:1", "2:3", "3:2", "3:4", "4:3", "4:5", "5:4", "9:16", "16:9", "21:9"]
IMAGE_SIZES = ["1K", "2K", "4K"]

API_BASE = "https://generativelanguage.googleapis.com/v1beta/models"


def get_api_key():
    """
    获取 API 密钥。

    ⚠️ 需要设置：请将下方占位符替换为你的 Gemini API 密钥。

    在此获取 API 密钥：https://aistudio.google.com/apikey
    """
    return "YOUR_GEMINI_API_KEY_HERE"  # <-- 将此替换为你的 API 密钥


def detect_image_format(image_bytes: bytes) -> tuple[str, str]:
    """
    通过魔术字节检测实际图像格式。
    返回：(mime_type, 扩展名)

    Gemini API 有时会报告不正确的 MIME 类型，因此我们从图像数据本身验证实际格式。
    """
    if image_bytes[:8] == b'\x89PNG\r\n\x1a\n':
        return "image/png", ".png"
    elif image_bytes[:2] == b'\xff\xd8':
        return "image/jpeg", ".jpg"
    elif image_bytes[:4] == b'RIFF' and image_bytes[8:12] == b'WEBP':
        return "image/webp", ".webp"
    elif image_bytes[:6] in (b'GIF87a', b'GIF89a'):
        return "image/gif", ".gif"
    else:
        # 未知格式默认使用 PNG
        return "image/png", ".png"


def load_image_as_base64(image_path: str) -> tuple[str, str]:
    """
    加载图像文件并返回 (base64_data, mime_type)。
    """
    path = Path(image_path)
    if not path.exists():
        print(f"错误：参考图像未找到：{image_path}", file=sys.stderr)
        sys.exit(1)

    image_bytes = path.read_bytes()
    mime_type, _ = detect_image_format(image_bytes)
    base64_data = base64.b64encode(image_bytes).decode("utf-8")
    return base64_data, mime_type


def generate_image(
    prompt: str,
    aspect_ratio: str = "1:1",
    image_size: str = "2K",
    reference_images: list[str] | None = None,
) -> tuple[bytes, str]:
    """
    使用 Gemini 3 Pro Preview API 生成图像。

    Args:
        prompt: 图像的文本描述
        aspect_ratio: 输出宽高比
        image_size: 输出分辨率（1K、2K、4K）
        reference_images: 参考图像路径列表（最多 14 张）

    Returns: (image_bytes, mime_type)
    """
    api_key = get_api_key()

    if api_key == "YOUR_GEMINI_API_KEY_HERE":
        print("错误：请在 scripts/generate_image.py 中设置你的 API 密钥", file=sys.stderr)
        print("编辑 get_api_key() 函数并替换 YOUR_GEMINI_API_KEY_HERE", file=sys.stderr)
        sys.exit(1)

    url = f"{API_BASE}/{MODEL_ID}:generateContent?key={api_key}"

    # 构建 parts 列表 - 文本提示词在前，参考图像在后
    parts = [{"text": prompt}]

    # 添加参考图像（如提供）
    if reference_images:
        if len(reference_images) > 14:
            print("警告：最多支持 14 张参考图像，使用前 14 张", file=sys.stderr)
            reference_images = reference_images[:14]

        for img_path in reference_images:
            base64_data, mime_type = load_image_as_base64(img_path)
            parts.append({
                "inlineData": {
                    "mimeType": mime_type,
                    "data": base64_data
                }
            })
            print(f"已添加参考图像：{img_path}", file=sys.stderr)

    # 按 Gemini 3 Pro Preview 规范构建请求负载
    payload = {
        "contents": [{"parts": parts}],
        "generationConfig": {
            "responseModalities": ["TEXT", "IMAGE"],
            "imageConfig": {
                "aspectRatio": aspect_ratio,
                "imageSize": image_size,
            },
        },
    }

    # 发送请求
    headers = {"Content-Type": "application/json"}
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(url, data=data, headers=headers, method="POST")

    try:
        with urllib.request.urlopen(req, timeout=180) as response:
            result = json.loads(response.read().decode("utf-8"))
    except urllib.error.HTTPError as e:
        error_body = e.read().decode("utf-8")
        print(f"API 错误 ({e.code})：{error_body}", file=sys.stderr)
        sys.exit(1)
    except urllib.error.URLError as e:
        print(f"网络错误：{e.reason}", file=sys.stderr)
        sys.exit(1)

    # 从响应中提取图像
    candidates = result.get("candidates", [])
    if not candidates:
        print("错误：响应中无候选结果", file=sys.stderr)
        print(f"响应：{json.dumps(result, indent=2)}", file=sys.stderr)
        sys.exit(1)

    parts = candidates[0].get("content", {}).get("parts", [])

    for part in parts:
        if "inlineData" in part:
            inline_data = part["inlineData"]
            image_bytes = base64.b64decode(inline_data["data"])
            # 通过魔术字节检测实际格式（API 的 mime_type 可能不正确）
            actual_mime, _ = detect_image_format(image_bytes)
            reported_mime = inline_data.get("mimeType", "image/png")
            if actual_mime != reported_mime:
                print(f"注意：API 报告 {reported_mime}，实际格式为 {actual_mime}", file=sys.stderr)
            return image_bytes, actual_mime

    # 未找到图像 - 检查文本响应
    for part in parts:
        if "text" in part:
            print(f"模型响应（无图像）：{part['text']}", file=sys.stderr)

    print("错误：响应中无图像数据", file=sys.stderr)
    sys.exit(1)


def get_extension(mime_type: str) -> str:
    """从 MIME 类型获取文件扩展名。"""
    extensions = {
        "image/png": ".png",
        "image/jpeg": ".jpg",
        "image/webp": ".webp",
        "image/gif": ".gif",
    }
    return extensions.get(mime_type, ".png")


def main():
    parser = argparse.ArgumentParser(
        description="使用 Gemini 3 Pro Preview (Nano Banana Pro) 生成图像",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例：
  %(prog)s "一个友好的机器人吉祥物" --output ./robot.png
  %(prog)s "网站横幅" --aspect 16:9 --output ./banner.png
  %(prog)s "详细风景画" --size 4K --output ./landscape.png

  # 使用参考图像（风格迁移、角色一致性）：
  %(prog)s "同一角色在森林中" --ref ./char.png -o ./forest.png
  %(prog)s "转换为水彩风格" --ref ./photo.jpg -o ./watercolor.png
  %(prog)s "合并风格" --ref ./img1.png --ref ./img2.png -o ./combined.png
        """,
    )
    parser.add_argument("prompt", help="图像描述/提示词")
    parser.add_argument(
        "--output", "-o",
        required=True,
        help="输出文件路径（缺少扩展名时自动添加）",
    )
    parser.add_argument(
        "--aspect", "-a",
        choices=ASPECT_RATIOS,
        default="1:1",
        help="宽高比。默认：1:1",
    )
    parser.add_argument(
        "--size", "-s",
        choices=IMAGE_SIZES,
        default="2K",
        help="图像分辨率：1K、2K 或 4K。默认：2K",
    )
    parser.add_argument(
        "--ref", "-r",
        action="append",
        dest="reference_images",
        metavar="IMAGE",
        help="用于风格迁移或角色一致性的参考图像（可多次使用，最多 14 张）",
    )

    args = parser.parse_args()

    print(f"正在使用 Gemini 3 Pro Preview 生成图像...", file=sys.stderr)
    print(f"提示词：{args.prompt}", file=sys.stderr)
    print(f"宽高比：{args.aspect}，分辨率：{args.size}", file=sys.stderr)
    if args.reference_images:
        print(f"参考图像：{len(args.reference_images)} 张", file=sys.stderr)

    image_bytes, mime_type = generate_image(
        prompt=args.prompt,
        aspect_ratio=args.aspect,
        image_size=args.size,
        reference_images=args.reference_images,
    )

    # 确定输出路径 - 始终使用实际格式的正确扩展名
    output_path = Path(args.output)
    correct_ext = get_extension(mime_type)

    # 将用户指定的扩展名替换为正确的
    output_path = output_path.with_suffix(correct_ext)

    if args.output != str(output_path):
        print(f"注意：使用 {correct_ext} 扩展名（实际格式：{mime_type}）", file=sys.stderr)

    # 如需创建父目录
    output_path.parent.mkdir(parents=True, exist_ok=True)

    # 写入图像
    output_path.write_bytes(image_bytes)

    print(f"图像已保存：{output_path}", file=sys.stderr)
    # 将路径输出到 stdout 以便捕获
    print(output_path)


if __name__ == "__main__":
    main()
