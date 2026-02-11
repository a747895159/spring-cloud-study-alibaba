---
name: project-testing
description: "{project} 的自定义测试模式和测试数据。涵盖端到端、集成以及特殊测试需求。"
generated_by: ai-dev-kit:recommend-skills
---

<!-- 由 ai-dev-kit:recommend-skills 于 {date} 生成 -->
<!-- 如不再需要，可安全删除此技能 -->

# {project} 测试模式

本项目的自定义测试模式、测试数据和策略。

## 变量

| 变量 | 默认值 | 描述 |
|------|--------|------|
| COVERAGE_TARGET | 80 | 最低覆盖率百分比 |
| E2E_TIMEOUT | 30000 | 端到端测试超时时间（毫秒） |
| PARALLEL_TESTS | true | 尽可能并行运行测试 |

## 使用说明

1. 确定所需测试类型（单元/集成/端到端）
2. 使用合适的测试数据和模式
3. 遵循项目命名规范
4. 确保正确清理

## 危险信号 - 停下来重新考虑

如果你即将：
- 编写未正确隔离的测试
- 跳过测试数据的清理
- 硬编码测试数据而非使用测试工厂
- 编写不稳定的测试（依赖时序、依赖顺序）

**停下来** -> 使用正确的测试数据 -> 确保隔离 -> 然后编写测试

## 测试分类

### 单元测试

位置：`tests/unit/`

模式：
- 在隔离环境中测试单个函数/方法
- Mock 外部依赖
- 执行速度快（每个 < 100ms）

### 集成测试

位置：`tests/integration/`

模式：
- 测试组件间交互
- 使用测试数据库/测试数据
- 可能有外部依赖

### 端到端测试

位置：`tests/e2e/` 或 `playwright/`

模式：
- 测试完整的用户工作流
- 使用浏览器自动化
- 可接受较长的执行时间

## 测试数据

### 数据库测试数据

位置：`tests/fixtures/`

用法：
```python
# Python 示例
from tests.fixtures import sample_user, sample_order

def test_order_creation(sample_user, sample_order):
    # 测试使用预配置的测试数据
    pass
```

### Mock 服务

位置：`tests/mocks/`

可用的 mock：
- [TODO: 列出项目特定的 mock]

## 命名规范

| 测试类型 | 文件模式 | 函数模式 |
|----------|----------|----------|
| 单元测试 | `test_*.py` | `test_<function>_<scenario>` |
| 集成测试 | `test_*_integration.py` | `test_<component>_<action>` |
| 端到端测试 | `*.spec.ts` | `test('<feature> - <scenario>')` |

## 覆盖率要求

| 组件 | 最低覆盖率 |
|------|-----------|
| 核心逻辑 | 90% |
| API 路由 | 80% |
| 工具函数 | 70% |

## CI 集成

测试在 CI 中运行：
- PR 提交时：单元测试 + 集成测试
- 合并时：包括端到端在内的所有测试
- 每日夜间：完整回归测试套件

## 自定义

编辑此文件以添加：
- 新的测试数据定义
- 额外的 Mock 服务
- 自定义测试模式
- 覆盖率例外
