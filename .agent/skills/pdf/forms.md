**重要：你必须按顺序完成以下步骤。不要跳过任何步骤直接编写代码。**

如果需要填写 PDF 表单，首先检查 PDF 是否具有可填写的表单字段。从本文件目录运行以下脚本：
 `python scripts/check_fillable_fields <file.pdf>`，根据结果转到"可填写字段"或"不可填写字段"部分并按照说明操作。

# 可填写字段
如果 PDF 具有可填写的表单字段：
- 从本文件目录运行以下脚本：`python scripts/extract_form_field_info.py <input.pdf> <field_info.json>`。它将创建一个 JSON 文件，包含以下格式的字段列表：
```
[
  {
    "field_id": (字段的唯一 ID),
    "page": (页码，从 1 开始),
    "rect": ([左, 下, 右, 上] PDF 坐标的边界框，y=0 为页面底部),
    "type": ("text", "checkbox", "radio_group", 或 "choice"),
  },
  // 复选框具有 "checked_value" 和 "unchecked_value" 属性：
  {
    "field_id": (字段的唯一 ID),
    "page": (页码，从 1 开始),
    "type": "checkbox",
    "checked_value": (设置此值以选中复选框),
    "unchecked_value": (设置此值以取消选中复选框),
  },
  // 单选按钮组具有 "radio_options" 列表。
  {
    "field_id": (字段的唯一 ID),
    "page": (页码，从 1 开始),
    "type": "radio_group",
    "radio_options": [
      {
        "value": (设置此值以选择此单选选项),
        "rect": (此选项单选按钮的边界框)
      },
      // 其他单选选项
    ]
  },
  // 多选字段具有 "choice_options" 列表：
  {
    "field_id": (字段的唯一 ID),
    "page": (页码，从 1 开始),
    "type": "choice",
    "choice_options": [
      {
        "value": (设置此值以选择此选项),
        "text": (选项的显示文本)
      },
    ],
  }
]
```
- 将 PDF 转换为 PNG（每页一张图像）：
`python scripts/convert_pdf_to_images.py <file.pdf> <output_directory>`
然后分析图像以确定每个表单字段的用途（确保将边界框 PDF 坐标转换为图像坐标）。
- 创建 `field_values.json` 文件，格式如下：
```
[
  {
    "field_id": "last_name",
    "description": "用户的姓氏",
    "page": 1,
    "value": "Simpson"
  },
  {
    "field_id": "Checkbox12",
    "description": "如果用户年满 18 岁则选中的复选框",
    "page": 1,
    "value": "/On"
  },
]
```
- 运行填写脚本：
`python scripts/fill_fillable_fields.py <input pdf> <field_values.json> <output pdf>`

# 不可填写字段
如果 PDF 没有可填写的表单字段，需要通过视觉方式确定数据位置并创建文本注释。请**严格**按照以下步骤操作。

## 步骤 1：视觉分析（必需）
- 将 PDF 转换为 PNG 图像：
`python scripts/convert_pdf_to_images.py <file.pdf> <output_directory>`
- 仔细检查每个 PNG 图像，识别所有表单字段。标签和输入边界框**不得重叠**。

表单结构示例：

*标签位于框内*
```
┌────────────────────────┐
│ 姓名：                  │
└────────────────────────┘
```

*标签在线条前*
```
邮箱：_______________________
```

*标签在线条下方*
```
_________________________
姓名
```

*标签在线条上方*
```
请输入特殊要求：
________________________________________________
```

*复选框*
```
你是公民吗？是 □  否 □
```
输入边界框应仅覆盖小方块，而不是文本标签。

### 步骤 2：创建 fields.json 和验证图像（必需）
创建 `fields.json` 文件，格式如下：
```
{
  "pages": [
    { "page_number": 1, "image_width": (宽度像素), "image_height": (高度像素) }
  ],
  "form_fields": [
    {
      "page_number": 1,
      "description": "用户姓氏输入位置",
      "field_label": "姓氏",
      "label_bounding_box": [30, 125, 95, 142],
      "entry_bounding_box": [100, 125, 280, 142],
      "entry_text": { "text": "Johnson", "font_size": 14, "font_color": "000000" }
    },
    {
      "page_number": 2,
      "description": "年满 18 岁复选框",
      "entry_bounding_box": [140, 525, 155, 540],
      "field_label": "是",
      "label_bounding_box": [100, 525, 132, 540],
      "entry_text": { "text": "X" }
    }
  ]
}
```

创建验证图像：
`python scripts/create_validation_image.py <page_number> <fields.json路径> <输入图像路径> <输出图像路径>`

### 步骤 3：验证边界框（必需）
- 运行自动检查：`python scripts/check_bounding_boxes.py <JSON 文件>`
- **手动检查验证图像**：红色矩形仅覆盖输入区域，蓝色矩形包含标签文本

### 步骤 4：向 PDF 添加注释
`python scripts/fill_pdf_form_with_annotations.py <输入PDF> <fields.json路径> <输出PDF>`
