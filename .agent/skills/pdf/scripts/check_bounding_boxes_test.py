import unittest
import json
import io
from check_bounding_boxes import get_bounding_box_messages


# 目前此测试不在 CI 中自动运行；仅用于文档和手动检查。
class TestGetBoundingBoxMessages(unittest.TestCase):
    
    def create_json_stream(self, data):
        """辅助方法：从数据创建 JSON 流"""
        return io.StringIO(json.dumps(data))
    
    def test_no_intersections(self):
        """测试用例：无边界框相交"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 30]
                },
                {
                    "description": "Email",
                    "page_number": 1,
                    "label_bounding_box": [10, 40, 50, 60],
                    "entry_bounding_box": [60, 40, 150, 60]
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("成功" in msg for msg in messages))
        self.assertFalse(any("失败" in msg for msg in messages))
    
    def test_label_entry_intersection_same_field(self):
        """测试：同一字段的标签和输入框相交"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 60, 30],
                    "entry_bounding_box": [50, 10, 150, 30]  # 与标签重叠
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("失败" in msg and "相交" in msg for msg in messages))
        self.assertFalse(any("成功" in msg for msg in messages))
    
    def test_intersection_between_different_fields(self):
        """测试：不同字段的边界框相交"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 30]
                },
                {
                    "description": "Email",
                    "page_number": 1,
                    "label_bounding_box": [40, 20, 80, 40],  # 与 Name 的框重叠
                    "entry_bounding_box": [160, 10, 250, 30]
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("失败" in msg and "相交" in msg for msg in messages))
        self.assertFalse(any("成功" in msg for msg in messages))
    
    def test_different_pages_no_intersection(self):
        """测试：不同页面的框不算相交"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 30]
                },
                {
                    "description": "Email",
                    "page_number": 2,
                    "label_bounding_box": [10, 10, 50, 30],  # 相同坐标但不同页面
                    "entry_bounding_box": [60, 10, 150, 30]
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("成功" in msg for msg in messages))
        self.assertFalse(any("失败" in msg for msg in messages))
    
    def test_entry_height_too_small(self):
        """测试：输入框高度与字体大小的检查"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 20],  # 高度为 10
                    "entry_text": {
                        "font_size": 14  # 字体大小超过高度
                    }
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("失败" in msg and "高度" in msg for msg in messages))
        self.assertFalse(any("成功" in msg for msg in messages))
    
    def test_entry_height_adequate(self):
        """测试：足够的输入框高度应通过"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 30],  # 高度为 20
                    "entry_text": {
                        "font_size": 14  # 字体大小小于高度
                    }
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("成功" in msg for msg in messages))
        self.assertFalse(any("失败" in msg for msg in messages))
    
    def test_default_font_size(self):
        """测试：未指定时使用默认字体大小"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 20],  # 高度为 10
                    "entry_text": {}  # 未指定 font_size，应使用默认值 14
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("失败" in msg and "高度" in msg for msg in messages))
        self.assertFalse(any("成功" in msg for msg in messages))
    
    def test_no_entry_text(self):
        """测试：缺少 entry_text 时不触发高度检查"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [60, 10, 150, 20]  # 高度较小但无 entry_text
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("成功" in msg for msg in messages))
        self.assertFalse(any("失败" in msg for msg in messages))
    
    def test_multiple_errors_limit(self):
        """测试：错误消息数量限制以防止过多输出"""
        fields = []
        # 创建大量重叠字段
        for i in range(25):
            fields.append({
                "description": f"Field{i}",
                "page_number": 1,
                "label_bounding_box": [10, 10, 50, 30],  # 全部重叠
                "entry_bounding_box": [20, 15, 60, 35]   # 全部重叠
            })
        
        data = {"form_fields": fields}
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        # 应在约 20 条消息后中止
        self.assertTrue(any("中止" in msg for msg in messages))
        # 应有一些失败消息但不会有数百条
        failure_count = sum(1 for msg in messages if "失败" in msg)
        self.assertGreater(failure_count, 0)
        self.assertLess(len(messages), 30)  # 应有限制
    
    def test_edge_touching_boxes(self):
        """测试：边缘接触的框不算相交"""
        data = {
            "form_fields": [
                {
                    "description": "Name",
                    "page_number": 1,
                    "label_bounding_box": [10, 10, 50, 30],
                    "entry_bounding_box": [50, 10, 150, 30]  # 在 x=50 处接触
                }
            ]
        }
        
        stream = self.create_json_stream(data)
        messages = get_bounding_box_messages(stream)
        self.assertTrue(any("成功" in msg for msg in messages))
        self.assertFalse(any("失败" in msg for msg in messages))
    

if __name__ == '__main__':
    unittest.main()
