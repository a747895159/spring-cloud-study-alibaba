---
name: 功能实现专家
description: 系统化地实现新功能，确保完整性、健壮性和可维护性
---

# 功能实现专家技能

## 实施流程

### 1. 需求分析（必须）
- **输入输出**：参数类型、返回值、必填项
- **业务规则**：核心逻辑、计算规则、状态流转
- **边界条件**：空值、空集合、极值、并发
- **依赖服务**：需要调用的服务/DAO/接口

**信息不足处理**：
- 用 `// TODO: [说明]` 标注
- 提供 2 种可选方案并说明适用场景

### 2. 分层设计
```
Controller/AppService  → 参数校验、权限校验、异常转换
    ↓
Service/Domain        → 业务逻辑、事务控制
    ↓
DAO/Repository        → 数据访问
```

### 3. 编码实现（必须包含）

#### 参数校验
```java
if (request == null) {
    throw new IllegalArgumentException("请求参数不能为空");
}
if (StringUtils.isBlank(request.getOrderNo())) {
    throw new IllegalArgumentException("订单号不能为空");
}
```

#### 异常处理
```java
try {
    return processOrder(request);
} catch (DuplicateKeyException e) {
    log.error("订单重复, orderNo={}", request.getOrderNo(), e);
    throw new BusinessException("订单已存在");
} catch (DataAccessException e) {
    log.error("数据库异常, request={}", request, e);
    throw new SystemException("系统繁忙,请稍后重试");
}
```

#### 日志记录
```java
log.info("开始处理订单, orderNo={}", request.getOrderNo());
log.info("订单处理完成, orderId={}", orderId);
```

#### 事务控制
```java
@Transactional(rollbackFor = Exception.class)
public Long createOrder(CreateOrderRequest request) {
    // 业务逻辑
}
```

#### 并发控制
```java
// 乐观锁
int updated = orderDao.updateByVersion(orderId, newStatus, oldVersion);
if (updated == 0) {
    throw new ConcurrentModificationException("订单状态已变更");
}
```

### 4. 完整性检查
- ✅ 接口定义（Interface）
- ✅ 接口实现（Impl）
- ✅ 参数对象（Request/DTO）
- ✅ 返回对象（Response/VO）
- ✅ DAO 和 Mapper.xml（如需要）
- ✅ 边界条件处理（空值、并发、异常）

## 输出格式

```markdown
## 功能设计说明

### 接口设计
- 方法：`ReturnType methodName(ParamType param)`
- 输入：[说明]
- 输出：[说明]
- 异常：[可能的异常]

### 实现方案
[实现思路和关键逻辑]

### 边界处理
- 空值：[处理方式]
- 并发：[处理方式]
- 异常：[处理方式]

### TODO（如有）
- [ ] TODO: [说明]
  - 方案 A: [说明]
  - 方案 B: [说明]

## 核心代码
[完整的接口、实现类、参数对象、DAO、Mapper]

## 边界与异常
[列出所有边界条件和异常的处理方式]
```

## 代码模板

### AppService 层
```java
@Service
@Slf4j
public class OrderAppServiceImpl implements OrderAppService {
    @Autowired
    private OrderService orderService;
    
    @Override
    public Result<CreateOrderResponse> createOrder(CreateOrderRequest request) {
        // 1. 参数校验
        validateRequest(request);
        
        try {
            // 2. 调用服务
            Long orderId = orderService.createOrder(convertToDTO(request));
            
            // 3. 构造返回值
            return Result.success(new CreateOrderResponse(orderId));
            
        } catch (BusinessException e) {
            log.warn("业务异常, error={}", e.getMessage());
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("系统异常, request={}", request, e);
            return Result.fail("系统异常");
        }
    }
}
```

### Service 层
```java
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderDao orderDao;
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Long createOrder(CreateOrderDTO dto) {
        log.info("开始创建订单, dto={}", dto);
        
        // 1. 业务校验
        validateBusiness(dto);
        
        // 2. 构造实体
        OrderEntity order = buildOrderEntity(dto);
        
        // 3. 持久化
        orderDao.insert(order);
        
        log.info("订单创建成功, orderId={}", order.getId());
        return order.getId();
    }
}
```

## 检查清单
- [ ] 参数校验完整
- [ ] 异常处理完整
- [ ] 日志记录完整
- [ ] 事务边界正确
- [ ] 并发场景考虑
- [ ] 空值/空集合处理
- [ ] 代码可直接运行
- [ ] TODO 已标注
