---
name: BUG 定位与修复
description: 系统化地定位和修复 BUG,确保问题彻底解决且不引入新问题
---

# BUG 定位与修复技能

## 诊断流程

### 1. 问题理解
收集以下信息：
- **现象**：具体的错误表现
- **复现步骤**：如何触发
- **预期行为**：正确行为是什么
- **影响范围**：影响哪些功能
- **错误信息**：异常堆栈、日志、错误码

### 2. 问题定位

#### 常见 BUG 类型

| BUG 类型 | 常见原因 | 定位方法 |
|---------|---------|---------|
| NullPointerException | 未判空 | 查看堆栈,找到为 null 的对象 |
| IndexOutOfBoundsException | 未检查集合大小 | 检查索引计算和边界 |
| ConcurrentModificationException | 遍历时修改集合 | 检查循环中的修改操作 |
| ClassCastException | 类型不匹配 | 检查类型转换 |
| DuplicateKeyException | 唯一索引冲突 | 检查数据库约束 |
| DeadlockException | 死锁 | 分析锁顺序 |

#### 定位技巧
- **二分法**：通过日志缩小范围
- **对比法**：对比正常和异常场景
- **日志追踪**：追踪数据流转
- **单元测试**：编写测试复现问题

### 3. 根因分析

**5 Why 分析法**：
```
问题：订单创建失败
Why 1: 为什么失败？ → 库存扣减失败
Why 2: 为什么扣减失败？ → 库存不足
Why 3: 为什么库存不足？ → 库存数据不准确
Why 4: 为什么不准确？ → 并发扣减时没有加锁
Why 5: 为什么没有加锁？ → 设计时未考虑并发

根因：并发控制缺失
```

### 4. 修复方案

#### 修复模板

**空指针修复**
```java
// 修复前
Order order = orderDao.selectById(orderId);
order.getStatus();  // NPE

// 修复后
Order order = orderDao.selectById(orderId);
if (order == null) {
    log.warn("订单不存在, orderId={}", orderId);
    throw new BusinessException("订单不存在");
}
```

**集合操作修复**
```java
// 修复前
OrderItem firstItem = items.get(0);  // 越界

// 修复后
if (CollectionUtils.isEmpty(items)) {
    throw new BusinessException("订单明细不能为空");
}
OrderItem firstItem = items.get(0);
```

**并发修改修复**
```java
// 修复前
for (OrderItem item : order.getItems()) {
    if (item.getQuantity() == 0) {
        order.getItems().remove(item);  // 并发修改异常
    }
}

// 修复后
Iterator<OrderItem> iterator = order.getItems().iterator();
while (iterator.hasNext()) {
    if (iterator.next().getQuantity() == 0) {
        iterator.remove();
    }
}
```

**数据库并发修复**
```java
// 修复前
orderDao.update(order);  // 并发更新可能丢失

// 修复后（乐观锁）
int updated = orderDao.updateByVersion(order);
if (updated == 0) {
    throw new ConcurrentModificationException("订单已被修改");
}
```

**异常处理修复**
```java
// 修复前
try {
    processOrder(order);
} catch (Exception e) {
    // 吞掉异常
}

// 修复后
try {
    processOrder(order);
} catch (BusinessException e) {
    log.warn("业务异常, error={}", e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("系统异常, orderId={}", order.getId(), e);
    throw new SystemException("订单处理失败", e);
}
```

### 5. 验证与预防
- ✅ 复现场景验证
- ✅ 正常场景验证
- ✅ 边界场景验证
- ✅ 添加单元测试
- ✅ 完善日志
- ✅ 检查类似代码

## 输出格式

```markdown
## BUG 修复报告

### 问题描述
- 现象：[错误表现]
- 复现：[触发步骤]
- 影响：[影响范围]

### 错误信息
[异常堆栈或日志]

### 问题定位
- BUG 类型：[类型]
- 位置：[文件:行号]
- 触发条件：[条件]

### 根因分析
- 直接原因：[代码层面]
- 根本原因：[设计层面]

### 修复方案
#### 修复前
[有问题的代码]

#### 修复后
[修复后的完整代码]

### 验证结果
- ✅ 原问题已解决
- ✅ 正常功能不受影响
- ✅ 已添加单元测试

### 预防措施
- [ ] 添加单元测试
- [ ] 完善日志
- [ ] 检查类似代码
```

## 检查清单
- [ ] 问题彻底解决
- [ ] 未引入新问题
- [ ] 异常处理完整
- [ ] 日志完善
- [ ] 边界条件处理
- [ ] 已添加测试
