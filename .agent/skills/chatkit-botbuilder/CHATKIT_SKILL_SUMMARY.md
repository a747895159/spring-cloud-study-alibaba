# ChatKit Botbuilder 技能 - 完整总结

## 🎯 创建内容

一个全面的、生产就绪的技能，用于构建具有完整 OpenAI Agents SDK 和 MCP 工具集成的 ChatKit 聊天机器人。

**技能位置：** `~/.claude/skills/chatkit-botbuilder/`

## 📋 技能内容

### 1. SKILL.md（主指南 - 510 行）

完整文档涵盖：

- **架构概述** - 高级数据流图
- **快速开始工作流** - 三阶段实现指南
  - 阶段 1：后端设置（FastAPI）
  - 阶段 2：前端设置（Next.js + React）
  - 阶段 3：工具实现（MCP）
- **核心模式与最佳实践** - 用户隔离、流式传输、线程管理
- **集成模式** - 任务管理、多应用部署、实时协作
- **常见问题与解决方案** - 故障排除指南
- **高级主题** - WebSockets、自定义 Schema、会话持久化
- **验证检查清单** - 11 项实现检查清单

### 2. 参考文档（46,000+ 字符）

#### `references/backend_architecture.md`
完整的 FastAPI ChatKit 服务端实现，包括：
- JWT 认证中间件
- CustomChatKitStore 实现
- MyChatKitServer 类及 respond() 方法
- FastAPI 端点配置
- MCP 工具注册
- 三级用户隔离
- 测试策略
- 实现检查清单

#### `references/frontend_integration.md`
Next.js ChatKit 组件配置，包括：
- 环境设置和依赖
- ChatKit 配置文件（chatkit-config.ts）
- 带认证的 fetch 封装
- ChatKit 组件
- 仪表板集成
- 实时同步自动刷新
- 认证流程
- 调试指南
- 性能优化建议

#### `references/mcp_wrapper_guide.md`
MCP 工具封装函数完整指南，包括：
- 问题与解决方案
- 封装器工作原理（闭包模式）
- 全部 6 个工具的完整封装实现
- ChatKit 服务端集成
- 封装器的必要性说明
- 编程方式创建封装器
- 测试策略
- 常见问题调试
- 性能考虑
- 实现检查清单

#### `references/user_isolation.md`
全面的安全指南，包括：
- 三级隔离策略（中间件、工具、数据库）
- 完整的隔离数据流
- 验证检查清单
- 常见隔离失败及修复
- 隔离测试
- 安全最佳实践
- 隔离问题调试
- 合规与审计

## 🚀 快速开始

当你需要构建 ChatKit 聊天机器人时：

1. **阅读：** `SKILL.md` - 获取概述和架构理解
2. **参考：** `backend_architecture.md` - 实现 FastAPI 后端
3. **参考：** `frontend_integration.md` - 设置 Next.js 前端
4. **参考：** `mcp_wrapper_guide.md` - 创建工具封装器
5. **参考：** `user_isolation.md` - 确保安全性

## 💡 关键特性

✅ **完整架构模式**
- 完整的 FastAPI 后端实现
- Next.js 前端集成
- OpenAI Agents SDK 集成
- MCP 工具封装模式

✅ **用户隔离保证**
- 三级安全（中间件、工具、数据库）
- 来自 TaskPilotAI 的验证模式
- 全面的测试策略

✅ **实时同步**
- 自动刷新机制
- ChatKit ↔ 仪表板同步
- 基于轮询的实时更新

✅ **生产就绪**
- 错误处理
- 日志策略
- 性能优化建议
- 调试指南
- 安全最佳实践

## 📊 技能规模

| 组件 | 大小 | 用途 |
|------|------|------|
| SKILL.md | 14 KB | 主指南含架构与模式 |
| backend_architecture.md | 11 KB | FastAPI 服务端实现细节 |
| frontend_integration.md | 10 KB | Next.js 组件设置与集成 |
| mcp_wrapper_guide.md | 12 KB | 工具封装模式与示例 |
| user_isolation.md | 13 KB | 安全与隔离验证 |
| **合计** | **60 KB** | **完整实现指南** |

## 🔍 使用场景

此技能使 Claude 能够帮助你：

1. **从零构建 ChatKit 聊天机器人**
   - 架构指导
   - 每个组件的代码示例
   - 逐步实现

2. **将 ChatKit 集成到现有应用**
   - FastAPI 后端集成
   - Next.js 前端集成
   - 实时同步

3. **创建专业 AI 助手**
   - 自定义 MCP 工具集成
   - 特定领域聊天机器人设计
   - 多用户系统设置

4. **修复 ChatKit 集成问题**
   - 故障排除指南
   - 常见问题与解决方案
   - 调试策略

5. **确保用户隔离和安全**
   - 三级隔离验证
   - 安全最佳实践
   - 测试策略

6. **将 ChatKit 部署到生产环境（Vercel + Render）**
   - OpenAI 域名验证设置
   - 环境变量配置
   - CORS 和安全头
   - 多环境管理

## 🎓 学习价值

此技能展示了：

- **现代 AI 架构** - 如何将 OpenAI Agent SDK 与自定义后端集成
- **全栈开发** - 前端（Next.js）、后端（FastAPI）、数据库
- **安全模式** - JWT 认证、用户隔离、数据过滤
- **工具集成** - MCP 工具、工具封装器、参数自动注入
- **实时系统** - 自动刷新、轮询、同步
- **生产模式** - 错误处理、日志、测试、调试
- **OpenAI 域名验证** - 生产部署的 ChatKit SDK 域名验证
- **多平台部署** - Vercel 前端 + Render 后端配置

## ✨ 包含的示例

- 从 localStorage 提取 JWT 令牌
- 带自定义 fetch 的 ChatKit 配置
- 使用闭包模式的 MCP 工具封装器
- FastAPI ChatKit 端点实现
- 带 user_id 过滤的数据库查询
- 用户隔离验证测试

## 🔐 生产部署指南（新增）

### ChatKit SDK 的 OpenAI 域名验证

将 ChatKit 部署到生产环境时，官方 `@openai/chatkit-react` SDK 需要域名验证：

#### 解决的问题
```
Error: Domain verification failed for https://task-pilot-ai-ashen.vercel.app
POST https://api.openai.com/v1/chatkit/domain_keys/verify 400 (Bad Request)
```

#### 解决方案：OpenAI 域名注册

1. **在 OpenAI 平台注册域名**
   - 前往：https://platform.openai.com/settings/organization/security/domain-allowlist
   - 点击"+ Add domain"
   - 输入你的生产域名（例如 `task-pilot-ai-ashen.vercel.app`）
   - OpenAI 生成一个**公钥**（例如 `domain_pk_694d951d300881908730eaa457e5605809652cfa18d7a99a`）

2. **配置前端（chatkit-config.ts）**
   ```typescript
   // 使用 OpenAI 的公钥作为 domainKey
   const DOMAIN_PUBLIC_KEY = process.env.NEXT_PUBLIC_DOMAIN_PUBLIC_KEY || 'domain_pk_694d951d300881908730eaa457e5605809652cfa18d7a99a'

   // 重要：domainKey 必须是实际的公钥，不是自定义标识符
   const DOMAIN_KEY = DOMAIN_PUBLIC_KEY

   export const chatKitConfig: UseChatKitOptions = {
     api: {
       url: API_URL,
       domainKey: DOMAIN_KEY,  // 必须是 OpenAI 的公钥
       fetch: authenticatedFetch,
     },
     // ... 其余配置
   }
   ```

3. **在 Vercel 添加环境变量**
   - `NEXT_PUBLIC_DOMAIN_PUBLIC_KEY` = `domain_pk_...`（来自 OpenAI）
   - `NEXT_PUBLIC_API_URL` = 你的后端 URL
   - `NEXT_PUBLIC_OPENAI_API_KEY` = 可选（如前端需要直接访问 OpenAI）

4. **Render 后端配置**
   - `OPENAI_API_KEY` = 你的 OpenAI API 密钥（用于代理执行）
   - `CHATKIT_DOMAIN_ALLOWLIST` = 允许的域名列表
   - `CORS_ORIGINS` = 包含 Vercel URL

#### 关键洞察
- ❌ **不要**使用自定义域名标识符（例如 `'taskpilot-production'`）
- ✅ **要**使用 OpenAI 域名注册生成的实际公钥
- ✅ SDK 会根据 OpenAI 的注册表验证此密钥
- ✅ 400 错误表示域名密钥错误 - 检查 OpenAI 平台注册

## 🔗 相关技能

- **mcp-builder** - 用于构建额外的 MCP 工具
- **frontend-design** - 用于创建精美的 UI 组件
- **nextjs-devtools** - 用于 Next.js 特定开发
- **skill-creator** - 用于创建额外的自定义技能

## 📝 实现说明

所有示例基于实际的 TaskPilotAI 实现：
- 来自 /backend/routes/chatkit.py 的真实代码
- 来自 /frontend/components/ChatKit/ 的真实前端
- 来自 TaskPilotAI 第二阶段的验证模式

此技能涵盖了构建 ChatKit 聊天机器人所需的完整架构知识。

## 🎯 成功标准

当以下条件满足时，表示你已成功使用此技能：

- ✅ ChatKit 端点接收用户消息
- ✅ Agent 可以带用户上下文调用 MCP 工具
- ✅ 在 ChatKit 中创建的任务出现在仪表板中
- ✅ 每个用户只能看到自己的数据
- ✅ ChatKit 和 UI 之间的实时同步正常工作
- ✅ 所有安全检查通过

## 🆘 支持与故障排除

### 常见问题与解决方案

#### 问题 1：域名验证错误（生产环境）
**错误：** `Domain verification failed for https://yourapp.vercel.app`
- ❌ 问题：使用了自定义域名密钥而非 OpenAI 的公钥
- ✅ 解决：在 https://platform.openai.com/settings/organization/security/domain-allowlist 注册域名
- ✅ 在 `chatkit-config.ts` 中使用生成的公钥
- ✅ 在 Vercel 环境中添加 `NEXT_PUBLIC_DOMAIN_PUBLIC_KEY`

#### 问题 2：域名验证 400 Bad Request
**错误：** `POST https://api.openai.com/v1/chatkit/domain_keys/verify 400`
- ❌ 问题：域名密钥格式错误
- ✅ 解决：验证 `domainKey` 格式为 `domain_pk_...`（非自定义字符串）
- ✅ 检查 OpenAI 平台 - 复制那里显示的精确公钥

#### 问题 3：ChatKit 在本地正常但在生产环境不工作
**错误：** 在 `localhost:3000` 上正常但在生产域名失败
- ℹ️ 原因：Localhost 在开发中绕过域名验证
- ✅ 解决：在 OpenAI 平台注册生产域名（参阅生产部署指南）

### 通用故障排除步骤

如果遇到问题：

1. **检查环境变量**
   - Vercel：Settings → Environment Variables
   - Render：Settings → Environment
   - 验证所有 `NEXT_PUBLIC_*` 变量已设置

2. **审查文档**
   - SKILL.md - 常见问题与解决方案章节
   - 参考指南（后端、前端、封装器、隔离）

3. **验证检查清单项目**
   - 每个组件对照实现检查清单
   - OpenAI 平台的域名注册
   - `chatkit-config.ts` 中的正确公钥

4. **启用调试日志**
   - 检查浏览器控制台（F12）的错误
   - 检查 Render 日志的后端问题
   - 查找特定错误代码（400、401、403、404）

5. **逐一测试每个组件**
   - 先在本地测试后端
   - 用 mock API 在本地测试前端
   - 单独测试隔离级别
   - 用 curl/postman 测试域名验证

---

**状态：** ✅ 完成并已验证
**创建时间：** 2025-12-25
**更新时间：** 2025-12-27
**版本：** 1.1
**验证：** 通过 skill-creator 验证 + 生产部署（Vercel + Render）

### 最近更新（v1.1）
- ✅ 添加了 OpenAI 域名验证的生产部署指南
- ✅ 记录了域名注册流程和公钥配置
- ✅ 添加了域名验证错误的故障排除
- ✅ 包含 Vercel + Render 环境变量设置
- ✅ 添加了 3 个常见生产部署问题及解决方案
- ✅ 真实验证：TaskPilotAI 第三阶段 ChatKit 集成
