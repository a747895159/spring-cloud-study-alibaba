# 常见架构模式及其识别方法

## 概述

本参考文档旨在列出常见的软件系统架构模式，并提供在现有项目代码和文档中识别这些模式的线索和方法。理解项目所采用的架构模式对于分析项目结构、预测系统行为以及指导未来的开发和重构至关重要。

## 1. 分层架构 (Layered Architecture)

### 描述
最常见的架构模式之一，系统被划分为不同的逻辑层，每一层都对上层提供服务，并依赖下层提供服务。通常包括：表现层 (Presentation Layer)、业务逻辑层 (Business Logic Layer)、数据访问层 (Data Access Layer)。

### 识别线索
*   **目录结构**: 项目根目录下通常有 `controllers` / `views`, `services` / `business`, `models` / `repositories` / `dao` 等目录。
*   **依赖关系**: 高层模块（如控制器）依赖低层模块（如服务），服务层依赖数据访问层，反向依赖极少或没有。
*   **代码命名**: 类名或文件名常包含层级后缀，如 `UserController`, `UserService`, `UserRepository`。
*   **技术栈**: 常见于传统的 Web 应用框架 (如 Spring MVC, Django)。

## 2. 微服务架构 (Microservices Architecture)

### 描述
将单个应用程序拆分为一组小型服务，每个服务都在自己的进程中运行，并使用轻量级机制（通常是 HTTP API）进行通信。每个服务都围绕业务能力构建，并可独立部署。

### 识别线索
*   **多仓库/多模块**: 项目通常由多个独立的代码仓库或模块组成，每个对应一个服务。
*   **独立部署**: 存在多个 `Dockerfile`、独立部署脚本或 CI/CD 流水线，每个服务一个。
*   **API 网关**: 可能存在 API 网关 (如 Nginx, Zuul, Spring Cloud Gateway) 作为所有服务的统一入口。
*   **服务发现**: 使用服务发现机制 (如 Eureka, Consul, Kubernetes Service)。
*   **轻量级通信**: 服务间通常通过 RESTful API 或消息队列进行通信。
*   **数据库**: 每个微服务通常拥有自己的数据库或数据存储。

## 3. 事件驱动架构 (Event-Driven Architecture - EDA)

### 描述
系统通过异步发送、接收和处理事件来运行。组件之间通过事件进行解耦，事件生产者发布事件，事件消费者订阅事件并做出响应。

### 识别线索
*   **消息队列/事件总线**: 大量使用消息队列 (如 Kafka, RabbitMQ, SQS) 或事件总线。
*   **事件定义**: 代码中存在清晰的事件对象或事件类型定义。
*   **异步操作**: 业务流程中存在明显的异步操作和解耦。
*   **事件生产者/消费者**: 代码中存在事件发布者 (publishers) 和事件订阅者 (subscribers) 的模式。

## 4. 领域驱动设计 (Domain-Driven Design - DDD)

### 描述
一种软件开发方法，将重点放在将软件实现与核心业务领域模型紧密结合。强调领域模型、聚合根、实体、值对象、仓储和领域服务等概念。

### 识别线索
*   **目录结构**: 存在 `domain`, `application`, `infrastructure` 等目录。
*   **核心实体**: 代码中定义了丰富的实体 (Entities) 和值对象 (Value Objects)，且具有复杂的业务行为。
*   **聚合根**: 识别聚合根 (Aggregate Roots) 和限界上下文 (Bounded Contexts)。
*   **仓储模式**: 大量使用仓储 (Repositories) 来抽象数据访问。
*   **领域服务**: 存在封装领域业务逻辑的领域服务 (Domain Services)。

## 5. 客户端-服务器架构 (Client-Server Architecture)

### 描述
最基本的分布式架构，客户端向服务器请求服务，服务器提供服务。

### 识别线索
*   **前后端分离**: 存在独立的前端项目 (React, Vue, Angular) 和后端项目 (REST API)。
*   **HTTP/HTTPS 通信**: 客户端和服务器通过 HTTP/HTTPS 协议进行数据交换。
*   **API 调用**: 前端代码中大量存在对后端 API 的调用。

## 6. MVC/MVP/MVVM 模式 (Model-View-Controller/Presenter/ViewModel)

### 描述
用于将用户界面与业务逻辑分离的模式。
*   **MVC**: Model (数据/业务逻辑), View (用户界面), Controller (处理用户输入)。
*   **MVP**: Model, View, Presenter (将业务逻辑从 View 中抽取出来)。
*   **MVVM**: Model, View, ViewModel (通过数据绑定实现 View 和 ViewModel 的同步)。

### 识别线索
*   **目录结构**: 存在 `views` / `templates`, `controllers` / `presenters`, `models` 目录。
*   **代码职责**: 类和文件明确地按 MVC/MVP/MVVM 模式职责命名和划分。
*   **框架特性**: 常见于 Web 框架 (Django, Rails) 和前端框架 (Angular, Vue)。

## 识别方法论

*   **自顶向下**: 从高层目录结构和项目概览开始，逐步深入到代码细节。
*   **自底向上**: 从代码文件中的关键字、导入、类名和函数名开始，推断其所属的层级和职责。
*   **配置文件分析**: `package.json`, `pom.xml`, `requirements.txt` 等文件能快速揭示项目使用的框架和库，这些往往与特定架构模式相关。
*   **文档交叉验证**: 将从代码中推断出的架构模式与现有文档（如果存在）进行对比验证。
*   **关注点分离**: 寻找代码中关注点（如数据访问、业务逻辑、UI 渲染）是如何被分离和组织的。
