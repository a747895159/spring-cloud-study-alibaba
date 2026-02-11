---
name: system-integration-validator
description: 在部署前验证系统集成。用于检查端口、数据库连接、前后端 API 或调试被阻塞/卡住的工作流时使用。检测死端、瓶颈、循环依赖。
---

# 系统集成验证器

在部署前验证系统集成。

## 使用场景

- 部署前验证
- 检查端口可用性
- 验证数据库连接
- 调试卡住的工作流
- 检测死端或循环依赖

## 工作流程

### 步骤 1：检查端口

验证所有所需端口是否空闲。

### 步骤 2：验证数据库

测试 PostgreSQL 和 Redis 连接。

### 步骤 3：验证 API 契约

确保前端 ↔ 后端匹配。

### 步骤 4：分析数据流

检测死端、孤立输入、瓶颈。

---

## 部署前检查清单

1. **端口** - 所有所需端口空闲
2. **数据库** - 连接、连接池、迁移正常
3. **API 契约** - 前端 ↔ 后端匹配
4. **数据流** - 无死端或循环

## 端口检查
```bash
for port in 3000 3001 5432 6379 8080; do
  lsof -i :$port > /dev/null 2>&1 && echo "⚠️ $port 被占用" || echo "✅ $port 空闲"
done
```

## 数据库检查
```bash
pg_isready -h localhost -p 5432 && echo "✅ PostgreSQL 正常"
redis-cli ping && echo "✅ Redis 正常"
```

## 流程分析

需要检查：
- **死端**：输出从未被消费
- **孤立输入**：输入从未被提供
- **瓶颈**：入度过高（>3 个输入）
- **循环依赖**：A → B → A

## 常见阻塞问题
```typescript
// ❌ 无超时
await fetch(url)

// ✓ 带超时
const ctrl = new AbortController()
setTimeout(() => ctrl.abort(), 5000)
await fetch(url, { signal: ctrl.signal })
```
