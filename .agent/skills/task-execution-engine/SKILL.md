---
name: task-execution-engine
description: 使用 Markdown 复选框从设计文档中执行实现任务。适用于：(1) 从 feature-design-assistant 输出实现功能，(2) 恢复中断的工作，(3) 批量执行任务。触发短语："开始实现"、"运行任务"、"恢复"。
---

# 功能流水线

直接从设计文档执行实现任务。任务以 Markdown 复选框管理 — 无需单独的会话文件。

## 快速参考

```bash
# 获取下一个任务
python3 scripts/task_manager.py next --file <design.md>

# 标记任务完成
python3 scripts/task_manager.py done --file <design.md> --task "任务标题"

# 标记任务失败
python3 scripts/task_manager.py fail --file <design.md> --task "任务标题" --reason "..."

# 查看状态
python3 scripts/task_manager.py status --file <design.md>
```

## 任务格式

任务以 Markdown 复选框形式写在设计文档中：

```markdown
## 实现任务

- [ ] **创建 User 模型** `priority:1` `phase:model`
  - files: src/models/user.py, tests/models/test_user.py
  - [ ] User 模型包含 email 和 password_hash 字段
  - [ ] 实现邮箱验证
  - [ ] 密码哈希使用 bcrypt

- [ ] **实现 JWT 工具** `priority:2` `phase:model`
  - files: src/utils/jwt.py
  - [ ] generate_token() 创建有效的 JWT
  - [ ] verify_token() 验证 JWT

- [ ] **创建认证 API** `priority:3` `phase:api` `deps:创建 User 模型,实现 JWT 工具`
  - files: src/api/auth.py
  - [ ] POST /register 端点
  - [ ] POST /login 端点
```

详细格式规范请参阅 [references/task-format.md](references/task-format.md)。

## 执行循环

```
循环直到没有剩余任务：
  1. 获取下一个任务 (task_manager.py next)
  2. 读取任务详情（文件、标准）
  3. 实现任务
  4. 验证验收标准
  5. 更新状态 (task_manager.py done/fail)
  6. 继续
```

### 无人值守模式规则

- **不得停下来**提问
- **不得要求**澄清
- 根据代码库模式做出自主决策
- 如果被阻塞，标记为失败并继续

## 状态更新

已完成的任务：
```markdown
- [x] **创建 User 模型** `priority:1` `phase:model` ✅
  - files: src/models/user.py
  - [x] User 模型包含 email 字段
  - [x] 密码哈希已实现
```

失败的任务：
```markdown
- [x] **创建 User 模型** `priority:1` `phase:model` ❌
  - files: src/models/user.py
  - [ ] User 模型包含 email 字段
  - reason: 缺少数据库配置
```

## 恢复/恢复

要恢复中断的工作，只需使用相同的设计文件再次运行：

```
/feature-pipeline docs/designs/xxx.md
```

任务管理器将找到第一个未完成的任务并从那里继续。

## 集成

此技能通常在 `/feature-analyzer` 完成后触发：

```
用户: /feature-analyzer implement user auth

Claude: [设计功能，生成任务列表]
        设计已保存到 docs/designs/2026-01-02-user-auth.md
        准备开始实现？

用户: 是 / 开始实现

Claude: [通过 task-execution-engine 执行任务]
```
