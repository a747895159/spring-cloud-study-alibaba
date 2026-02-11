import sys
from pypdf import PdfReader


# 供 Claude 运行的脚本，用于判断 PDF 是否包含可填写的表单字段。参见 forms.md。


reader = PdfReader(sys.argv[1])
if (reader.get_fields()):
    print("此 PDF 包含可填写的表单字段")
else:
    print("此 PDF 不包含可填写的表单字段；你需要通过视觉方式确定数据输入位置")
