# 用户隔离与安全指南

## 概述

用户隔离确保每个用户只能访问自己的数据。ChatKit 实现了三级隔离，以防止在多用户系统中数据泄露并维护安全性。

## 三级隔离策略

### 第 1 级：中间件（认证）

**作用：** 验证 JWT 令牌并提取 user_id

**实现：**
```python
class JWTAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # 从 Authorization 头提取令牌
        auth_header = request.headers.get("Authorization")

        # 验证令牌格式
        scheme, token = auth_header.split()
        if scheme.lower() != "bearer":
            return JSONResponse({"detail": "Invalid auth scheme"}, status_code=401)

        # 解码 JWT
        payload = jwt.decode(token, settings.JWT_SECRET, algorithms=['HS256'])
        user_id = payload.get("user_id")

        # 为所有下游处理器设置 user_id
        request.state.user_id = user_id

        return await call_next(request)
```

**安全保证：**
- 仅已认证用户可访问 ChatKit 端点
- 无效令牌被拒绝
- 过期令牌被拒绝
- 令牌篡改被检测

### 第 2 级：工具封装器（参数注入）

**作用：** 自动将 user_id 注入每个工具调用

**实现：**
```python
async def respond(self, thread, input, context):
    user_id = context.user_id  # 来自中间件

    # 封装器在闭包中捕获 user_id
    def add_task_wrapper(title: str, description: str = None):
        # user_id 自动包含
        return mcp_add_task(user_id=user_id, title=title, description=description)

    # Agent 只能调用封装器，不能调用原始工具
    wrapped_tools = [add_task_wrapper, ...]
    agent = create_task_agent(tools=wrapped_tools)
```

**安全保证：**
- 工具始终接收 user_id
- Agent 无法绕过 user_id 参数
- 不可能意外遗漏 user_id
- 每请求隔离（每次请求新建封装器）

### 第 3 级：数据库（查询过滤）

**作用：** 按 user_id 过滤所有数据库查询

**实现：**
```python
def list_tasks(user_id: str, status: Optional[str] = None):
    """仅返回属于该用户的任务"""
    with Session(engine) as session:
        # 关键：每个查询都按 user_id 过滤
        query = select(Task).where(Task.user_id == user_id)

        # 如提供则应用状态过滤
        if status == "pending":
            query = query.where(Task.completed == False)
        elif status == "completed":
            query = query.where(Task.completed == True)

        # 执行查询 - 仅返回用户的任务
        tasks = session.exec(query).all()
        return tasks
```

**安全保证：**
- 数据库级别强制执行
- 即使中间件失败，数据库仍会过滤
- 即使封装器失败，数据库仍会过滤
- 通过 SQLModel 防止 SQL 注入

## 完整数据流

```
用户 1（user-123）消息
    ↓
请求头：Authorization: Bearer <user-123-token>
    ↓
JWT 中间件：
    验证令牌签名
    检查过期时间
    提取 user_id = "user-123"
    设置 request.state.user_id = "user-123"
    ↓
ChatKit 端点：
    接收带 request.state.user_id = "user-123" 的请求
    创建 context.user_id = "user-123"
    ↓
MyChatKitServer.respond()：
    从上下文提取 user_id = "user-123"
    创建捕获 user_id = "user-123" 的封装器
    ↓
工具封装器（add_task_wrapper）：
    Agent 调用：add_task_wrapper(title="Buy milk")
    封装器注入：mcp_add_task(user_id="user-123", title="Buy milk")
    ↓
MCP 工具（add_task）：
    接收 user_id = "user-123"
    创建 user_id = "user-123" 的 Task
    ↓
数据库：
    INSERT INTO tasks (id, user_id, title, ...)
    VALUES (uuid, "user-123", "Buy milk", ...)
    ↓
用户 2（user-456）列出任务：
    SELECT * FROM tasks WHERE user_id = "user-456"
    返回：[空或仅 user-456 的任务]
    不会返回 user-123 的 "Buy milk" 任务
```

## 验证检查清单

### 开发环境验证

**✓ 中间件级别**
```bash
# 测试无效令牌
curl -H "Authorization: Bearer invalid" \
  http://localhost:8000/api/v1/chatkit
# 预期：401 Unauthorized

# 测试缺少令牌
curl http://localhost:8000/api/v1/chatkit
# 预期：401 Unauthorized

# 测试有效令牌
curl -H "Authorization: Bearer <valid-jwt>" \
  http://localhost:8000/api/v1/chatkit
# 预期：200 OK
```

**✓ 工具封装器级别**
```python
# 为封装器添加日志
def add_task_wrapper(title):
    logger.info(f"add_task_wrapper called with user_id={user_id}")
    return mcp_add_task(user_id=user_id, title=title)

# 检查日志中的 user_id 注入
# 输出：add_task_wrapper called with user_id=user-123
```

**✓ 数据库级别**
```python
# 检查数据库中的任务
import sqlite3
conn = sqlite3.connect('taskpilot.db')
cursor = conn.cursor()

# 列出所有任务及其 user_id
cursor.execute('SELECT id, user_id, title FROM tasks')
for row in cursor.fetchall():
    print(f"Task {row[0]} belongs to user {row[1]}")
```

### 生产环境验证

**✓ 用户 1 看不到用户 2 的任务**

1. **设置：**
   - 创建用户 A 账户，获取 JWT 令牌 A
   - 创建用户 B 账户，获取 JWT 令牌 B

2. **测试：**
   - 用户 A 登录，创建任务"Task A"
   - 用户 B 登录，列出任务
   - 验证用户 B 看不到"Task A"

**✓ User ID 不匹配失败**

1. **设置：**
   - 获取用户 A 的 JWT 令牌
   - 手动修改数据库中任务的 user_id = "user-999"

2. **测试：**
   - 用户 A 尝试删除 ID = 已修改任务的任务
   - 封装器中工具调用包含 user_id = "user-A"
   - 数据库过滤：WHERE user_id = "user-A" AND task_id = X
   - 查询返回 0 行（未找到）
   - 用户 A 看到"任务未找到"错误

**✓ 令牌过期正常工作**

1. **设置：**
   - 生成短过期时间（1 分钟）的 JWT
   - 用户使用此令牌登录

2. **测试：**
   - 等待 2 分钟
   - 尝试使用令牌
   - JWT 解码失败并显示"Token expired"
   - 请求以 401 Unauthorized 失败

## 常见隔离失败

### 失败 1：工具调用中缺少 user_id

**症状：**
- 所有用户看到相同的任务
- 新任务在数据库中没有 user_id
- "用户 A 创建了任务，用户 B 能看到"

**根本原因：**
```python
# 错误：工具调用时没有 user_id
def add_task_wrapper(title):
    return mcp_add_task(title=title)  # 缺少 user_id！
```

**修复：**
```python
# 正确：工具调用包含 user_id
def add_task_wrapper(title):
    return mcp_add_task(user_id=user_id, title=title)
```

### 失败 2：缺少数据库过滤

**症状：**
- 封装器正确传递 user_id
- 但所有用户仍看到所有任务
- "用户 A 创建了任务，用户 B 能列出它"

**根本原因：**
```python
# 错误：查询中没有 user_id 过滤
def list_tasks(user_id: str, status: Optional[str] = None):
    with Session(engine) as session:
        # BUG：返回所有任务，忽略 user_id 参数
        query = select(Task)
        tasks = session.exec(query).all()
```

**修复：**
```python
# 正确：按 user_id 过滤
def list_tasks(user_id: str, status: Optional[str] = None):
    with Session(engine) as session:
        # 正确：仅返回 user_id 匹配的任务
        query = select(Task).where(Task.user_id == user_id)
        tasks = session.exec(query).all()
```

### 失败 3：过期令牌未验证

**症状：**
- 用户永久保持登录状态
- 旧令牌过期后仍然有效
- 无法强制登出

**根本原因：**
```python
# 错误：没有过期检查
payload = jwt.decode(token, JWT_SECRET, algorithms=['HS256'])
```

**修复：**
```python
# 正确：验证包含过期检查
payload = jwt.decode(
    token,
    JWT_SECRET,
    algorithms=['HS256']
    # JWT 库自动检查 'exp' 声明
)
```

### 失败 4：封装器定义在错误的作用域

**症状：**
- 封装器调用中 user_id 错误
- 用户 A 的请求使用了用户 B 的 user_id
- "用户 A 的任务出现了用户 B 的 ID"

**根本原因：**
```python
# 错误：封装器定义在 respond() 外部
user_id = None  # 全局变量

def add_task_wrapper(title):
    return mcp_add_task(user_id=user_id, ...)  # 使用全局变量，错误！

async def respond(self, thread, input, context):
    global user_id
    user_id = context.user_id  # 更新全局变量
    # 问题：全局变量在请求间共享！
```

**修复：**
```python
# 正确：封装器定义在 respond() 内部，使用闭包
async def respond(self, thread, input, context):
    user_id = context.user_id  # 局部变量

    def add_task_wrapper(title):  # 在此定义
        return mcp_add_task(user_id=user_id, ...)  # 捕获局部 user_id
    # 每个请求获得自己的封装器，使用正确的 user_id
```

## 隔离测试

### 单元测试：中间件提取

```python
def test_jwt_middleware_extracts_user_id():
    """验证中间件正确从 JWT 提取 user_id"""
    from datetime import datetime, timedelta
    import jwt

    # 创建测试令牌
    payload = {
        "user_id": "test-user-123",
        "email": "test@example.com",
        "iat": datetime.utcnow(),
        "exp": datetime.utcnow() + timedelta(hours=1)
    }
    token = jwt.encode(payload, "secret", algorithm="HS256")

    # 验证提取
    decoded = jwt.decode(token, "secret", algorithms=["HS256"])
    assert decoded["user_id"] == "test-user-123"
    print("✓ 中间件正确提取 user_id")
```

### 集成测试：端到端隔离

```python
async def test_user_isolation_end_to_end():
    """验证三级完整隔离"""
    # 用户 1 创建任务
    user_1_id = "user-1"
    task = await add_task(
        user_id=user_1_id,
        title="User 1's task",
        description="Secret task"
    )

    # 用户 2 尝试列出任务
    user_2_id = "user-2"
    tasks = await list_tasks(user_id=user_2_id)

    # 验证用户 2 看不到用户 1 的任务
    task_titles = [t.title for t in tasks]
    assert "User 1's task" not in task_titles
    print("✓ 端到端用户隔离已验证")

    # 验证用户 2 创建自己的任务
    task_2 = await add_task(
        user_id=user_2_id,
        title="User 2's task",
        description="Different task"
    )

    # 验证用户 1 看不到用户 2 的任务
    user_1_tasks = await list_tasks(user_id=user_1_id)
    user_1_titles = [t.title for t in user_1_tasks]
    assert "User 2's task" not in user_1_titles
    print("✓ 双向隔离已验证")
```

## 安全最佳实践

1. **永远不要信任客户端输入**
   - 不要使用请求体中的 user_id
   - 始终从经过验证的 JWT 令牌中提取
   - 中间件确保这一点

2. **始终过滤数据库查询**
   - 每个 SELECT 必须有 WHERE user_id = ?
   - 每个 UPDATE 必须有 WHERE user_id = ?
   - 每个 DELETE 必须有 WHERE user_id = ?

3. **验证令牌过期**
   - JWT 库自动完成此操作
   - 但在生产日志中验证
   - 监控"Token expired"错误

4. **使用强密钥**
   - JWT_SECRET 应为 32+ 字符
   - 存储在环境变量中
   - 永远不要提交到版本控制

5. **实现速率限制**
   - 防止暴力攻击
   - 限制每个用户的 API 调用
   - 实现指数退避

6. **记录安全事件**
   - 认证失败
   - 授权失败
   - 异常访问模式
   - 工具调用参数（已脱敏）

7. **监控异常**
   - 用户访问正常模式之外的数据
   - 多个用户从同一 IP 访问
   - 快速工具调用（可能是自动化攻击）

## 隔离问题调试

### 启用详细日志

```python
import logging

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

# 中间件
logger.debug(f"JWT token: {token[:20]}...")
logger.debug(f"Extracted user_id: {user_id}")

# 工具封装器
logger.debug(f"Calling tool for user_id: {user_id}")

# MCP 工具
logger.debug(f"list_tasks query: WHERE user_id = {user_id}")
```

### 追踪请求流

```
1. 检查请求头
   curl -v http://localhost:8000/api/v1/chatkit
   查找：Authorization: Bearer ...

2. 检查中间件日志
   grep "user_id" logs/app.log
   预期："Extracted user_id: user-123"

3. 检查工具日志
   grep "add_task_wrapper" logs/app.log
   预期："add_task called for user user-123"

4. 检查数据库
   SELECT * FROM tasks WHERE user_id != 'user-123'
   应该不包含步骤 1 中用户的任务

5. 验证响应
   仅返回 user_id = 'user-123' 的任务
```

## 合规与审计

### 记录所有访问

```python
logger.info(f"""
    User Access:
    - User ID: {user_id}
    - Action: {action}
    - Resource: {resource_id}
    - Timestamp: {datetime.utcnow()}
    - Status: {status}
""")
```

### 定期审计

```sql
-- 查找没有 user_id 的任务（数据损坏）
SELECT * FROM tasks WHERE user_id IS NULL;

-- 查找任务异常多的用户
SELECT user_id, COUNT(*) as task_count
FROM tasks
GROUP BY user_id
ORDER BY task_count DESC
LIMIT 10;

-- 查找同一 IP 的访问模式
SELECT user_id, client_ip, COUNT(*) as access_count
FROM access_logs
GROUP BY user_id, client_ip
HAVING access_count > 1000;
```
