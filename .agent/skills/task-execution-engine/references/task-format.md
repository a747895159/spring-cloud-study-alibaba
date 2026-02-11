# 任务格式规范

## 基本结构

```markdown
## 实现任务

- [ ] **任务标题** `priority:N` `phase:PHASE` `deps:依赖1,依赖2`
  - files: file1.py, file2.py
  - [ ] 验收标准 1
  - [ ] 验收标准 2
```

## 任务行

```
- [ ] **任务标题** `priority:1` `phase:model` `deps:其他任务`
```

| 组件 | 必需 | 描述 |
|------|------|------|
| `- [ ]` | 是 | 复选框（未选中） |
| `**标题**` | 是 | 粗体任务标题 |
| `priority:N` | 否 | 优先级 1-10（默认：5，数字越小优先级越高） |
| `phase:X` | 否 | 阶段：model、api、ui、test、docs |
| `deps:A,B` | 否 | 逗号分隔的依赖任务标题 |

## 任务详情（缩进）

### 文件行

```markdown
  - files: src/models/user.py, tests/test_user.py
```

逗号分隔的待创建/修改文件列表。

### 验收标准

```markdown
  - [ ] User 模型包含 email 字段
  - [ ] 密码哈希使用 bcrypt
```

每个验收标准一个复选框。所有标准必须全部选中才算任务完成。

### 失败原因（自动添加）

```markdown
  - reason: 数据库连接失败
```

任务标记为失败时自动添加。

## 状态标记

| 状态 | 复选框 | 标记 |
|------|--------|------|
| 待处理 | `- [ ]` | （无） |
| 已完成 | `- [x]` | ✅ |
| 已失败 | `- [x]` | ❌ |

## 优先级顺序

1. 优先级数字越小 = 越先执行
2. 依赖项必须先完成
3. 依赖未满足的任务处于"阻塞"状态

## 示例

### 待处理任务

```markdown
- [ ] **Create User model** `priority:1` `phase:model`
  - files: src/models/user.py
  - [ ] User model has email and password_hash fields
  - [ ] Email validation implemented
```

### 已完成任务

```markdown
- [x] **Create User model** `priority:1` `phase:model` ✅
  - files: src/models/user.py
  - [x] User model has email and password_hash fields
  - [x] Email validation implemented
```

### 已失败任务

```markdown
- [x] **Create User model** `priority:1` `phase:model` ❌
  - files: src/models/user.py
  - [ ] User model has email and password_hash fields
  - reason: bcrypt package not installed
```

### 带依赖的任务

```markdown
- [ ] **Create auth API** `priority:3` `phase:api` `deps:Create User model,Implement JWT`
  - files: src/api/auth.py
  - [ ] POST /register endpoint
  - [ ] POST /login endpoint
```

此任务在"Create User model"和"Implement JWT"都完成之前不会被 `next` 命令选中。
