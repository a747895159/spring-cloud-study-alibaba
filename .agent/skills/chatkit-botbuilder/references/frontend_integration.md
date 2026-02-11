# ChatKit 前端集成指南

## Next.js 设置

### 1. 安装依赖

```bash
npm install @openai/chatkit-react better-auth jose
```

### 2. ChatKit 配置文件

创建 `lib/chatkit-config.ts`：

```typescript
import type { UseChatKitOptions } from '@openai/chatkit-react'

// 环境变量
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000'
const DOMAIN_KEY = process.env.NEXT_PUBLIC_DOMAIN_KEY || 'chatkit-app-dev'

// ChatKit 端点
const API_URL = `${API_BASE_URL}/api/v1/chatkit`

/**
 * 从多个可能的位置提取 JWT 令牌
 */
function getAuthToken(): string | null {
  if (typeof window === 'undefined') return null

  // 尝试 access_token（API 客户端标准）
  let token = localStorage.getItem('access_token')
  if (token) return token

  // 尝试 auth_token（备选）
  token = localStorage.getItem('auth_token')
  if (token) return token

  // 尝试 session token（Better Auth）
  token = localStorage.getItem('authjs.session-token')
  if (token) return token

  // 尝试 sessionStorage
  token = sessionStorage.getItem('access_token')
  if (token) return token

  return null
}

/**
 * 带 JWT 认证的自定义 fetch 函数
 * 所有 ChatKit API 调用都通过此函数
 */
async function authenticatedFetch(
  input: string | URL | Request,
  options?: RequestInit
): Promise<Response> {
  const token = getAuthToken()

  const headers: Record<string, string> = {
    ...(options?.headers as Record<string, string> || {}),
  }

  // 如果可用则添加 JWT 令牌
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  // 为 ChatKit 验证添加域名密钥
  headers['X-ChatKit-Domain-Key'] = DOMAIN_KEY

  return fetch(input, {
    ...options,
    headers,
  })
}

/**
 * ChatKit 配置
 * 即用型的 ChatKit 组件配置
 */
export const chatKitConfig: UseChatKitOptions = {
  api: {
    url: API_URL,
    domainKey: DOMAIN_KEY,
    fetch: authenticatedFetch,
  },

  theme: 'light',

  header: {
    enabled: true,
    title: {
      enabled: true,
      text: 'TaskPilot AI Chat',
    },
  },

  history: {
    enabled: true,
    showDelete: true,
    showRename: true,
  },

  composer: {
    placeholder: 'Ask me to add, update, or delete tasks...',
  },

  disclaimer: {
    text: 'ChatKit powered by OpenAI • Managed by TaskPilot AI',
  },

  // 事件处理器
  onReady: () => {
    console.log('ChatKit is ready!')
  },

  onError: (error: { error: Error }) => {
    console.error('ChatKit error:', error.error)
  },

  onResponseStart: () => {
    console.log('Assistant is responding...')
  },

  onResponseEnd: () => {
    console.log('Assistant response complete')
  },

  onThreadChange: (event: { threadId: string | null }) => {
    if (event.threadId) {
      localStorage.setItem('chatkit_thread_id', event.threadId)
    }
  },
}

/**
 * 获取当前 ChatKit 线程 ID
 */
export function getChatKitThreadId(): string | null {
  if (typeof window === 'undefined') return null
  return localStorage.getItem('chatkit_thread_id')
}

/**
 * 从 localStorage 清除 ChatKit 线程
 */
export function clearChatKitThread(): void {
  if (typeof window === 'undefined') return
  localStorage.removeItem('chatkit_thread_id')
}

/**
 * 验证 ChatKit 配置
 */
export function validateChatKitConfig(): { valid: boolean; errors: string[] } {
  const errors: string[] = []

  if (!API_BASE_URL) {
    errors.push('NEXT_PUBLIC_API_URL is not configured')
  }

  if (!API_URL) {
    errors.push('ChatKit endpoint URL could not be constructed')
  }

  if (!DOMAIN_KEY || typeof DOMAIN_KEY !== 'string') {
    errors.push('NEXT_PUBLIC_DOMAIN_KEY is not properly configured')
  }

  return {
    valid: errors.length === 0,
    errors,
  }
}
```

### 3. 环境配置

创建 `.env.local`：

```bash
NEXT_PUBLIC_API_URL=http://localhost:8000
NEXT_PUBLIC_DOMAIN_KEY=chatkit-app-dev
```

### 4. ChatKit 组件

```typescript
import React from 'react'
import { ChatKitWidget } from '@openai/chatkit-react'
import { chatKitConfig } from '@/lib/chatkit-config'

interface ChatKitPanelProps {
  authToken?: string
  userId?: string
}

export function ChatKitPanel({ authToken, userId }: ChatKitPanelProps) {
  if (!authToken || !userId) {
    return (
      <div className="flex items-center justify-center h-full text-gray-500">
        Loading authentication...
      </div>
    )
  }

  return (
    <div className="w-full h-full overflow-hidden flex flex-col bg-white rounded-lg shadow-sm border border-gray-200">
      <div className="p-4 border-b border-gray-200">
        <h3 className="text-lg font-semibold text-gray-900">TaskPilot AI Chat</h3>
        <p className="text-sm text-gray-500 mt-1">Powered by OpenAI ChatKit</p>
      </div>
      <div className="flex-1 overflow-hidden">
        <ChatKitWidget {...chatKitConfig} />
      </div>
    </div>
  )
}
```

### 5. 集成到仪表板

```typescript
'use client'

import { useState, useEffect } from 'react'
import { ChatKitPanel } from '@/components/ChatKit/ChatKitPanel'

export default function DashboardPage() {
  const [authToken, setAuthToken] = useState<string>('')
  const [userId, setUserId] = useState<string>('')
  const [showChat, setShowChat] = useState(false)
  const [tasks, setTasks] = useState([])

  // 从 JWT 提取用户 ID
  useEffect(() => {
    if (typeof window !== 'undefined') {
      const token = localStorage.getItem('access_token')
      if (token) {
        setAuthToken(token)
        try {
          const parts = token.split('.')
          if (parts.length === 3) {
            const decoded = JSON.parse(atob(parts[1]))
            setUserId(decoded.user_id || decoded.sub || '')
          }
        } catch (e) {
          console.error('Failed to decode token:', e)
        }
      }
    }
  }, [])

  // ChatKit 活动时自动刷新任务
  useEffect(() => {
    if (!showChat) return

    // 立即获取
    fetchTasks()

    // 每 1 秒获取一次
    const interval = setInterval(() => {
      fetchTasks()
    }, 1000)

    return () => clearInterval(interval)
  }, [showChat])

  const fetchTasks = async () => {
    const response = await fetch('/api/tasks', {
      headers: {
        'Authorization': `Bearer ${authToken}`,
      },
    })
    if (response.ok) {
      const data = await response.json()
      setTasks(data.tasks || [])
    }
  }

  return (
    <div className="flex gap-4 p-6">
      {/* 任务区域 */}
      <div className={`flex-1 ${showChat ? 'w-1/2' : 'w-full'}`}>
        <button
          onClick={() => setShowChat(!showChat)}
          className={showChat ? 'bg-green-600' : 'bg-blue-600'}
        >
          {showChat ? '✓ Chat Active' : '💬 Open Chat'}
        </button>

        {/* 你的任务列表 UI */}
        <div className="space-y-4 mt-4">
          {tasks.map((task) => (
            <div key={task.id} className="p-4 border rounded">
              <h3>{task.title}</h3>
              <p>{task.description}</p>
            </div>
          ))}
        </div>
      </div>

      {/* ChatKit 区域 */}
      {showChat && (
        <div className="w-1/2 min-w-0">
          <ChatKitPanel authToken={authToken} userId={userId} />
        </div>
      )}
    </div>
  )
}
```

## 认证流程

### 1. 用户登录

```typescript
// 用户通过 Better Auth 或你的认证系统登录
const { data } = await authClient.signIn.email({
  email: 'user@example.com',
  password: 'password',
})

// 返回 JWT 令牌并存储
localStorage.setItem('access_token', data.token)
```

### 2. localStorage 中的令牌

```typescript
// 令牌结构
// Header: { alg: "HS256", typ: "JWT" }
// Payload: { user_id: "user-123", email: "user@example.com", iat: ... }
// Signature: ...
```

### 3. authenticatedFetch 添加令牌

```typescript
// 每个 fetch 调用都包含授权信息
const response = await authenticatedFetch('/api/v1/chatkit', {
  method: 'POST',
  body: JSON.stringify({ message: 'Create a task' }),
})

// 变成：
// Authorization: Bearer <token>
```

### 4. 后端验证令牌

```python
# JWT 中间件提取并验证
payload = jwt.decode(token, JWT_SECRET, algorithms=['HS256'])
user_id = payload.get('user_id')
request.state.user_id = user_id  # 对 ChatKit 端点可用
```

## 实时同步

### 自动刷新策略

```typescript
// 当聊天活跃时，每 1 秒刷新任务列表
useEffect(() => {
  if (!showChat) return

  const interval = setInterval(() => {
    fetchTasks()
  }, 1000)

  return () => clearInterval(interval)
}, [showChat])
```

### 为什么这能工作

1. **用户在 ChatKit 中创建任务** - "Create 'Buy milk'"
2. **ChatKit 调用 add_task 工具** - 工具在数据库中用 user_id 创建任务
3. **仪表板自动刷新** - 用相同的 user_id 调用 GET /tasks
4. **任务出现** - 仪表板显示新创建的任务

## 环境变量

创建 `.env.local`：

```bash
# API 后端
NEXT_PUBLIC_API_URL=http://localhost:8000

# ChatKit 配置
NEXT_PUBLIC_DOMAIN_KEY=chatkit-app-dev

# OpenAI（如果前端需要）
NEXT_PUBLIC_OPENAI_API_KEY=sk-your-key-here
```

## 开发与生产环境

### 开发环境
```bash
NEXT_PUBLIC_API_URL=http://localhost:8000
NEXT_PUBLIC_DOMAIN_KEY=chatkit-dev
```

### 生产环境
```bash
NEXT_PUBLIC_API_URL=https://api.yourdomain.com
NEXT_PUBLIC_DOMAIN_KEY=chatkit-production
```

## 调试

### 在控制台检查令牌

```typescript
const token = localStorage.getItem('access_token')
const decoded = JSON.parse(atob(token.split('.')[1]))
console.log('User ID:', decoded.user_id)
```

### 检查 ChatKit 配置

```typescript
import { validateChatKitConfig } from '@/lib/chatkit-config'
const { valid, errors } = validateChatKitConfig()
if (!valid) console.error('Config errors:', errors)
```

### 监控网络请求

1. 打开开发者工具 → Network
2. 在 ChatKit 中发送消息
3. 查找 POST 到 `/api/v1/chatkit` 的请求
4. 检查请求头中的 `Authorization: Bearer <token>`
5. 检查响应的 StreamingResult

## 性能优化建议

1. **延迟加载 ChatKit 组件** - 仅在用户打开聊天时加载
2. **批量刷新请求** - 使用 debounce 代替直接调用
3. **缓存令牌** - 存储在 localStorage 中，过期时刷新
4. **最小化重渲染** - 为 fetch 函数使用 useCallback
5. **监控轮询** - 根据需求调整间隔
