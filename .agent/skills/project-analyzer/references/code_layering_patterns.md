# 常见代码分层模式及其职责定义

## 概述

本参考文档旨在介绍软件项目中常见的代码分层模式，并明确各层级的职责，以帮助理解和分析现有项目的内部结构。清晰的分层有助于提高代码的可维护性、可扩展性和可测试性。

## 1. 经典三层架构 (Three-Tier Architecture)

### 描述
将应用程序逻辑划分为三个主要层：表示层、业务逻辑层和数据访问层。

*   **表示层 (Presentation Layer / UI Layer)**
    *   **职责**: 负责与用户交互，显示信息和接收用户输入。不包含业务逻辑。
    *   **典型组件**: Web 页面、桌面应用界面、移动应用界面、控制器 (Controller)。
    *   **识别线索**: 包含 View 文件 (如 HTML/JSP/Vue/React 组件)、Controller 文件、路由定义、用户界面相关的代码。

*   **业务逻辑层 (Business Logic Layer / Service Layer)**
    *   **职责**: 实现核心业务规则和业务流程，协调数据访问层和表示层。
    *   **典型组件**: 服务 (Service)、业务逻辑对象、业务规则引擎。
    *   **识别线索**: 包含 Service 文件、处理业务流程的类、DTO (Data Transfer Object) 转换、事务管理。

*   **数据访问层 (Data Access Layer / Persistence Layer)**
    *   **职责**: 负责与数据存储进行交互，执行数据的 CRUD (创建、读取、更新、删除) 操作，并处理数据映射。
    *   **典型组件**: 数据访问对象 (DAO)、仓储 (Repository)、ORM (Object-Relational Mapping) 配置。
    *   **识别线索**: 包含 Repository 文件、DAO 文件、数据库连接配置、实体类定义、数据迁移脚本。

### 识别示例
*   **目录**: `controllers/`, `services/`, `repositories/` 或 `dao/`。
*   **命名**: `UserController`, `UserService`, `UserRepository`。
*   **依赖**: `Controller` -> `Service` -> `Repository`。

## 2. 领域驱动设计 (Domain-Driven Design - DDD) 的分层

### 描述
DDD 强调将业务复杂性置于核心领域模型中，其分层通常更为细致。

*   **用户界面层 (User Interface Layer)**
    *   **职责**: 与经典三层架构的表示层类似，负责用户交互。

*   **应用层 (Application Layer)**
    *   **职责**: 协调领域层对象执行特定应用用例，不包含业务逻辑，只协调领域对象完成任务。
    *   **典型组件**: 应用服务 (Application Service)。
    *   **识别线索**: 方法名通常对应用户用例（如 `createUser`, `placeOrder`），内部调用领域服务或仓储。

*   **领域层 (Domain Layer)**
    *   **职责**: 包含核心业务逻辑、领域模型 (实体、值对象、聚合根) 和领域服务。与数据持久化无关。
    *   **典型组件**: 实体 (Entity)、值对象 (Value Object)、聚合根 (Aggregate Root)、领域服务 (Domain Service)、规约 (Specification)。
    *   **识别线索**: 复杂的业务对象拥有行为而非仅仅数据、业务规则的集中体现。

*   **基础设施层 (Infrastructure Layer)**
    *   **职责**: 为其他层提供通用技术能力，如数据持久化、消息传递、外部服务集成等。
    *   **典型组件**: 仓储实现 (Repository Implementations)、消息队列客户端、HTTP 客户端。
    *   **识别线索**: 数据库连接、ORM 配置、外部 API 客户端、日志和监控配置。

### 识别示例
*   **目录**: `ui/`, `application/`, `domain/`, `infrastructure/`。
*   **命名**: `UserApplicationService`, `OrderAggregate`, `UserRepositoryImpl`。

## 3. 清洁架构 (Clean Architecture) / 六边形架构 (Hexagonal Architecture)

### 描述
强调关注点分离，将业务规则与外部世界（如数据库、UI、第三方服务）隔离。核心业务逻辑位于最内部的圈层，外部适配器负责与外部系统交互。

*   **实体 (Entities)**
    *   **职责**: 封装企业范围的业务规则。

*   **用例 (Use Cases / Interactors)**
    *   **职责**: 封装应用程序特定的业务规则，协调实体完成用例。

*   **接口适配器 (Interface Adapters)**
    *   **职责**: 将用例层的输入/输出转换为外部系统（如 Web、数据库）所需的格式。
    *   **典型组件**: 控制器 (Controller)、Presenter、Gateway (Repository 接口)。

*   **框架和驱动 (Frameworks & Drivers)**
    *   **职责**: 外部系统，如数据库、Web 框架、UI 框架。

### 识别线索
*   **依赖规则**: 依赖关系总是指向内部。核心业务逻辑不依赖外部。
*   **接口/实现分离**: 大量使用接口 (Interface) 和其具体实现 (Implementation) 来实现依赖倒置。
*   **端口和适配器**: 代码中存在“端口” (Ports，即接口) 和“适配器” (Adapters，即实现)。
*   **目录**: `core/domain`, `core/application`, `adapters/web`, `adapters/persistence`。

## 4. 微服务架构中的分层 (Layers in Microservices)

### 描述
虽然微服务本身是横向扩展的，但每个独立的微服务内部仍可能采用分层架构。

### 识别线索
*   每个微服务内部可以是一个独立的三层架构或 DDD 分层。
*   服务间通过 API 网关和消息队列进行通信，而非直接依赖。

## 识别方法论

*   **目录结构扫描**: 首先查看项目根目录和主要模块的目录结构，这是分层最直观的体现。
*   **依赖关系分析**: 
    *   **导入/引用分析**: 查看文件顶部的 `import` 语句或代码中的类实例化，判断模块间的依赖方向。
    *   **代码追踪**: 从一个入口点（如 Controller）开始，逐步向下追踪其调用的服务、数据访问，以识别调用链和分层边界。
*   **关键字搜索**: 搜索 `Service`, `Repository`, `Controller`, `DAO`, `Impl`, `Gateway`, `Adapter`, `Domain` 等关键字，它们通常是分层组件的命名约定。
*   **文件内容分析**: 阅读文件的功能描述、类和方法的职责，判断其所属的逻辑层。
*   **技术栈线索**: 特定框架（如 Spring Boot, Django, NestJS）往往自带推荐的分层结构。

## 评估与重构建议

*   **职责是否清晰**: 各层级和模块的职责是否单一，没有职责重叠或混淆。
*   **依赖是否合理**: 依赖关系是否单向且符合分层原则，是否存在循环依赖或跨层依赖。
*   **耦合度**: 各层级之间的耦合度是否低，修改一层是否会过多影响其他层。
*   **测试性**: 核心业务逻辑是否易于独立测试。
*   **可扩展性**: 是否容易在现有层级中添加新功能或引入新技术。

通过以上方法，可以有效地理解项目的代码分层和架构模式，为后续的工作提供宝贵的见解。
