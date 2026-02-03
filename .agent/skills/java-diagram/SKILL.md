---
name: Java 设计画图专家
description: 使用 Mermaid 绘制 Java 代码的类图、时序图、流程图等，可视化展示代码结构和交互
---

# Java 设计画图专家技能

## 核心能力

根据 Java 代码自动生成：
- **类图**：展示类结构、继承关系、依赖关系
- **时序图**：展示方法调用流程、对象交互
- **流程图**：展示业务逻辑流程
- **ER 图**：展示实体关系（如有数据库实体）

## 1. 类图（Class Diagram）

### 基础类图
```mermaid
classDiagram
    class Order {
        -Long id
        -String orderNo
        -BigDecimal totalAmount
        -OrderStatus status
        +createOrder()
        +cancelOrder()
        +getOrderInfo()
    }
    
    class OrderItem {
        -Long id
        -Long orderId
        -Long skuId
        -Integer quantity
        -BigDecimal price
    }
    
    Order "1" --> "*" OrderItem : contains
```

### 继承关系
```mermaid
classDiagram
    class BaseEntity {
        <<abstract>>
        -Long id
        -Date createTime
        -Date updateTime
        +save()
        +update()
    }
    
    class Order {
        -String orderNo
        -BigDecimal totalAmount
        +createOrder()
    }
    
    class User {
        -String username
        -String email
        +login()
    }
    
    BaseEntity <|-- Order
    BaseEntity <|-- User
```

### 接口实现
```mermaid
classDiagram
    class OrderService {
        <<interface>>
        +createOrder(request)
        +cancelOrder(orderId)
        +queryOrder(orderId)
    }
    
    class OrderServiceImpl {
        -OrderDao orderDao
        -StockService stockService
        +createOrder(request)
        +cancelOrder(orderId)
        +queryOrder(orderId)
    }
    
    OrderService <|.. OrderServiceImpl : implements
    OrderServiceImpl --> OrderDao : uses
    OrderServiceImpl --> StockService : uses
```

### 关系符号说明
- `<|--` - 继承（实线三角箭头）
- `<|..` - 实现接口（虚线三角箭头）
- `-->` - 依赖/关联（实线箭头）
- `..>` - 依赖（虚线箭头）
- `--o` - 聚合（空心菱形）
- `--*` - 组合（实心菱形）

### 成员可见性
- `+` - public
- `-` - private
- `#` - protected
- `~` - package

## 2. 时序图（Sequence Diagram）

### 方法调用流程
```mermaid
sequenceDiagram
    participant Controller as OrderController
    participant Service as OrderServiceImpl
    participant Dao as OrderDao
    participant DB as Database
    
    Controller->>Service: createOrder(request)
    activate Service
    
    Service->>Service: validateRequest(request)
    Service->>Dao: selectByOrderNo(orderNo)
    activate Dao
    Dao->>DB: SELECT * FROM order
    DB-->>Dao: null
    deactivate Dao
    
    Service->>Dao: insert(order)
    activate Dao
    Dao->>DB: INSERT INTO order
    DB-->>Dao: order_id
    deactivate Dao
    
    Service-->>Controller: order_id
    deactivate Service
```

### 多对象交互
```mermaid
sequenceDiagram
    participant User
    participant OrderService
    participant StockService
    participant PaymentService
    participant MQ
    
    User->>OrderService: 创建订单
    OrderService->>StockService: 检查库存
    StockService-->>OrderService: 库存充足
    
    OrderService->>OrderService: 创建订单
    OrderService->>StockService: 扣减库存
    StockService-->>OrderService: 扣减成功
    
    OrderService->>PaymentService: 创建支付单
    PaymentService-->>OrderService: 支付单号
    
    OrderService->>MQ: 发送订单消息
    OrderService-->>User: 订单创建成功
```

### 条件和循环
```mermaid
sequenceDiagram
    participant Service
    participant Dao
    
    Service->>Dao: queryOrders()
    Dao-->>Service: orderList
    
    loop 遍历订单
        Service->>Service: processOrder(order)
        alt 订单有效
            Service->>Dao: updateOrder(order)
        else 订单无效
            Service->>Service: logError()
        end
    end
```

## 3. 流程图（Flowchart）

### 业务逻辑流程
```mermaid
flowchart TD
    Start([开始]) --> Input[接收订单请求]
    Input --> ValidateParam{参数校验}
    
    ValidateParam -->|失败| ReturnError[返回参数错误]
    ValidateParam -->|成功| CheckDuplicate{检查重复}
    
    CheckDuplicate -->|重复| ReturnError2[返回订单已存在]
    CheckDuplicate -->|不重复| CheckStock{检查库存}
    
    CheckStock -->|不足| ReturnError3[返回库存不足]
    CheckStock -->|充足| BeginTx[开启事务]
    
    BeginTx --> CreateOrder[创建订单]
    CreateOrder --> DeductStock[扣减库存]
    DeductStock --> InsertItems[插入订单明细]
    InsertItems --> CommitTx[提交事务]
    
    CommitTx --> SendMQ[发送MQ消息]
    SendMQ --> ReturnSuccess[返回成功]
    
    ReturnError --> End([结束])
    ReturnError2 --> End
    ReturnError3 --> End
    ReturnSuccess --> End
```

### 异常处理流程
```mermaid
flowchart TD
    Start[开始] --> Try[尝试执行业务]
    Try --> Success{执行成功?}
    
    Success -->|是| Return[返回结果]
    Success -->|否| CatchEx{异常类型}
    
    CatchEx -->|业务异常| LogWarn[记录警告日志]
    CatchEx -->|系统异常| LogError[记录错误日志]
    CatchEx -->|数据库异常| LogError
    
    LogWarn --> ReturnBizError[返回业务错误]
    LogError --> ReturnSysError[返回系统错误]
    
    Return --> End[结束]
    ReturnBizError --> End
    ReturnSysError --> End
```

## 4. 状态图（State Diagram）

### 订单状态流转
```mermaid
stateDiagram-v2
    [*] --> 待支付: 创建订单
    
    待支付 --> 已支付: 支付成功
    待支付 --> 已取消: 超时/用户取消
    
    已支付 --> 待发货: 商家确认
    已支付 --> 退款中: 用户申请退款
    
    待发货 --> 已发货: 商家发货
    已发货 --> 已完成: 用户确认收货
    已发货 --> 退货中: 用户申请退货
    
    退款中 --> 已退款: 退款成功
    退货中 --> 已退货: 退货成功
    
    已完成 --> [*]
    已取消 --> [*]
    已退款 --> [*]
    已退货 --> [*]
```

## 5. ER 图（Entity Relationship）

### 实体关系
```mermaid
erDiagram
    ORDER ||--o{ ORDER_ITEM : contains
    ORDER }o--|| USER : belongs_to
    ORDER_ITEM }o--|| PRODUCT : references
    
    ORDER {
        bigint id PK
        string order_no UK
        bigint user_id FK
        decimal total_amount
        string status
        datetime create_time
    }
    
    ORDER_ITEM {
        bigint id PK
        bigint order_id FK
        bigint sku_id FK
        int quantity
        decimal price
    }
    
    USER {
        bigint id PK
        string username UK
        string email
    }
    
    PRODUCT {
        bigint id PK
        string name
        decimal price
        int stock
    }
```

## 使用场景

### 场景 1：分析现有代码结构
**输入**：Java 类代码
**输出**：类图（展示类结构、继承、依赖关系）

### 场景 2：理解方法调用流程
**输入**：Service 层方法代码
**输出**：时序图（展示方法调用顺序和对象交互）

### 场景 3：梳理业务逻辑
**输入**：业务处理方法
**输出**：流程图（展示业务逻辑分支和异常处理）

### 场景 4：设计数据模型
**输入**：Entity 类代码
**输出**：ER 图（展示实体关系）

### 场景 5：分析状态流转
**输入**：状态枚举和流转逻辑
**输出**：状态图（展示状态变化）

## 绘图原则

### 1. 类图
- 只展示关键属性和方法
- 突出类之间的关系
- 使用正确的可见性符号

### 2. 时序图
- 按时间顺序从上到下
- 使用 activate/deactivate 表示方法执行
- 区分同步调用（实线）和异步调用（虚线）

### 3. 流程图
- 清晰的开始和结束
- 判断节点使用菱形
- 异常流程要完整

### 4. ER 图
- 标注主键（PK）、外键（FK）、唯一键（UK）
- 使用正确的关系符号
- 包含关键字段

## 输出格式

```markdown
## [功能/类名]设计图

### 类图
```mermaid
classDiagram
    [类图代码]
```

### 时序图
```mermaid
sequenceDiagram
    [时序图代码]
```

### 流程图
```mermaid
flowchart TD
    [流程图代码]
```

### 说明
[对图表的关键说明]
```

## 检查清单

- [ ] 图表类型选择正确
- [ ] Mermaid 语法正确
- [ ] 类图包含关键属性和方法
- [ ] 时序图展示完整调用流程
- [ ] 流程图包含异常处理
- [ ] ER 图标注主键外键
- [ ] 图表清晰易懂

## 快速参考

| 需求 | 图表类型 | 关键字 |
|-----|---------|--------|
| 展示类结构 | 类图 | `classDiagram` |
| 展示方法调用 | 时序图 | `sequenceDiagram` |
| 展示业务逻辑 | 流程图 | `flowchart TD` |
| 展示状态变化 | 状态图 | `stateDiagram-v2` |
| 展示实体关系 | ER 图 | `erDiagram` |
