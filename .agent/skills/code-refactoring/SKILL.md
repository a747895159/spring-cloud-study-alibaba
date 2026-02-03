---
name: 代码重构专家
description: 系统化地重构代码,提升可维护性、性能和代码质量
---

# 代码重构专家技能

## 重构原则
1. **小步快跑**：每次重构一个小目标
2. **保持功能**：不改变外部行为
3. **渐进式**：优先重构高价值、低风险部分

## 何时重构
- ✅ 代码重复
- ✅ 方法过长（> 100 行）
- ✅ 类职责不清
- ✅ 参数过多（> 8 个）
- ✅ 嵌套过深（> 5 层）
- ✅ 性能问题

## 重构模式

### 1. 提取方法
**场景**：方法过长

```java
// 重构前
public void processOrder(Order order) {
    // 校验（10 行）
    // 计算（10 行）
    // 保存（10 行）
}

// 重构后
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotalAmount(order);
    saveOrder(order);
}
```

### 2. 参数对象化
**场景**：参数过多

```java
// 重构前
public List<Order> queryOrders(String orderNo, String status, 
    Long userId, Date startTime, Date endTime, Integer pageNo, Integer pageSize) {
}

// 重构后
public List<Order> queryOrders(OrderQueryRequest request) {
}
```

### 3. 消除重复代码
**场景**：多处相似代码

```java
// 重构前
public void cancelOrder(Long orderId) {
    Order order = orderDao.selectById(orderId);
    if (order == null) throw new BusinessException("订单不存在");
    if (!"PENDING".equals(order.getStatus())) throw new BusinessException("状态不允许取消");
}

public void deleteOrder(Long orderId) {
    Order order = orderDao.selectById(orderId);
    if (order == null) throw new BusinessException("订单不存在");
    if (!"PENDING".equals(order.getStatus())) throw new BusinessException("状态不允许删除");
}

// 重构后
private Order getPendingOrder(Long orderId, String operation) {
    Order order = orderDao.selectById(orderId);
    if (order == null) throw new BusinessException("订单不存在");
    if (!"PENDING".equals(order.getStatus())) {
        throw new BusinessException("订单状态不允许" + operation);
    }
    return order;
}
```

### 4. 简化条件表达式
**场景**：复杂嵌套

```java
// 重构前
if (order != null) {
    if (order.getStatus() != null) {
        if ("PAID".equals(order.getStatus())) {
            if (order.getShipTime() != null) {
                return "已发货";
            }
        }
    }
}

// 重构后
if (order == null || order.getStatus() == null) {
    return "未知";
}
if ("PAID".equals(order.getStatus())) {
    return order.getShipTime() != null ? "已发货" : "待发货";
}
```

### 5. 引入策略模式
**场景**：大量 if-else

```java
// 重构前
public BigDecimal calculateDiscount(Order order, String type) {
    if ("VIP".equals(type)) {
        return order.getTotalAmount().multiply(new BigDecimal("0.8"));
    } else if ("COUPON".equals(type)) {
        return order.getTotalAmount().subtract(new BigDecimal("10"));
    }
    return order.getTotalAmount();
}

// 重构后
public interface DiscountStrategy {
    BigDecimal calculate(Order order);
}

public class DiscountService {
    private Map<String, DiscountStrategy> strategies = new HashMap<>();
    
    public BigDecimal calculateDiscount(Order order, String type) {
        DiscountStrategy strategy = strategies.get(type);
        return strategy != null ? strategy.calculate(order) : order.getTotalAmount();
    }
}
```

### 6. 性能优化
**场景**：N+1 查询

```java
// 重构前
for (Long orderId : orderIds) {
    Order order = orderDao.selectById(orderId);  // N+1
}

// 重构后
List<Order> orders = orderDao.selectByIds(orderIds);  // 批量查询
```

## 输出格式

```markdown
## 代码重构报告

### 重构目标
[要解决的问题]

### 问题分析
- 当前问题：[列出问题]
- 重构收益：[改进点]

### 重构方案
- 重构模式：[使用的模式]
- 改动范围：[涉及的文件]

### 重构前代码
[关键代码]

### 重构后代码
[完整代码]

### 改进说明
- ✅ [改进点 1]
- ✅ [改进点 2]
```

## 检查清单
- [ ] 功能保持不变
- [ ] 代码更易读
- [ ] 消除重复
- [ ] 方法长度合理
- [ ] 异常处理完整
- [ ] 性能不降低
