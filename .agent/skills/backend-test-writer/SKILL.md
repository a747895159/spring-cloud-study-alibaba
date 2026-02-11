---
name: backend-test-writer
description: 为后端代码（Express 路由、MongoDB 模型、Node 服务）生成测试时使用 - 分析文件类型，从 package.json 检测测试框架，生成包含设置/拆卸和边缘情况覆盖的全面测试
---

# 后端测试编写器

为 MERN 技术栈代码生成全面的后端测试。分析文件类型，检测项目约定，生成可直接运行的测试。

**理念：** 智能默认值，零配置。从项目中自动检测一切。

<workflow>

## 工作流程

复制并跟踪进度：

- [ ] 阶段 0：基础设施检查
- [ ] 阶段 1：分析目标文件
- [ ] 阶段 2：生成测试
- [ ] 阶段 3：输出报告摘要

### 阶段 0：基础设施检查

生成测试前，验证环境：

1. 检查 `package.json` 中的测试框架（Jest/Vitest/Mocha）
2. 如果没有 → 提示："是否设置 Jest + Supertest？"
3. 检查 `mongodb-memory-server`（集成测试需要）
4. 检测测试文件约定（同目录、`__tests__/` 或 `tests/`）

**停止条件：** 无测试框架且用户拒绝设置。

### 阶段 1：分析目标文件

| 模式 | 类型 | 测试方法 |
|------|------|----------|
| `routes/`、`*.routes.js` | 路由 | 集成测试（Supertest + 真实数据库） |
| `controllers/` | 控制器 | 集成测试 |
| `services/` | 服务 | 单元测试（mock 依赖） |
| `models/`、`*.model.js` | 模型 | 单元测试（验证测试） |
| `middleware/` | 中间件 | 单元测试（mock req/res/next） |
| `utils/`、`helpers/` | 工具 | 单元测试（纯函数） |

**覆盖：** 用户可指定 `--unit` 或 `--integration`。

### 阶段 2：生成测试

按顺序处理文件并显示进度。用户可随时停止。

**每个测试包含：**
- 检测到的框架对应的正确导入
- 设置/拆卸（数据库连接、清理）
- 全面覆盖：
  - 成功场景（正常路径）
  - 验证错误（400）
  - 未找到（404）
  - 认证失败（401/403）（如果受保护）
  - 边缘情况（重复、空值、null）

**参考：** 完整代码示例请参阅 [test-patterns.md](reference/test-patterns.md)。

### 阶段 3：报告

```
已生成：X 个测试文件
覆盖：共 Y 个测试用例
下一步：运行 `npm test` 验证
```

</workflow>

<quick-reference>

## 快速参考

| 文件类型 | 导入 | 数据库设置 |
|----------|------|-----------|
| 路由 | `supertest`、`mongodb-memory-server` | 真实（内存数据库） |
| 服务 | `jest` | Mock |
| 模型 | `mongoose` | Mock |
| 中间件 | `jest` | 无 |

## 测试结构模式

```javascript
describe('[资源] [方法]', () => {
  describe('success cases', () => {
    it('should [expected behavior]', async () => {});
  });
  describe('validation errors', () => {
    it('should return 400 for [invalid case]', async () => {});
  });
  describe('edge cases', () => {
    it('should handle [edge case]', async () => {});
  });
});
```

</quick-reference>

<checklists>

## 检查清单

### 基础设施（优先检查）
- [ ] `package.json` 中有测试框架
- [ ] 已定义测试脚本（`"test": "jest"`）
- [ ] 已安装 Supertest（集成测试）
- [ ] 已安装 mongodb-memory-server（数据库测试）

### 每个文件的生成检查
- [ ] 先检查是否存在已有测试（如有则进行差距分析）
- [ ] 文件类型对应的正确导入
- [ ] 包含设置/拆卸
- [ ] 测试了正常路径
- [ ] 测试了错误情况（400、404、401）
- [ ] 测试了边缘情况（领域特定：日期→夏令时/时区，金额→精度等）
- [ ] 断言中无密钥
- [ ] 正确处理 async/await
- [ ] 为每个测试分配优先级（P0=关键、P1=重要、P2=锦上添花）
- [ ] 建议使用测试辅助工厂处理复杂输入

</checklists>

<common-mistakes>

## 常见错误

| 错误 | 修复 |
|------|------|
| 在测试中启动服务器 | 导入 app，让 Supertest 处理 |
| 无数据库清理 | 添加 `afterEach` 和 `deleteMany({})` |
| 测试实现细节 | 通过 HTTP 接口测试行为 |
| 缺少 async/await | 等待异步操作 |
| 在集成测试中使用 mock | 集成测试使用真实数据库 |

</common-mistakes>

<guidelines>

## 准则

- 不要为未阅读的代码生成测试
- 不要跳过基础设施检查
- 不要只生成正常路径测试
- 不要忘记测试间的清理

</guidelines>

<references>

## 参考文件

实现特定模式时加载：

| 场景 | 参考文件 |
|------|----------|
| 编写任何测试 | [test-patterns.md](reference/test-patterns.md) |
| 设置测试基础设施 | [test-setup.md](reference/test-setup.md) |

</references>
