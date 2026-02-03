---
name: Python 开发专家
description: Python 代码开发、审查和优化，遵循 Pythonic 风格和最佳实践
---

# Python 开发专家技能

## 代码规范

### 命名规范
- **模块/包**：`my_module.py`（小写+下划线）
- **类名**：`MyClass`（大驼峰）
- **函数/变量**：`my_function`（小写+下划线）
- **常量**：`MAX_SIZE`（全大写）
- **私有成员**：`_private_method`（单下划线前缀）

### 类型注解（推荐）
```python
def greet(name: str, age: int) -> str:
    return f"Hello {name}, you are {age} years old"

from typing import List, Dict, Optional

def process_items(items: List[str]) -> Dict[str, int]:
    return {item: len(item) for item in items}
```

## 最佳实践

### 1. 上下文管理器
```python
# 推荐
with open('file.txt', 'r') as f:
    content = f.read()
```

### 2. 列表推导式
```python
# 推荐
squares = [x**2 for x in range(10)]
```

### 3. 字符串格式化
```python
# 推荐（f-string）
message = f"Hello {name}, you are {age} years old"
```

### 4. 异常处理
```python
try:
    result = risky_operation()
except ValueError as e:
    logger.error(f"Value error: {e}")
    raise
except Exception as e:
    logger.error(f"Unexpected error: {e}")
    raise
```

### 5. 使用生成器（大数据）
```python
# 推荐（内存高效）
def read_large_file(file_path):
    with open(file_path, 'r') as f:
        for line in f:
            yield line.strip()
```

## 常见模式

### 装饰器
```python
import functools
import time

def timer(func):
    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        start = time.time()
        result = func(*args, **kwargs)
        print(f"{func.__name__} took {time.time() - start:.2f}s")
        return result
    return wrapper

@timer
def slow_function():
    time.sleep(1)
```

### 数据类（Python 3.7+）
```python
from dataclasses import dataclass

@dataclass
class User:
    name: str
    age: int
    email: str = None
    active: bool = True
```

## 性能优化

### 1. 使用内置函数
```python
# 推荐
total = sum(numbers)
maximum = max(numbers)
```

### 2. 集合查找（O(1)）
```python
# 推荐
valid_ids = {1, 2, 3, 4, 5}
if user_id in valid_ids:
    pass
```

### 3. 字符串拼接
```python
# 推荐
result = ' '.join(['Hello', 'World', 'Python'])
```

## 常见陷阱

### 1. 可变默认参数
```python
# 错误
def add_item(item, items=[]):
    items.append(item)
    return items

# 正确
def add_item(item, items=None):
    if items is None:
        items = []
    items.append(item)
    return items
```

### 2. 深拷贝 vs 浅拷贝
```python
import copy

original = [[1, 2], [3, 4]]
shallow = original.copy()      # 浅拷贝
deep = copy.deepcopy(original)  # 深拷贝
```

## 代码检查清单
- [ ] 遵循 PEP 8 规范
- [ ] 使用类型注解
- [ ] 异常处理完整
- [ ] 使用上下文管理器
- [ ] 避免可变默认参数
- [ ] 函数职责单一
- [ ] 使用 logging 而非 print

## 输出格式

```markdown
## Python 代码报告

### 代码质量
- 规范性：[PEP 8 符合度]
- 类型注解：[是否使用]

### 发现的问题
1. **[问题类型]** - 第 X 行
   - 问题：[描述]
   - 建议：[修复方案]

### 优化后代码
[完整的 Python 代码]
```

## 推荐工具
- **格式化**：black, autopep8
- **检查**：pylint, flake8, mypy
- **测试**：pytest
