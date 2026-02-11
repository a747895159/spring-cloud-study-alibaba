---
name: 功能设计助手
description: "通过自然的协作对话将想法转化为完整的设计和规范。在规划新功能、设计架构或对代码库进行重大更改时使用。"
---

# 功能设计助手

通过结构化的信息收集和协作验证，帮助将想法转化为完整的设计和规范。

**启动时声明：** "我正在使用功能设计助手技能来设计此功能。"

## 阶段 1：上下文发现

首先，探索代码库以了解：
- 项目结构和技术栈
- 现有模式和约定
- 相关功能或模块
- 相关区域的最近变更

## 阶段 2：结构化信息收集

使用 **AskUserQuestion** 批量高效收集信息。每次调用最多可以问 4 个问题。

### 第 1 轮：核心需求（4 个问题）

```json
{
  "questions": [
    {
      "question": "此功能的主要目标是什么？",
      "header": "目标",
      "multiSelect": false,
      "options": [
        { "label": "新功能", "description": "为系统添加全新的能力" },
        { "label": "增强", "description": "改进或扩展现有功能" },
        { "label": "Bug 修复", "description": "修复不正确的行为或问题" },
        { "label": "重构", "description": "在不改变行为的情况下提升代码质量" }
      ]
    },
    {
      "question": "此功能的主要用户是谁？",
      "header": "用户",
      "multiSelect": true,
      "options": [
        { "label": "终端用户", "description": "使用产品的外部客户" },
        { "label": "管理员", "description": "内部管理员或运维人员" },
        { "label": "开发者", "description": "使用 API 或 SDK 的其他开发者" },
        { "label": "系统", "description": "自动化流程或后台任务" }
      ]
    },
    {
      "question": "此功能的预期范围是什么？",
      "header": "范围",
      "multiSelect": false,
      "options": [
        { "label": "小型（1-2 天）", "description": "单个组件，有限的变更" },
        { "label": "中型（3-5 天）", "description": "多个组件，中等复杂度" },
        { "label": "大型（1-2 周）", "description": "横切关注点，重大变更" },
        { "label": "不确定", "description": "需要进一步探索才能估算" }
      ]
    },
    {
      "question": "是否有硬性截止日期或约束？",
      "header": "时间线",
      "multiSelect": false,
      "options": [
        { "label": "紧急", "description": "尽快完成，几天之内" },
        { "label": "本迭代", "description": "应在当前迭代内完成" },
        { "label": "灵活", "description": "没有硬性截止日期，质量优先" },
        { "label": "仅规划", "description": "现在只做设计，稍后实施" }
      ]
    }
  ]
}
```

### 第 2 轮：技术需求（4 个问题）

```json
{
  "questions": [
    {
      "question": "此功能会涉及系统的哪些层？",
      "header": "系统层",
      "multiSelect": true,
      "options": [
        { "label": "数据模型", "description": "数据库 Schema、模型、迁移" },
        { "label": "业务逻辑", "description": "服务、领域逻辑、规则" },
        { "label": "API", "description": "REST/GraphQL 端点、契约" },
        { "label": "UI", "description": "前端组件、用户界面" }
      ]
    },
    {
      "question": "关键的质量要求是什么？",
      "header": "质量",
      "multiSelect": true,
      "options": [
        { "label": "高性能", "description": "必须处理高负载或非常快速" },
        { "label": "高安全", "description": "敏感数据、认证、访问控制" },
        { "label": "高可靠", "description": "不能失败，需要冗余" },
        { "label": "易维护", "description": "需要易于理解和修改" }
      ]
    },
    {
      "question": "错误应该如何处理？",
      "header": "错误处理",
      "multiSelect": false,
      "options": [
        { "label": "快速失败", "description": "遇到任何错误立即停止" },
        { "label": "优雅降级", "description": "以减少的功能继续运行" },
        { "label": "重试恢复", "description": "带恢复逻辑的自动重试" },
        { "label": "视情况而定", "description": "不同情况使用不同策略" }
      ]
    },
    {
      "question": "首选什么测试方法？",
      "header": "测试",
      "multiSelect": false,
      "options": [
        { "label": "TDD（推荐）", "description": "先写测试，再写实现" },
        { "label": "后置测试", "description": "先实现，后添加测试" },
        { "label": "最少测试", "description": "仅测试关键路径" },
        { "label": "无测试", "description": "此功能跳过测试" }
      ]
    }
  ]
}
```

### 第 3 轮：集成与依赖（4 个问题）

```json
{
  "questions": [
    {
      "question": "此功能是否需要外部集成？",
      "header": "集成",
      "multiSelect": true,
      "options": [
        { "label": "数据库", "description": "新表、查询或迁移" },
        { "label": "外部 API", "description": "第三方服务调用" },
        { "label": "消息队列", "description": "异步处理、事件" },
        { "label": "无", "description": "不需要外部集成" }
      ]
    },
    {
      "question": "是否依赖其他功能或团队？",
      "header": "依赖",
      "multiSelect": true,
      "options": [
        { "label": "认证系统", "description": "用户认证或授权" },
        { "label": "其他功能", "description": "依赖正在开发中的功能" },
        { "label": "外部团队", "description": "需要其他团队的输入" },
        { "label": "无", "description": "完全独立的功能" }
      ]
    },
    {
      "question": "如何处理向后兼容性？",
      "header": "兼容性",
      "multiSelect": false,
      "options": [
        { "label": "必须保持", "description": "不能破坏现有客户端" },
        { "label": "API 版本控制", "description": "创建新版本，废弃旧版本" },
        { "label": "允许破坏性变更", "description": "可以进行破坏性变更" },
        { "label": "不适用", "description": "新功能，没有现有用户" }
      ]
    },
    {
      "question": "需要什么文档？",
      "header": "文档",
      "multiSelect": true,
      "options": [
        { "label": "API 文档", "description": "端点文档" },
        { "label": "用户指南", "description": "终端用户操作指南" },
        { "label": "开发指南", "description": "技术实现细节" },
        { "label": "无", "description": "不需要文档" }
      ]
    }
  ]
}
```

### 第 4 轮：补充问题（根据上下文）

根据之前的回答，提出后续问题。示例：

**如果选择了 UI 层：**
```json
{
  "questions": [
    {
      "question": "应该使用什么 UI 框架/方案？",
      "header": "UI 技术",
      "multiSelect": false,
      "options": [
        { "label": "React", "description": "使用 Hooks 的 React 组件" },
        { "label": "Vue", "description": "Vue.js 组件" },
        { "label": "服务端渲染", "description": "服务端渲染的 HTML 模板" },
        { "label": "沿用现有模式", "description": "遵循当前项目约定" }
      ]
    }
  ]
}
```

**如果选择了高安全：**
```json
{
  "questions": [
    {
      "question": "需要哪些安全措施？",
      "header": "安全",
      "multiSelect": true,
      "options": [
        { "label": "输入验证", "description": "严格的输入清洗" },
        { "label": "限流", "description": "防止滥用和 DoS 攻击" },
        { "label": "审计日志", "description": "跟踪所有敏感操作" },
        { "label": "加密", "description": "静态/传输数据加密" }
      ]
    }
  ]
}
```

## 阶段 3：方案探索

收集需求后，提出 2-3 个方案：

```markdown
## 方案选项

### 方案 A：[名称]（推荐）
**优点：** ...
**缺点：** ...
**最适合：** ...

### 方案 B：[名称]
**优点：** ...
**缺点：** ...
**最适合：** ...

### 方案 C：[名称]
**优点：** ...
**缺点：** ...
**最适合：** ...
```

使用 AskUserQuestion 确认方案：

```json
{
  "questions": [
    {
      "question": "您想采用哪个方案？",
      "header": "方案选择",
      "multiSelect": false,
      "options": [
        { "label": "方案 A（推荐）", "description": "方案 A 的简要总结" },
        { "label": "方案 B", "description": "方案 B 的简要总结" },
        { "label": "方案 C", "description": "方案 C 的简要总结" }
      ]
    }
  ]
}
```

## 阶段 4：设计展示

分章节展示设计（每章节 300-500 字），每部分后进行验证：

1. **架构概览** - 高层结构
2. **数据模型** - 实体、关系、Schema
3. **API 设计** - 端点、请求/响应
4. **组件设计** - 内部模块、接口
5. **错误处理** - 错误场景、恢复策略
6. **测试策略** - 测试什么以及如何测试

每章节后使用 AskUserQuestion：

```json
{
  "questions": [
    {
      "question": "这个章节看起来正确吗？",
      "header": "审查",
      "multiSelect": false,
      "options": [
        { "label": "没问题", "description": "继续下一章节" },
        { "label": "小修改", "description": "需要小幅调整" },
        { "label": "大修改", "description": "需要重大修改" },
        { "label": "有疑问", "description": "需要在继续之前澄清" }
      ]
    }
  ]
}
```

## 阶段 5：文档与任务

### 保存设计文档

写入 `docs/designs/YYYY-MM-DD-<主题>-design.md`：

```markdown
# 功能：[名称]

## 摘要
[简要描述]

## 需求
[来自阶段 2 的回答]

## 架构
[来自阶段 4]

## 实施任务
[任务清单]
```

### 生成实施任务

```markdown
## 实施任务

- [ ] **任务标题** `priority:1` `phase:model` `time:15min`
  - 文件：src/file1.py, tests/test_file1.py
  - [ ] 为 X 编写失败的测试
  - [ ] 运行测试，验证失败
  - [ ] 实现最少代码
  - [ ] 运行测试，验证通过
  - [ ] 提交

- [ ] **另一个任务** `priority:2` `phase:api` `deps:任务标题` `time:10min`
  - 文件：src/api.py
  - [ ] 编写失败的测试
  - [ ] 实现并验证
  - [ ] 提交
```

## 阶段 6：执行交接

```json
{
  "questions": [
    {
      "question": "您希望如何进行实施？",
      "header": "下一步",
      "multiSelect": false,
      "options": [
        { "label": "立即执行", "description": "在当前会话中运行 /feature-pipeline" },
        { "label": "新会话", "description": "开始新会话进行实施" },
        { "label": "稍后", "description": "保存设计，稍后手动实施" },
        { "label": "修改设计", "description": "返回并修改设计" }
      ]
    }
  ]
}
```

## 关键原则

- **高效批量提问** - 適当时使用全部 4 个问题位
- **非排他选项使用多选** - 系统层、功能、需求
- **决策使用单选** - 方案、时间线、策略
- **标记推荐** - 在首选选项中添加"（推荐）"
- **渐进式细化** - 从一般到具体的问题
- **增量验证** - 在每个阶段检查理解
- **严格遵循 YAGNI** - 从设计中移除不必要的功能
