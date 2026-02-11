---
name: "doc-translator"
description: "Translates English documents to Chinese with accurate semantics and grammar. Invoke when user asks to translate any English documentation or content to Chinese."
---

# 文档翻译专家

本技能专门用于将英语文档翻译成中文，确保语义和语法准确，适用于各类技术文档、用户手册、API文档等。

## 翻译原则

### 核心要求
- **语义准确**：确保翻译后的中文含义与原文完全一致
- **语法正确**：使用符合中文表达习惯的语法结构
- **技术术语**：保持技术术语的准确性和一致性
- **代码保留**：所有代码块保持原样，不进行翻译
- **格式完整**：保持原文的文档格式和结构

### 翻译策略

#### 1. 术语处理

**保持英文的技术术语：**
- 编程语言：Python, Java, JavaScript, Go, Rust等
- 框架和库：Spring Boot, React, Vue, Django, Flask等
- 技术概念：API, HTTP, REST, JSON, XML, SQL, NoSQL等
- 开发工具：Git, Maven, Gradle, Docker, Kubernetes等
- 云服务：AWS, Azure, GCP等
- 数据库：MySQL, PostgreSQL, MongoDB, Redis等
- 协议：TCP/IP, UDP, HTTPS, WebSocket等

**可翻译的术语：**
- 通用技术概念：authentication → 认证, authorization → 授权, deployment → 部署
- 操作描述：configure → 配置, install → 安装, setup → 设置

#### 2. 代码块处理

**保持原样的内容：**
```python
def hello_world():
    print("Hello, World!")
```

```bash
npm install react
```

```json
{
  "name": "my-app",
  "version": "1.0.0"
}
```

**可翻译的内容：**
- 代码注释可以翻译为中文
```python
# Calculate the total price
def calculate_total(items):
    return sum(item.price for item in items)
```
翻译为：
```python
# 计算总价
def calculate_total(items):
    return sum(item.price for item in items)
```

#### 3. 格式保持

- **Markdown格式**：保持标题层级、列表、表格、链接等
- **代码块**：保持语言标识符和缩进
- **链接**：保持URL和链接文本（可翻译链接文本）
- **图片**：保持图片链接和alt文本（可翻译alt文本）

#### 4. 语言风格

- 使用清晰、专业的中文表达
- 避免直译，注重中文表达的自然流畅
- 保持技术文档的专业性和准确性
- 使用一致的术语翻译

## 翻译流程

### 第一步：分析原文结构

1. **识别文档类型**
   - 技术文档（API文档、开发指南、架构文档）
   - 用户手册（安装指南、使用说明）
   - 项目文档（README、CHANGELOG）
   - 其他类型

2. **理解文档结构**
   - 章节划分
   - 段落组织
   - 代码块位置
   - 特殊格式元素

3. **识别关键内容**
   - 技术术语
   - 代码块
   - 命令行指令
   - 文件路径

### 第二步：分段翻译

1. **按章节翻译**
   - 保持章节顺序
   - 确保上下文连贯性
   - 保持术语一致性

2. **处理特殊元素**
   - 代码块：保持原样
   - 链接：保持URL，翻译链接文本
   - 表格：翻译内容，保持格式
   - 列表：翻译列表项

3. **术语一致性检查**
   - 同一术语使用相同翻译
   - 建立术语表（可选）
   - 参考行业标准翻译

### 第三步：质量检查

1. **语义检查**
   - 确保翻译与原文含义一致
   - 检查是否有遗漏或错误理解
   - 验证技术概念的准确性

2. **语法检查**
   - 检查句子结构是否正确
   - 确保表达流畅自然
   - 消除翻译腔

3. **格式检查**
   - 验证Markdown格式完整
   - 确认代码块未被误译
   - 检查链接和引用

4. **术语检查**
   - 确保技术术语使用准确
   - 检查术语翻译的一致性
   - 验证是否符合行业标准

## 翻译示例

### 示例1：技术文档

**原文（英语）：**
```markdown
# Getting Started

This guide will help you set up the development environment for the project.

## Prerequisites

Before you begin, make sure you have the following installed:
- Node.js 16 or higher
- npm 8 or higher
- Git

## Installation

1. Clone the repository:
```bash
git clone https://github.com/example/project.git
cd project
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The application will be available at http://localhost:3000
```

**翻译后（中文）：**
```markdown
# 快速开始

本指南将帮助您设置项目的开发环境。

## 前置要求

在开始之前，请确保已安装以下软件：
- Node.js 16 或更高版本
- npm 8 或更高版本
- Git

## 安装步骤

1. 克隆仓库：
```bash
git clone https://github.com/example/project.git
cd project
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

应用程序将在 http://localhost:3000 上运行
```

### 示例2：API文档

**原文（英语）：**
```markdown
## User API

### Create User

Creates a new user account.

**Endpoint:** `POST /api/users`

**Request Body:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**Response:**
```json
{
  "id": 123,
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```
```

**翻译后（中文）：**
```markdown
## 用户 API

### 创建用户

创建新的用户账户。

**端点：** `POST /api/users`

**请求体：**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "securepassword123"
}
```

**响应：**
```json
{
  "id": 123,
  "username": "johndoe",
  "email": "john@example.com",
  "createdAt": "2024-01-15T10:30:00Z"
}
```
```

### 示例3：代码注释翻译

**原文（英语）：**
```python
class UserService:
    """Service class for managing user operations."""
    
    def __init__(self, db_connection):
        """Initialize the service with database connection."""
        self.db = db_connection
    
    def get_user(self, user_id):
        """Retrieve user by ID from the database.
        
        Args:
            user_id: The unique identifier of the user
            
        Returns:
            User object if found, None otherwise
        """
        # Query the database for the user
        query = "SELECT * FROM users WHERE id = %s"
        result = self.db.execute(query, (user_id,))
        return result.fetchone() if result else None
```

**翻译后（中文）：**
```python
class UserService:
    """用于管理用户操作的服务类。"""
    
    def __init__(self, db_connection):
        """使用数据库连接初始化服务。"""
        self.db = db_connection
    
    def get_user(self, user_id):
        """从数据库中根据ID检索用户。
        
        参数:
            user_id: 用户的唯一标识符
            
        返回:
            如果找到则返回用户对象，否则返回None
        """
        # 查询数据库获取用户信息
        query = "SELECT * FROM users WHERE id = %s"
        result = self.db.execute(query, (user_id,))
        return result.fetchone() if result else None
```

## 注意事项

### 不要翻译的内容

1. **技术术语**
   - 编程语言名称：Python, Java, JavaScript等
   - 框架和库名称：React, Vue, Spring Boot等
   - 协议和标准：HTTP, REST, JSON, XML等
   - 开发工具：Git, Docker, Kubernetes等

2. **代码相关**
   - 代码块内容
   - 变量名、函数名、类名
   - 关键字和语法元素
   - 文件路径和文件名

3. **命令和配置**
   - 命令行指令
   - 配置文件内容
   - URL链接
   - 环境变量

### 需要翻译的内容

1. **文档结构**
   - 标题
   - 章节和段落
   - 列表项
   - 表格内容

2. **说明文字**
   - 描述性文本
   - 操作步骤
   - 注意事项和警告
   - 示例说明

3. **代码注释**（可选）
   - 根据需求决定是否翻译
   - 保持代码格式不变

### 特殊情况处理

1. **混合语言文档**
   - 如果原文中已经包含中文，保持原样
   - 确保翻译后的语言风格一致

2. **不确定的术语**
   - 保持英文并在括号中添加中文解释
   - 例如：使用 Webhook（网络钩子）机制

3. **专有名词**
   - 公司名称、产品名称保持英文
   - 例如：Google, Microsoft, Amazon

4. **缩写词**
   - 常见缩写保持英文：API, SDK, CLI等
   - 不常见缩写可翻译并保留英文

## 质量标准

翻译完成后，应满足以下质量标准：

### 语义准确性
- ✅ 翻译与原文含义完全一致
- ✅ 技术概念准确传达
- ✅ 无遗漏重要信息
- ✅ 无添加无关信息

### 语法正确性
- ✅ 句子结构正确
- ✅ 表达流畅自然
- ✅ 符合中文语法规范
- ✅ 无翻译腔

### 术语一致性
- ✅ 同一术语使用相同翻译
- ✅ 技术术语使用准确
- ✅ 符合行业标准
- ✅ 术语表一致（如有）

### 格式完整性
- ✅ Markdown格式完整
- ✅ 代码块完整保留
- ✅ 链接和引用正确
- ✅ 表格格式正确

### 专业性
- ✅ 语言专业规范
- ✅ 符合技术文档风格
- ✅ 术语使用恰当
- ✅ 表达清晰准确

## 常见技术术语翻译参考

| 英文 | 中文 | 说明 |
|------|------|------|
| Authentication | 认证 | 验证用户身份 |
| Authorization | 授权 | 验证用户权限 |
| Deployment | 部署 | 将应用发布到生产环境 |
| Configuration | 配置 | 设置系统参数 |
| Installation | 安装 | 安装软件或依赖 |
| Setup | 设置 | 初始化配置 |
| Repository | 仓库 | 代码存储库 |
| Dependency | 依赖 | 项目所需的包或库 |
| Endpoint | 端点 | API的访问地址 |
| Payload | 负载 | 请求或响应的数据部分 |
| Middleware | 中间件 | 处理请求和响应的软件层 |
| Framework | 框架 | 提供基础结构的软件平台 |
| Library | 库 | 可重用的代码集合 |
| Component | 组件 | 可复用的UI或功能单元 |
| Service | 服务 | 提供特定功能的模块 |
| Interface | 接口 | 定义交互规范 |
| Implementation | 实现 | 接口的具体代码 |
| Instance | 实例 | 类的对象 |
| Method | 方法 | 类中的函数 |
| Property | 属性 | 对象的数据成员 |
| Parameter | 参数 | 函数或方法的输入 |
| Argument | 实参 | 调用时传递的具体值 |
| Variable | 变量 | 存储数据的标识符 |
| Constant | 常量 | 不可变的值 |
| Function | 函数 | 可执行的代码块 |
| Callback | 回调 | 作为参数传递的函数 |
| Promise | Promise | 异步操作的对象 |
| Async/Await | 异步/等待 | 处理异步操作的语法 |
| Event | 事件 | 系统或用户触发的动作 |
| Listener | 监听器 | 响应事件的函数 |
| Handler | 处理器 | 处理特定事件的函数 |
| Router | 路由器 | 管理URL路由的组件 |
| Controller | 控制器 | 处理请求的组件 |
| Model | 模型 | 数据结构和业务逻辑 |
| View | 视图 | 用户界面展示 |
| Template | 模板 | 可重用的页面结构 |
| Layout | 布局 | 页面的整体结构 |
| Style | 样式 | 视觉表现规则 |
| Theme | 主题 | 统一的视觉风格 |
| Plugin | 插件 | 扩展功能的模块 |
| Extension | 扩展 | 增强功能的组件 |
| Module | 模块 | 独立的功能单元 |
| Package | 包 | 代码的打包单位 |
| Bundle | 打包 | 将多个文件合并 |
| Build | 构建 | 编译和打包过程 |
| Compile | 编译 | 将源代码转换为可执行代码 |
| Debug | 调试 | 查找和修复错误 |
| Test | 测试 | 验证代码功能 |
| Release | 发布 | 发布新版本 |
| Version | 版本 | 软件的迭代标识 |
| Patch | 补丁 | 修复问题的更新 |
| Update | 更新 | 升级到新版本 |
| Upgrade | 升级 | 提升到更高版本 |
| Migration | 迁移 | 数据或系统的转移 |
| Backup | 备份 | 数据的副本 |
| Restore | 恢复 | 从备份恢复数据 |
| Log | 日志 | 系统运行记录 |
| Error | 错误 | 程序异常 |
| Exception | 异常 | 运行时错误 |
| Warning | 警告 | 潜在问题提示 |
| Info | 信息 | 一般性消息 |
| Debug | 调试 | 调试信息 |
| Trace | 跟踪 | 详细执行信息 |
