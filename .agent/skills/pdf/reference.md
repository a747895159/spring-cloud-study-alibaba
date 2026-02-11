# PDF 处理高级参考

本文档包含主要技能说明中未涵盖的高级 PDF 处理功能、详细示例和其他库。

## pypdfium2 库（Apache/BSD 许可证）

### 概述
pypdfium2 是 PDFium（Chromium 的 PDF 库）的 Python 绑定。非常适合快速 PDF 渲染、图像生成，可作为 PyMuPDF 的替代品。

### 将 PDF 渲染为图像
```python
import pypdfium2 as pdfium
from PIL import Image

# 加载 PDF
pdf = pdfium.PdfDocument("document.pdf")

# 将页面渲染为图像
page = pdf[0]  # 第一页
bitmap = page.render(
    scale=2.0,  # 更高分辨率
    rotation=0  # 不旋转
)

# 转换为 PIL Image
img = bitmap.to_pil()
img.save("page_1.png", "PNG")

# 处理多页
for i, page in enumerate(pdf):
    bitmap = page.render(scale=1.5)
    img = bitmap.to_pil()
    img.save(f"page_{i+1}.jpg", "JPEG", quality=90)
```

### 使用 pypdfium2 提取文本
```python
import pypdfium2 as pdfium

pdf = pdfium.PdfDocument("document.pdf")
for i, page in enumerate(pdf):
    text = page.get_text()
    print(f"第 {i+1} 页文本长度：{len(text)} 字符")
```

## JavaScript 库

### pdf-lib（MIT 许可证）

pdf-lib 是一个强大的 JavaScript 库，用于在任何 JavaScript 环境中创建和修改 PDF 文档。

#### 加载和操作现有 PDF
```javascript
import { PDFDocument } from 'pdf-lib';
import fs from 'fs';

async function manipulatePDF() {
    // 加载现有 PDF
    const existingPdfBytes = fs.readFileSync('input.pdf');
    const pdfDoc = await PDFDocument.load(existingPdfBytes);

    // 获取页数
    const pageCount = pdfDoc.getPageCount();
    console.log(`文档有 ${pageCount} 页`);

    // 添加新页面
    const newPage = pdfDoc.addPage([600, 400]);
    newPage.drawText('由 pdf-lib 添加', { x: 100, y: 300, size: 16 });

    // 保存修改后的 PDF
    const pdfBytes = await pdfDoc.save();
    fs.writeFileSync('modified.pdf', pdfBytes);
}
```

#### 从头创建复杂 PDF
```javascript
import { PDFDocument, rgb, StandardFonts } from 'pdf-lib';
import fs from 'fs';

async function createPDF() {
    const pdfDoc = await PDFDocument.create();
    const helveticaFont = await pdfDoc.embedFont(StandardFonts.Helvetica);
    const helveticaBold = await pdfDoc.embedFont(StandardFonts.HelveticaBold);

    const page = pdfDoc.addPage([595, 842]); // A4 尺寸
    const { width, height } = page.getSize();

    page.drawText('发票 #12345', {
        x: 50, y: height - 50, size: 18,
        font: helveticaBold, color: rgb(0.2, 0.2, 0.8)
    });

    page.drawRectangle({
        x: 40, y: height - 100, width: width - 80, height: 30,
        color: rgb(0.9, 0.9, 0.9)
    });

    const items = [
        ['项目', '数量', '价格', '总计'],
        ['小部件', '2', '$50', '$100'],
        ['小配件', '1', '$75', '$75']
    ];

    let yPos = height - 150;
    items.forEach(row => {
        let xPos = 50;
        row.forEach(cell => {
            page.drawText(cell, { x: xPos, y: yPos, size: 12, font: helveticaFont });
            xPos += 120;
        });
        yPos -= 25;
    });

    const pdfBytes = await pdfDoc.save();
    fs.writeFileSync('created.pdf', pdfBytes);
}
```

#### 高级合并和拆分操作
```javascript
import { PDFDocument } from 'pdf-lib';
import fs from 'fs';

async function mergePDFs() {
    const mergedPdf = await PDFDocument.create();
    const pdf1Bytes = fs.readFileSync('doc1.pdf');
    const pdf2Bytes = fs.readFileSync('doc2.pdf');
    const pdf1 = await PDFDocument.load(pdf1Bytes);
    const pdf2 = await PDFDocument.load(pdf2Bytes);

    // 复制第一个 PDF 的所有页面
    const pdf1Pages = await mergedPdf.copyPages(pdf1, pdf1.getPageIndices());
    pdf1Pages.forEach(page => mergedPdf.addPage(page));

    // 复制第二个 PDF 的特定页面（第 0、2、4 页）
    const pdf2Pages = await mergedPdf.copyPages(pdf2, [0, 2, 4]);
    pdf2Pages.forEach(page => mergedPdf.addPage(page));

    const mergedPdfBytes = await mergedPdf.save();
    fs.writeFileSync('merged.pdf', mergedPdfBytes);
}
```

### pdfjs-dist（Apache 许可证）

PDF.js 是 Mozilla 的 JavaScript 库，用于在浏览器中渲染 PDF。

#### 基本 PDF 加载和渲染
```javascript
import * as pdfjsLib from 'pdfjs-dist';

pdfjsLib.GlobalWorkerOptions.workerSrc = './pdf.worker.js';

async function renderPDF() {
    const loadingTask = pdfjsLib.getDocument('document.pdf');
    const pdf = await loadingTask.promise;
    console.log(`已加载包含 ${pdf.numPages} 页的 PDF`);

    const page = await pdf.getPage(1);
    const viewport = page.getViewport({ scale: 1.5 });

    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    await page.render({ canvasContext: context, viewport: viewport }).promise;
    document.body.appendChild(canvas);
}
```

#### 带坐标的文本提取
```javascript
import * as pdfjsLib from 'pdfjs-dist';

async function extractText() {
    const loadingTask = pdfjsLib.getDocument('document.pdf');
    const pdf = await loadingTask.promise;
    let fullText = '';

    for (let i = 1; i <= pdf.numPages; i++) {
        const page = await pdf.getPage(i);
        const textContent = await page.getTextContent();
        const pageText = textContent.items.map(item => item.str).join(' ');
        fullText += `\n--- 第 ${i} 页 ---\n${pageText}`;
    }
    return fullText;
}
```

## 高级命令行操作

### poppler-utils 高级功能

```bash
# 提取带边界框坐标的文本
pdftotext -bbox-layout document.pdf output.xml

# 高分辨率转换为 PNG
pdftoppm -png -r 300 document.pdf output_prefix

# 转换特定页面范围
pdftoppm -png -r 600 -f 1 -l 3 document.pdf high_res_pages

# 转换为 JPEG 并指定质量
pdftoppm -jpeg -jpegopt quality=85 -r 200 document.pdf jpeg_output

# 提取所有嵌入图像及元数据
pdfimages -j -p document.pdf page_images

# 列出图像信息
pdfimages -list document.pdf
```

### qpdf 高级功能

```bash
# 每 3 页拆分
qpdf --split-pages=3 input.pdf output_group_%02d.pdf

# 复杂页面范围提取
qpdf input.pdf --pages input.pdf 1,3-5,8,10-end -- extracted.pdf

# 从多个 PDF 合并特定页面
qpdf --empty --pages doc1.pdf 1-3 doc2.pdf 5-7 doc3.pdf 2,4 -- combined.pdf

# 优化（线性化以供流式传输）
qpdf --linearize input.pdf optimized.pdf

# 压缩
qpdf --optimize-level=all input.pdf compressed.pdf

# 修复损坏的 PDF
qpdf --check input.pdf
qpdf --fix-qdf damaged.pdf repaired.pdf

# 高级加密
qpdf --encrypt user_pass owner_pass 256 --print=none --modify=none -- input.pdf encrypted.pdf
```

## 高级 Python 技术

### pdfplumber 高级功能

```python
import pdfplumber

with pdfplumber.open("document.pdf") as pdf:
    page = pdf.pages[0]
    
    # 提取带坐标的文本
    chars = page.chars
    for char in chars[:10]:
        print(f"字符：'{char['text']}' 位于 x:{char['x0']:.1f} y:{char['y0']:.1f}")
    
    # 按边界框提取文本（左, 上, 右, 下）
    bbox_text = page.within_bbox((100, 100, 400, 200)).extract_text()
```

### 自定义设置的高级表格提取
```python
import pdfplumber
import pandas as pd

with pdfplumber.open("complex_table.pdf") as pdf:
    page = pdf.pages[0]
    table_settings = {
        "vertical_strategy": "lines",
        "horizontal_strategy": "lines",
        "snap_tolerance": 3,
        "intersection_tolerance": 15
    }
    tables = page.extract_tables(table_settings)
    
    # 可视化调试
    img = page.to_image(resolution=150)
    img.save("debug_layout.png")
```

### reportlab 高级 - 专业报告表格
```python
from reportlab.platypus import SimpleDocTemplate, Table, TableStyle, Paragraph
from reportlab.lib.styles import getSampleStyleSheet
from reportlab.lib import colors

data = [
    ['产品', 'Q1', 'Q2', 'Q3', 'Q4'],
    ['小部件', '120', '135', '142', '158'],
    ['小配件', '85', '92', '98', '105']
]

doc = SimpleDocTemplate("report.pdf")
elements = []
styles = getSampleStyleSheet()
elements.append(Paragraph("季度销售报告", styles['Title']))

table = Table(data)
table.setStyle(TableStyle([
    ('BACKGROUND', (0, 0), (-1, 0), colors.grey),
    ('TEXTCOLOR', (0, 0), (-1, 0), colors.whitesmoke),
    ('ALIGN', (0, 0), (-1, -1), 'CENTER'),
    ('FONTNAME', (0, 0), (-1, 0), 'Helvetica-Bold'),
    ('GRID', (0, 0), (-1, -1), 1, colors.black)
]))
elements.append(table)
doc.build(elements)
```

## 批量处理与错误处理
```python
import os, glob, logging
from pypdf import PdfReader, PdfWriter

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def batch_process_pdfs(input_dir, operation='merge'):
    pdf_files = glob.glob(os.path.join(input_dir, "*.pdf"))
    
    if operation == 'merge':
        writer = PdfWriter()
        for pdf_file in pdf_files:
            try:
                reader = PdfReader(pdf_file)
                for page in reader.pages:
                    writer.add_page(page)
                logger.info(f"已处理：{pdf_file}")
            except Exception as e:
                logger.error(f"处理 {pdf_file} 失败：{e}")
                continue
        with open("batch_merged.pdf", "wb") as output:
            writer.write(output)
    
    elif operation == 'extract_text':
        for pdf_file in pdf_files:
            try:
                reader = PdfReader(pdf_file)
                text = "".join(page.extract_text() for page in reader.pages)
                output_file = pdf_file.replace('.pdf', '.txt')
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(text)
                logger.info(f"已从 {pdf_file} 提取文本")
            except Exception as e:
                logger.error(f"从 {pdf_file} 提取文本失败：{e}")
```

## 性能优化提示

1. **大型 PDF**：使用流式方法；用 `qpdf --split-pages` 拆分大文件
2. **文本提取**：`pdftotext -bbox-layout` 最快；pdfplumber 适合结构化数据和表格
3. **图像提取**：`pdfimages` 比渲染页面快得多
4. **表单填写**：pdf-lib 保持表单结构最好
5. **内存管理**：分块处理大型 PDF

## 故障排除

### 加密 PDF
```python
from pypdf import PdfReader
try:
    reader = PdfReader("encrypted.pdf")
    if reader.is_encrypted:
        reader.decrypt("password")
except Exception as e:
    print(f"解密失败：{e}")
```

### 损坏的 PDF
```bash
qpdf --check corrupted.pdf
qpdf --replace-input corrupted.pdf
```

### 文本提取问题 - OCR 回退
```python
import pytesseract
from pdf2image import convert_from_path

def extract_text_with_ocr(pdf_path):
    images = convert_from_path(pdf_path)
    return "".join(pytesseract.image_to_string(img) for img in images)
```

## 许可信息

| 库 | 许可证 |
|----|--------|
| pypdf | BSD |
| pdfplumber | MIT |
| pypdfium2 | Apache/BSD |
| reportlab | BSD |
| poppler-utils | GPL-2 |
| qpdf | Apache |
| pdf-lib | MIT |
| pdfjs-dist | Apache |