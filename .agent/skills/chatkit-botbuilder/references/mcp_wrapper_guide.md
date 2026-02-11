# MCP 工具封装指南

## 概述

MCP 工具封装器是 OpenAI Agents SDK 和你的 MCP 工具之间的关键桥梁。它们自动注入 `user_id` 参数，确保每个工具调用维持用户隔离。

## 问题

### 无封装器

```python
# OpenAI Agent 调用工具但不传递 user_id
agent.call_tool("add_task", {"title": "Buy milk"})

# MCP 工具期望 user_id 作为第一个参数
def add_task(user_id: str, title: str):
    # 错误：user_id 缺失！
    pass
```

**结果：** 创建的任务没有 user_id，破坏了用户隔离。

### 有封装器

```python
# OpenAI Agent 调用封装器
agent.call_tool("add_task_wrapper", {"title": "Buy milk"})

# 封装器自动注入 user_id
def add_task_wrapper(title: str):
    return add_task(user_id=user_id, title=title)  # user_id 已捕获！

# MCP 工具接收 user_id
def add_task(user_id: str, title: str):
    # user_id 可用于数据库过滤
    pass
```

**结果：** 任务使用正确的 user_id 创建，隔离得以维持。

## 封装器工作原理

### 闭包模式

封装函数从外部作用域捕获 `user_id`：

```python
async def respond(self, thread, input, context):
    # 从上下文提取 user_id
    user_id = context.user_id

    # 封装函数在闭包中捕获 user_id
    def add_task_wrapper(title: str, description: str = None):
        """封装器从外部作用域捕获 user_id"""
        return mcp_add_task(
            user_id=user_id,  # 从外部作用域捕获
            title=title,
            description=description
        )

    # 将封装器传递给代理
    agent = create_task_agent(tools=[add_task_wrapper, ...])

    # 代理仅使用 {title, description} 调用封装器
    # 封装器在调用 mcp_add_task 前添加 user_id
```

## 完整封装器集

### 1. 添加任务封装器

```python
def add_task_wrapper(title: str, description: str = None):
    """带自动用户隔离的添加任务

    Args:
        title: 任务标题（必需）
        description: 任务描述（可选）

    Returns:
        包含 id 和确认消息的已创建任务
    """
    logger.info(f"add_task called for user {user_id}")
    return mcp_add_task(user_id=user_id, title=title, description=description)
```

### 2. 列出任务封装器

```python
def list_tasks_wrapper(status: str = "all"):
    """带自动用户隔离的列出任务

    Args:
        status: 按状态过滤（'all'、'pending'、'completed'）

    Returns:
        用户的任务列表，含计数和消息
    """
    logger.info(f"list_tasks called for user {user_id}")
    return mcp_list_tasks(user_id=user_id, status=status)
```

### 3. 按名称查找任务封装器

```python
def find_task_by_name_wrapper(name: str):
    """带自动用户隔离的按名称查找任务

    Args:
        name: 要搜索的任务名称或部分名称

    Returns:
        匹配的任务，含 id、标题和描述
    """
    logger.info(f"find_task_by_name called for user {user_id}")
    return mcp_find_task_by_name(user_id=user_id, name=name)
```

### 4. 完成任务封装器

```python
def complete_task_wrapper(task_id: str):
    """带自动用户隔离的标记任务完成

    Args:
        task_id: 要标记完成的任务 ID

    Returns:
        带完成状态的已更新任务
    """
    logger.info(f"complete_task called for user {user_id}")
    return mcp_complete_task(user_id=user_id, task_id=task_id)
```

### 5. 删除任务封装器

```python
def delete_task_wrapper(task_id: str):
    """带自动用户隔离的删除任务

    Args:
        task_id: 要删除的任务 ID

    Returns:
        确认消息
    """
    logger.info(f"delete_task called for user {user_id}")
    return mcp_delete_task(user_id=user_id, task_id=task_id)
```

### 6. 更新任务封装器

```python
def update_task_wrapper(task_id: str, title: str = None, description: str = None):
    """带自动用户隔离的更新任务

    Args:
        task_id: 要更新的任务 ID
        title: 新标题（可选）
        description: 新描述（可选）

    Returns:
        带新值的已更新任务
    """
    logger.info(f"update_task called for user {user_id}")
    return mcp_update_task(
        user_id=user_id,
        task_id=task_id,
        title=title,
        description=description
    )
```

## 在 ChatKit 服务端中集成

```python
class MyChatKitServer(ChatKitServer):
    async def respond(self, thread, input, context):
        # 提取 user_id
        user_id = getattr(context, 'user_id', None)

        # 导入原始 MCP 工具
        from mcp.tools import (
            add_task as mcp_add_task,
            list_tasks as mcp_list_tasks,
            delete_task as mcp_delete_task,
            complete_task as mcp_complete_task,
            update_task as mcp_update_task,
            find_task_by_name as mcp_find_task_by_name,
        )

        # 创建闭包捕获 user_id 的封装器
        def add_task_wrapper(title: str, description: str = None):
            return mcp_add_task(user_id=user_id, title=title, description=description)

        def list_tasks_wrapper(status: str = "all"):
            return mcp_list_tasks(user_id=user_id, status=status)

        def delete_task_wrapper(task_id: str):
            return mcp_delete_task(user_id=user_id, task_id=task_id)

        def complete_task_wrapper(task_id: str):
            return mcp_complete_task(user_id=user_id, task_id=task_id)

        def update_task_wrapper(task_id: str, title: str = None, description: str = None):
            return mcp_update_task(user_id=user_id, task_id=task_id, title=title, description=description)

        def find_task_by_name_wrapper(name: str):
            return mcp_find_task_by_name(user_id=user_id, name=name)

        # 向代理注册封装的工具
        wrapped_tools = [
            add_task_wrapper,
            list_tasks_wrapper,
            delete_task_wrapper,
            complete_task_wrapper,
            update_task_wrapper,
            find_task_by_name_wrapper,
        ]

        # 使用封装后的工具创建代理
        task_agent = create_task_agent(tools=wrapped_tools)

        # respond() 方法的其余实现...
```

## 封装器为何必要

### 原因 1：OpenAI Agent SDK 设计

官方 OpenAI Agents SDK 是领域无关的。它：
- 不了解你的认证
- 不了解你的用户上下文
- 仅使用用户消息中的参数调用工具

### 原因 2：MCP 工具签名

你的 MCP 工具需要 `user_id`：

```python
def add_task(user_id: str, title: str, description: Optional[str] = None):
    # 始终期望 user_id 作为第一个参数以实现隔离
    pass
```

### 原因 3：上下文传播

User ID 需要从 HTTP 请求一路传递到数据库查询：

```
请求头（Authorization: Bearer <JWT>）
    ↓
JWT 中间件（提取 user_id）
    ↓
ChatKit 服务端（在封装器闭包中捕获 user_id）
    ↓
Agent 工具调用（封装器注入 user_id）
    ↓
MCP 工具（接收并使用 user_id）
    ↓
数据库查询（按 user_id 过滤）
```

## 编程方式创建封装器

### 模式 1：手动创建封装器（推荐）

最直接和显式的方式：

```python
def add_task_wrapper(title: str, description: str = None):
    return mcp_add_task(user_id=user_id, title=title, description=description)
```

### 模式 2：使用 functools.partial

```python
from functools import partial

add_task_wrapper = partial(mcp_add_task, user_id=user_id)
```

**注意：** functools.partial 与 agent SDK 的工具内省配合不佳。

### 模式 3：动态封装器工厂

```python
def create_wrapper(tool_func, user_id):
    def wrapper(*args, **kwargs):
        kwargs['user_id'] = user_id
        return tool_func(*args, **kwargs)
    return wrapper

add_task_wrapper = create_wrapper(mcp_add_task, user_id)
```

**最佳实践：** 使用手动创建封装器以保证清晰性。

## 测试封装器

### 单元测试示例

```python
def test_add_task_wrapper():
    """测试封装器注入 user_id"""
    user_id = "test-user-123"

    def mcp_add_task(user_id, title, description=None):
        return {"task_id": "1", "user_id": user_id, "title": title}

    def add_task_wrapper(title, description=None):
        return mcp_add_task(user_id=user_id, title=title, description=description)

    result = add_task_wrapper("Buy milk")

    assert result["user_id"] == "test-user-123"
    assert result["title"] == "Buy milk"
    print("✓ 封装器正确注入 user_id")
```

### 集成测试示例

```python
async def test_chatkit_respects_user_isolation():
    """测试 ChatKit 工具调用维持用户隔离"""
    # 设置
    user_1_id = "user-1"
    user_2_id = "user-2"

    # 用户 1 创建任务
    response_1 = await chatkit_server.respond(
        thread=thread_1,
        input=UserMessageItem(content="Create task 'Buy milk'"),
        context=create_context(user_id=user_1_id)
    )

    # 用户 2 列出任务
    response_2 = await chatkit_server.respond(
        thread=thread_2,
        input=UserMessageItem(content="List all tasks"),
        context=create_context(user_id=user_2_id)
    )

    # 验证用户 2 看不到用户 1 的任务
    assert "Buy milk" not in response_2
    print("✓ 用户隔离已维持")
```

## 封装器问题调试

### 问题 1："user_id not defined" 错误

**问题：**
```python
def add_task_wrapper(title):
    return mcp_add_task(user_id=user_id, ...)  # NameError: user_id not defined
```

**解决方案：**
确保封装器定义在拥有 `user_id` 作用域的函数内部：

```python
async def respond(self, thread, input, context):
    user_id = context.user_id  # 在此定义

    def add_task_wrapper(title):  # 在此定义封装器
        return mcp_add_task(user_id=user_id, ...)  # 现在 user_id 可访问
```

### 问题 2：Agent 未调用封装器

**问题：**
Agent 不调用封装函数。

**症状：**
- 用户消息无效果
- 日志中没有工具调用
- 聊天中缺少工具响应

**解决方案：**
- 检查代理指令中是否提到了工具
- 验证封装函数在工具列表中
- 检查函数签名是否匹配预期参数
- 添加日志以验证代理看到封装器

### 问题 3：任务中的 User ID 错误

**问题：**
任务创建时使用了错误的 user_id。

**症状：**
- 用户 A 创建的任务出现在用户 B 处
- 用户隔离被破坏

**原因：**
封装器未正确捕获 user_id。

**解决方案：**
```python
# 错误：封装器定义在 respond() 外部
def add_task_wrapper(title):  # user_id 不在作用域内
    return mcp_add_task(user_id=user_id, ...)

# 正确：封装器定义在 respond() 内部
async def respond(self, thread, input, context):
    user_id = context.user_id

    def add_task_wrapper(title):  # user_id 在闭包中捕获
        return mcp_add_task(user_id=user_id, ...)
```

## 性能考虑

### 封装器开销

- **最小**：封装器只是带参数注入的函数调用
- **无数据库查询**：仅传递参数
- **无序列化**：使用原生 Python 对象

### 优化建议

1. **每个请求创建一次封装器** - 不要在循环中重复创建
2. **在文件顶部导入工具** - 不要在封装器内部导入
3. **谨慎使用日志** - 仅记录重要事件
4. **尽可能缓存代理** - 不要为每条消息重新创建

```python
# 好：每个 respond() 创建一次封装器
async def respond(self, thread, input, context):
    user_id = context.user_id

    # 创建一次封装器
    def add_task_wrapper(title):
        return mcp_add_task(user_id=user_id, title=title)

    tools = [add_task_wrapper, ...]  # 每个请求使用一次
    agent = create_task_agent(tools=tools)

    # 为此单个请求使用代理
```

## 封装器实现检查清单

- [ ] 封装器定义在 respond() 方法内
- [ ] 在定义封装器前从上下文提取 user_id
- [ ] 所有 MCP 工具都用 user_id 注入进行封装
- [ ] 封装器有清晰的文档字符串
- [ ] 封装器参数名与 MCP 工具名匹配
- [ ] 封装器签名与代理期望匹配
- [ ] 封装器传递给 create_task_agent()
- [ ] 添加了日志以追踪工具调用
- [ ] 在测试中验证了用户隔离
- [ ] 性能可接受（< 100ms 开销）
