---
name: chatkit-botbuilder
description: 创建生产级 ChatKit 聊天机器人的指南，该机器人将 OpenAI Agents SDK 与 MCP 工具和自定义后端集成。在为任何应用程序构建具有专门功能、实时任务执行和用户隔离的 AI 驱动聊天机器人时使用。
license: 完整条款见 LICENSE.txt
---

# ChatKit 机器人构建器

## 概述

使用 OpenAI ChatKit 框架创建生产级聊天机器人。此技能支持构建以下类型的聊天机器人：

- **集成 AI 代理**：使用 OpenAI Agents SDK 进行智能对话处理
- **执行工具**：连接 MCP（模型上下文协议）工具执行实际任务
- **支持自定义后端**：构建具有完整协议支持的 FastAPI 后端
- **确保用户隔离**：实现带 JWT 认证的多用户系统
- **实时同步**：当聊天机器人执行操作时实现实时 UI 更新
- **灵活部署**：部署到 Web、移动端或桌面应用

本技能提供从前端配置到后端服务器实现的完整 ChatKit 集成架构模式。

---

## 使用场景

当你需要以下功能时使用此技能：

1. **构建任务管理聊天机器人** - 创建任务创建、更新、完成的会话界面
2. **将 AI 集成到现有应用** - 将 ChatKit 添加到仪表板、Web 应用或平台
3. **创建专业 AI 助手** - 构建具有自定义工具集成的领域特定聊天机器人
4. **实现多用户聊天机器人** - 创建每个用户拥有隔离对话和数据的系统
5. **添加实时功能** - 构建触发实际应用变更的聊天机器人
6. **部署 AI 对话** - 创建与数据库和 API 交互的聊天机器人

---

## 架构概览

### 高层流程

```
用户消息
    ↓
ChatKit 前端 (React/Next.js)
    ↓ [Authorization Header 中的 JWT Token]
    ↓
FastAPI 后端 (ChatKit Server)
    ↓ [从 JWT 提取 user_id]
    ↓
OpenAI Agent (Agents SDK)
    ↓ [需要执行工具]
    ↓
MCP 工具 (自定义工具函数)
    ↓ [创建/更新/列出数据]
    ↓
数据库 (用户隔离数据)
    ↓
响应 → ChatKit → 前端 → 用户
```

### 关键组件

1. **前端 (Next.js + ChatKit SDK)**
   - 带对话历史的 ChatKit UI 组件
   - localStorage 中的 JWT 令牌管理
   - 带 Bearer 令牌认证的自定义 fetch 包装器
   - 实时自动刷新以同步后端变更

2. **后端 (FastAPI + ChatKit Server)**
   - ChatKit 协议端点处理请求
   - MyChatKitServer 类继承 ChatKitServer
   - 通过 JWT 中间件实现用户隔离
   - 工具包装函数自动注入 user_id

3. **代理 (OpenAI Agents SDK)**
   - 带指令的任务管理代理
   - 工具注册和执行
   - 会话管理

4. **工具 (MCP + 自定义函数)**
   - 自动注入 user_id 的包装函数
   - 带用户隔离的数据库操作
   - 一致的错误处理

5. **数据库**
   - SQLModel ORM 模型
   - 按用户过滤任务
   - 对话持久化

---

## 快速启动工作流

### 阶段 1：后端设置 (FastAPI)

**1. 创建 ChatKit Server 类**

```python
from chatkit.server import ChatKitServer
from chatkit.store import Store

class MyChatKitServer(ChatKitServer):
    def __init__(self):
        store = CustomChatKitStore()
        super().__init__(store=store)

    async def respond(self, thread, input, context):
        """处理用户消息并流式返回 AI 响应"""
        user_id = getattr(context, 'user_id', None)
        # 创建带包装工具的代理
        # 使用官方模式流式响应
```

**2. 创建 MCP 工具包装器**

```python
# 从上下文提取 user_id 并注入到工具调用中
def add_task_wrapper(title: str, description: str = None):
    return mcp_add_task(user_id=user_id, title=title, description=description)

def list_tasks_wrapper(status: str = "all"):
    return mcp_list_tasks(user_id=user_id, status=status)
```

**3. 创建 FastAPI 端点**

```python
@router.post("/api/v1/chatkit")
async def chatkit_protocol_endpoint(request: Request):
    user_id = request.state.user_id  # 从 JWT 中间件获取
    context = create_context_object(user_id=user_id)
    result = await chatkit_server.process(body, context)
    return StreamingResponse(result, media_type="text/event-stream")
```

**4. 配置 JWT 中间件**

```python
class JWTAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request, call_next):
        # 从 Authorization header 提取 JWT token
        # 解码并设置 request.state.user_id
        # 所有端点都可以访问已认证的 user_id
```

### 阶段 2：前端设置 (Next.js + React)

**1. 配置 ChatKit SDK**

```typescript
const chatKitConfig: UseChatKitOptions = {
  api: {
    url: `${API_BASE_URL}/api/v1/chatkit`,
    domainKey: 'your-domain-key',
    fetch: authenticatedFetch, // 带 JWT 的自定义 fetch
  },
  theme: 'light',
  header: { enabled: true, title: { text: 'AI Chat' } },
  history: { enabled: true },
}
```

**2. 创建认证 Fetch 包装器**

```typescript
async function authenticatedFetch(input, options) {
  const token = localStorage.getItem('access_token')
  const headers = {
    ...options?.headers,
    'Authorization': `Bearer ${token}`,
  }
  return fetch(input, { ...options, headers })
}
```

**3. 集成 ChatKit 组件**

```typescript
import { ChatKitWidget } from '@openai/chatkit-react'

export default function Dashboard() {
  return (
    <div className="flex gap-4">
      {/* 应用内容 */}
      {showChat && (
        <ChatKitWidget {...chatKitConfig} />
      )}
    </div>
  )
}
```

**4. 添加自动刷新实现实时同步**

```typescript
useEffect(() => {
  if (!showChatKit) return

  // 聊天打开时立即刷新
  fetchTasks()

  // 每 1 秒刷新一次实现实时更新
  const interval = setInterval(() => {
    fetchTasks()
  }, 1000)

  return () => clearInterval(interval)
}, [showChatKit])
```

### 阶段 3：工具实现 (MCP)

**1. 创建带用户隔离的 MCP 工具**

```python
def add_task(user_id: str, title: str, description: Optional[str] = None):
    """创建任务 - 从包装器接收 user_id"""
    task = Task(
        id=str(uuid.uuid4()),
        user_id=user_id,  # 关键：确保用户隔离
        title=title,
        description=description,
        completed=False,
        created_at=datetime.utcnow(),
    )
    with Session(engine) as session:
        session.add(task)
        session.commit()
```

**2. 注册 MCP 工具**

```python
mcp_server = MCPServer()
mcp_server.register_tool("add_task", add_task)
mcp_server.register_tool("list_tasks", list_tasks)
mcp_server.register_tool("delete_task", delete_task)
# ... 更多工具
```

---

## 核心模式与最佳实践

### 1. 用户隔离策略

**三级保障：**

1. **中间件级别** - JWT 验证确保只有已认证用户
2. **工具级别** - 包装函数自动注入 user_id
3. **数据库级别** - 所有查询按 user_id 过滤

```python
# 中间件从 token 提取 user_id
request.state.user_id = payload.get("user_id")

# 工具包装器捕获并注入
def add_task_wrapper(title):
    return mcp_add_task(user_id=user_id, ...)

# 数据库强制过滤
WHERE user_id = ? AND task_id = ?
```

### 2. 流式响应模式

```python
# 使用 Runner.run_streamed 的官方 ChatKit 模式
result = Runner.run_streamed(
    task_agent.agent,
    agent_input,
    context=agent_context,
)

# 使用官方 stream_agent_response 流式传输事件
async for event in stream_agent_response(agent_context, result):
    yield event
```

---

## 集成模式

### 模式 1：任务管理聊天机器人（基础）

**功能：**
- 用户通过与 ChatKit 对话创建任务
- ChatKit 在侧边栏显示任务列表
- 自动刷新保持任务列表同步

**参考文件：**
- [后端架构](./references/taskpilot_backend_architecture.md)
- [前端集成模式](./references/frontend_integration.md)

### 模式 2：多应用 ChatKit 部署

**功能：**
- 将 ChatKit 部署到多个应用
- 共享相同的后端和数据库
- 每个应用有隔离的用户上下文

### 模式 3：实时协作

**功能：**
- 多用户与同一聊天机器人实例对话
- 自动刷新保持所有人的数据同步
- 用户隔离防止跨用户数据泄露

---

## 常见问题与解决方案

### 问题 1：ChatKit 中创建的任务不显示在仪表板

**根本原因：** user_id 未传递给 MCP 工具
**解决方案：** 使用捕获并注入 user_id 的包装函数

### 问题 2：一个用户看到另一个用户的任务

**根本原因：** 数据库查询中缺少 user_id 过滤
**解决方案：** 在工具级别始终按 user_id 过滤

### 问题 3：ChatKit API 端点未找到

**根本原因：** 路由器未包含在 FastAPI 应用中
**解决方案：** 在 main.py 中包含路由器

### 问题 4：聊天组件不显示消息

**根本原因：** 自定义 fetch 未添加 JWT token
**解决方案：** 确保 authenticatedFetch 添加 Bearer token

---

## 高级主题

### 实时更新（WebSocket）

如需真正的实时（非轮询）：
- 在 HTTP 端点旁实现 WebSocket 端点
- 向所有连接的客户端广播更新
- 维护带用户上下文的连接状态

### 会话持久化

在数据库中存储对话历史：
- 将对话关联到用户
- 检索聊天历史作为上下文
- 允许恢复对话

---

## 资源

### references/

**后端架构：** 完整的 FastAPI ChatKit 服务器实现细节和模式

**前端集成：** Next.js ChatKit 组件配置和认证

**MCP 工具指南：** 创建带自动 user_id 注入的包装工具函数

**用户隔离：** 三级用户隔离策略和验证清单

### scripts/

**chatkit_server_template.py** - 包含所有必需方法的 FastAPI ChatKit 服务器模板

**mcp_wrapper_generator.py** - 自动生成 MCP 工具包装器的脚本

**frontend_config_generator.ts** - ChatKit 前端设置的 TypeScript 配置生成器

---

## 验证清单

构建 ChatKit 聊天机器人时需验证：

- [ ] JWT 中间件从 token 提取 user_id
- [ ] ChatKit 端点在 context 中接收 user_id
- [ ] 工具包装器捕获并注入 user_id
- [ ] 数据库查询按 user_id 过滤
- [ ] 前端 authenticatedFetch 包含 Bearer token
- [ ] ChatKit 配置指向正确的后端端点
- [ ] 自动刷新定期获取更新数据
- [ ] 一个用户无法看到另一个用户的数据
- [ ] 聊天机器人可以成功调用 MCP 工具
- [ ] 工具响应出现在 ChatKit 对话中
- [ ] 聊天机器人和仪表板之间的实时同步正常工作

---

## 后续步骤

1. **新项目：** 从 `assets/fastapi-backend-template/` 和 `assets/chatkit-nextjs-template/` 复制模板
2. **现有应用：** 按照"集成模式"部分参考架构指南
3. **高级功能：** 阅读"高级主题"部分并根据需要扩展
4. **故障排除：** 检查"常见问题与解决方案"并验证清单
