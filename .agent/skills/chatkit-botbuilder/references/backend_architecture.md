# ChatKit 后端架构

## 概述

本文档描述了使用 FastAPI、OpenAI Agents SDK 和 MCP 工具构建 ChatKit 后端的完整架构。

## 完整后端实现

### 1. JWT 认证中间件

```python
from fastapi import HTTPException, Request
from starlette.middleware.base import BaseHTTPMiddleware
from jose import jwt
from config import settings

class JWTAuthMiddleware(BaseHTTPMiddleware):
    async def dispatch(self, request: Request, call_next):
        # 跳过公开端点的认证
        public_paths = ["/auth/", "/health", "/docs", "/openapi.json"]
        if request.method == "OPTIONS" or any(request.url.path.startswith(path) for path in public_paths):
            return await call_next(request)

        # 从 Authorization 头提取令牌
        auth_header = request.headers.get("Authorization")
        if not auth_header:
            return JSONResponse({"detail": "Missing authorization header"}, status_code=401)

        try:
            scheme, token = auth_header.split()
            if scheme.lower() != "bearer":
                return JSONResponse({"detail": "Invalid auth scheme"}, status_code=401)

            # 处理开发环境的测试令牌
            if settings.ENVIRONMENT == "development" and token.startswith("test-token-"):
                user_id = token.replace("test-token-", "")
                request.state.user_id = user_id
                return await call_next(request)

            # 解码 JWT 令牌
            payload = jwt.decode(token, settings.JWT_SECRET, algorithms=[settings.JWT_ALGORITHM])
            user_id = payload.get("user_id")
            if not user_id:
                return JSONResponse({"detail": "Invalid token"}, status_code=401)

            request.state.user_id = user_id
        except Exception as e:
            return JSONResponse({"detail": str(e)}, status_code=401)

        return await call_next(request)
```

### 2. ChatKit 服务端实现

```python
from chatkit.server import ChatKitServer
from chatkit.store import Store
from chatkit.types import ThreadMetadata, UserMessageItem, Page, ErrorEvent
from chatkit.agents import AgentContext, simple_to_agent_input, stream_agent_response
from agents import Runner

class CustomChatKitStore(Store):
    """ChatKit 线程持久化的内存存储"""

    def __init__(self):
        self.threads = {}
        self.items = {}

    async def load_thread(self, thread_id: str, context) -> ThreadMetadata:
        if thread_id in self.threads:
            return self.threads[thread_id]
        return ThreadMetadata(
            id=thread_id,
            created_at=datetime.utcnow(),
            metadata={},
        )

    async def save_thread(self, thread: ThreadMetadata, context) -> None:
        self.threads[thread.id] = thread

    async def load_thread_items(self, thread_id: str, after: str | None, limit: int, order: str, context):
        items = self.items.get(thread_id, [])
        items.sort(key=lambda i: i.created_at, reverse=(order == "desc"))

        if after:
            after_index = next((i for i, item in enumerate(items) if item.id == after), -1)
            if after_index >= 0:
                items = items[after_index + 1:]

        has_more = len(items) > limit
        items = items[:limit]

        return Page(data=items, has_more=has_more, after=items[-1].id if items else None)

    async def add_thread_item(self, thread_id: str, item, context) -> None:
        if thread_id not in self.items:
            self.items[thread_id] = []
        self.items[thread_id].append(item)

    # ... 实现其他必需方法 ...


class MyChatKitServer(ChatKitServer):
    """集成代理的 ChatKit 服务端"""

    def __init__(self):
        store = CustomChatKitStore()
        super().__init__(store=store)

    async def respond(self, thread: ThreadMetadata, input: UserMessageItem, context) -> AsyncIterator:
        try:
            # 从上下文提取 user_id（由 JWT 中间件设置）
            user_id = getattr(context, 'user_id', None) or context.headers.get("X-User-ID")
            if not user_id:
                yield ErrorEvent(level='danger', message="Authentication required")
                return

            logger.info(f"ChatKit respond: user={user_id}, thread={thread.id}")

            # 将用户消息添加到线程
            await self.store.add_thread_item(thread.id, input, context)

            # 加载对话历史
            items_page = await self.store.load_thread_items(
                thread.id, after=None, limit=30, order="desc", context=context
            )
            items = list(reversed(items_page.data))
            agent_input = await simple_to_agent_input(items)

            # 初始化带 user_id 注入的 MCP 工具
            mcp_server = initialize_mcp_server()

            # 创建注入 user_id 的封装函数
            def add_task_wrapper(title: str, description: str = None):
                return mcp_add_task(user_id=user_id, title=title, description=description)

            def list_tasks_wrapper(status: str = "all"):
                return mcp_list_tasks(user_id=user_id, status=status)

            def delete_task_wrapper(task_id: str):
                return mcp_delete_task(user_id=user_id, task_id=task_id)

            # ... 为所有工具创建封装器 ...

            wrapped_tools = [
                add_task_wrapper,
                list_tasks_wrapper,
                delete_task_wrapper,
                # ... 所有封装的工具 ...
            ]

            # 使用封装后的工具创建代理
            task_agent = create_task_agent(tools=wrapped_tools)

            # 创建代理上下文
            agent_context = AgentContext(
                thread=thread,
                store=self.store,
                request_context=context,
            )

            # 流式代理响应
            result = Runner.run_streamed(
                task_agent.agent,
                agent_input,
                context=agent_context,
            )

            # 输出事件
            async for event in stream_agent_response(agent_context, result):
                yield event

        except Exception as e:
            logger.error(f"Error in respond: {str(e)}", exc_info=True)
            yield ErrorEvent(level='danger', message=f"Error: {str(e)}")
```

### 3. FastAPI 端点

```python
from fastapi import APIRouter, Request, Depends
from fastapi.responses import StreamingResponse
from sqlmodel import Session

router = APIRouter()
chatkit_server = MyChatKitServer()

@router.post("/api/v1/chatkit")
async def chatkit_protocol_endpoint(
    request: Request,
    db_session: Session = Depends(get_session)
):
    """ChatKit 协议端点"""
    try:
        # 从认证中间件提取 user_id
        user_id = getattr(request.state, "user_id", None)
        if not user_id:
            user_id = request.headers.get("X-User-ID")

        logger.info(f"ChatKit protocol request from user: {user_id}")

        # 获取请求体
        body = await request.body()

        # 创建包含 user_id 的上下文
        context = type('Context', (), {
            'user_id': user_id,
            'request': request,
            'db_session': db_session
        })()

        # 通过 ChatKit 服务端处理
        result = await chatkit_server.process(body, context)

        # 处理流式响应
        from chatkit.server import StreamingResult
        if isinstance(result, StreamingResult):
            return StreamingResponse(result, media_type="text/event-stream")

        # 处理常规响应
        if hasattr(result, 'json'):
            return Response(content=result.json, media_type="application/json")

        return JSONResponse(result)

    except Exception as e:
        logger.error(f"ChatKit protocol error: {str(e)}", exc_info=True)
        raise HTTPException(status_code=500, detail=str(e))
```

### 4. MCP 工具注册

```python
from mcp.tools import add_task, list_tasks, delete_task, complete_task, update_task, find_task_by_name

class MCPServer:
    def __init__(self):
        self.tools = {}

    def register_tool(self, name: str, tool) -> None:
        self.tools[name] = tool

    def get_tools(self):
        return self.tools

def initialize_mcp_server() -> MCPServer:
    mcp_server = MCPServer()

    mcp_server.register_tool("add_task", add_task)
    mcp_server.register_tool("list_tasks", list_tasks)
    mcp_server.register_tool("find_task_by_name", find_task_by_name)
    mcp_server.register_tool("complete_task", complete_task)
    mcp_server.register_tool("delete_task", delete_task)
    mcp_server.register_tool("update_task", update_task)

    logger.info(f"Registered tools: {list(mcp_server.get_tools().keys())}")
    return mcp_server
```

## 关键架构原则

### 1. 三级用户隔离

**中间件级别：**
- JWT 验证确保只有已认证用户
- 从令牌中提取 user_id → request.state.user_id

**工具级别：**
- 封装函数从上下文闭包中捕获 user_id
- 自动将 user_id 注入每个工具调用
- 防止意外遗漏 user_id

**数据库级别：**
- 所有查询按 user_id 过滤
- 在存储层强制数据隔离

### 2. 上下文传播

```
请求头（Authorization: Bearer <JWT>）
    ↓
JWT 中间件（提取 user_id）
    ↓
request.state.user_id = user_id
    ↓
ChatKit 端点（创建上下文对象）
    ↓
context.user_id = user_id
    ↓
MyChatKitServer.respond(context)
    ↓
封装函数捕获 user_id
    ↓
工具调用包含 user_id
```

### 3. 工具封装模式

封装器为何必要：

1. **OpenAI Agents SDK** 调用工具时不带领域上下文
2. **MCP 工具** 期望 user_id 作为第一个参数
3. **封装器桥接**此差距，在闭包中捕获 user_id

```python
# 无封装器：user_id 不可用
agent.call_tool("add_task", {"title": "Buy milk"})  # 缺少 user_id！

# 有封装器：user_id 自动注入
agent.call_tool("add_task_wrapper", {"title": "Buy milk"})
    ↓
def add_task_wrapper(title):
    return add_task(user_id=user_id, title=title)  # user_id 已捕获！
```

## 实现检查清单

- [ ] 在 FastAPI 应用中配置 JWT 中间件
- [ ] 实现 CustomChatKitStore 的所有必需方法
- [ ] MyChatKitServer 正确继承 ChatKitServer
- [ ] 工具封装器捕获并注入 user_id
- [ ] ChatKit 端点在路由中注册
- [ ] 路由包含在 FastAPI 应用中
- [ ] MCP 工具在 MCPServer 中注册
- [ ] Agent 上下文包含 user_id
- [ ] 正确处理流式响应
- [ ] 错误处理输出 ErrorEvent

## 测试后端

```bash
# 测试 ChatKit 端点
curl -X POST http://localhost:8000/api/v1/chatkit \
  -H "Authorization: Bearer test-token-user-123" \
  -H "Content-Type: application/json" \
  -d '{...chatkit protocol payload...}'

# 检查日志中的 user_id 提取
# 检查数据库中任务是否使用正确的 user_id 创建
```
