# Skills 目录说明

## 📚 什么是 Skills？

Skills 是扩展 AI 助手能力的专业化指令集。每个 skill 定义了一个特定任务的执行流程、检查清单和最佳实践。

## 📁 目录结构

```
.agent/skills/
├── README.md
│
├── 后端开发
│   ├── feature-implementation/    # 系统化 Java 功能实现
│   ├── python-expert/             # Python 开发与优化
│   ├── overseas-http-test/        # 自动生成 HTTP 接口测试
│   ├── backend-test-writer/       # 后端测试编写器
│   ├── java-diagram/              # Java 画图专家
│   └── system-integration-validator/ # 系统集成验证
│
├── 前端开发
│   ├── frontend-design/           # 前端设计师
│   ├── ui-design-system/          # UI设计系统
│   ├── interaction-design/        # UI交互与动效设计
│   └── frontend-code-review/      # 前端代码审查
│
├── 代码质量与流程
│   ├── code-review/               # Sentry 风格代码审查
│   ├── bug-diagnosis/             # 系统化 Bug 定位修复
│   ├── refactor-assistant/        # 自动化重构助手
│   ├── feature-design-assistant/  # 交互式功能设计
│   ├── task-execution-engine/     # Markdown 任务执行引擎
│   ├── stack-analyzer/            # 技术栈分析与推荐
│   └── discover-skills/           # 技能发现与安装
│
├── 文档与文件处理
│   ├── doc-translator/            # 专业中英技术翻译
│   ├── doc-writer/                # 模板化文档编写
│   ├── design-document/           # 详细设计文档编写
│   ├── docs-analysis/             # 文档研究与解释
│   ├── markitdown/                # 全能格式转 Markdown
│   ├── pdf/                       # PDF 处理工具箱
│   └── pptx/                      # PPT 生成与编辑
│
└── AI 与自动化
    ├── agent-browser/             # 浏览器自动化代理
    ├── chatkit-botbuilder/        # ChatKit 机器人构建
    ├── image-gen/                 # OpenAI/Google 绘图
    ├── nano-image-generator/      # Gemini 3 专业绘图
    └── wechat-management/         # 微信消息管理
```

## 🎯 可用技能详情

### 后端开发

#### 1. 系统化 Java 功能实现 (`feature-implementation`)

系统化 Java 功能实现，涵盖需求分析、分层设计（Controller/Service/DAO）、核心代码编写（参数校验、异常相关、事务管理、并发控制）及完整性检查。确保代码健壮性与可维护性。

#### 2. Python 开发与优化 (`python-expert`)

Python 代码开发与优化专家。提供代码规范（PEP 8）、最佳实践（上下文管理器、列表推导）、常见模式（装饰器、数据类）、性能优化及常见陷阱规避指南，确保代码 Pythonic。

#### 3. 自动生成 HTTP 接口测试 (`overseas-http-test`)

自动生成 Intellij IDEA HTTP Client 测试脚本。解析 Java Controller，自动配置环境变量与鉴权 Header，生成包含断言的正向与异常测试用例，支持 Token 自动注入。

#### 4. 后端测试编写器 (`backend-test-writer`)

为 MERN 栈（Node.js/Express/MongoDB）生成后端测试。自动检测测试框架，生成包含 Setup/Teardown 的集成与单元测试，覆盖正常路径、错误处理及边缘场景。

#### 5. Java 画图专家 (`java-diagram`)

使用 Mermaid 绘制 Java 代码图表。支持类图（结构/继承/依赖）、时序图（方法调用流程）、流程图（业务逻辑）、状态图及 ER 图，帮助可视化代码结构与交互逻辑。

#### 6. 系统集成验证 (`system-integration-validator`)

部署前系统集成验证。检查端口占用情况、数据库（PostgreSQL/Redis）连接状态、API 契约匹配程度及数据流中的死锁或瓶颈，确保系统各组件交互正常。

### 前端开发

#### 7. 前端设计师 (`frontend-design`)

打造独特的高品质生产级前端界面。拒绝通用的 AI 模板美学，强调大胆的设计方向（极简、极繁、复古等）、排版选择、配色方案、动效设计及精细的代码实现。

#### 8. UI设计系统 (`ui-design-system`)

UI 设计系统工具包。提供从品牌色生成设计令牌（颜色/字体/间距）、组件系统架构设计、响应式计算及开发者交付文档生成，确保视觉一致性与协作效率。

#### 9. UI交互与动效设计 (`interaction-design`)

交互设计专家。专注于微交互、平滑过渡、加载状态、手势交互及反馈模式的设计与实现。利用 Framer Motion 或 CSS 动画提升用户体验与界面愉悦感。

#### 10. 前端代码审查 (`frontend-code-review`)

前端代码审查专家（.tsx/.ts/.js）。提供待变更审查与文件定向审查模式，严格基于代码质量、性能及业务逻辑检查清单发现问题，并提供具体的修复建议。

### 代码质量与流程

#### 11. Sentry 风格代码审查 (`code-review`)

基于 Sentry 工程实践的代码审查。重点关注运行时错误、性能问题（N+1 查询、复杂度）、副作用、向后兼容性、ORM 查询效率及安全漏洞，提供建设性反馈。

#### 12. 系统化 Bug 定位修复 (`bug-diagnosis`)

系统化 Bug 定位与修复。遵循"理解现象-定位问题-根因分析(5 Why)-制定修复-验证与预防"的流程，提供 NPE、并发、异常处理等常见 Bug 的修复模板。

#### 13. 自动化重构助手 (`refactor-assistant`)

自动化代码重构建议与实现。分析代码异味与复杂度，建议提取函数/类、重命名、消除重复、参数对象化等重构模式，并在保证行为不变的前提下安全执行增量变更。

#### 14. 交互式功能设计 (`feature-design-assistant`)

交互式功能设计助手。通过结构化问答收集需求（目标/范围/技术栈），探索方案，分章节完成架构、模型、API 及组件设计，最终生成详细的设计文档与实施任务。

#### 15. Markdown 任务执行引擎 (`task-execution-engine`)

基于 Markdown 复选框的任务执行引擎。从设计文档直接读取并执行实现任务，支持任务状态管理（完成/失败）、断点恢复及无人值守模式，自动化驱动开发流程。

#### 16. 技术栈分析与推荐 (`stack-analyzer`)

项目技术栈分析与技能推荐。自动检测语言、框架、数据库与工具，激活通用 AI 技能并脚手架生成项目特定的研究、领域或测试技能，优化 AI 辅助开发环境。

#### 17. 技能发现与安装 (`discover-skills`)

技能发现工具。当现有技能不满足需求时，基于用户描述的任务目标与约束，查询 SkillRadar 服务获取最佳匹配的技能推荐，并支持自动下载与安装。

### 文档与文件处理

#### 18. 专业中英技术翻译 (`doc-translator`)

专业中英技术文档翻译。确保语义准确与技术术语一致，完整保留代码块、Markdown 格式及链接，适用于 API 文档、用户手册及项目说明文件的翻译工作。

#### 19. 模板化文档编写 (`doc-writer`)

模板化文档编写工具。基于仓库上下文与 `DOC_TEMPLATE.md`，起草或更新计划中或已完成的任务文档，确保文档结构完整、任务编号唯一且符合验收标准。

#### 20. 详细设计文档编写 (`design-document`)

详细设计文档编写技能。涵盖背景介绍、需求描述、技术架构、业务流程（Mermaid 图表）、模型设计（ER 图）及接口设计，确保技术方案清晰、完整且可实施。

#### 21. 文档研究与解释 (`docs-analysis`)

文档研究与解释专家。查阅官方文档、查找权威代码示例、解释库 API 用法及研究最佳实践，为开发提供简明扼要的总结、示例与参考资料。

#### 22. 全能格式转 Markdown (`markitdown`)

全能文档转 Markdown 工具。支持 PDF、Office 文档 (Docs/PPT/Excel)、图像 (OCR)、音频 (转录) 等格式转换为 Markdown，支持 AI 图像描述，方便 LLM 处理。

#### 23. PDF 处理工具箱 (`pdf`)

Python PDF 处理工具箱。基于 pypdf/reportlab 等库，提供 PDF 文本与表格提取、文件合并/拆分、页面旋转、元数据提取、水印添加及表单处理等功能。

#### 24. PPT 生成与编辑 (`pptx`)

PowerPoint 演示文稿处理。支持从 HTML 转换生成排版精准的 PPT、解包 OOXML 编辑幻灯片内容、布局分析、颜色/排版提取及基于模板的自动化 PPT 生成。

### AI 与自动化

#### 25. 浏览器自动化代理 (`agent-browser`)

浏览器自动化代理。支持网页导航、元素交互（点击/输入/滚动）、数据提取、截图/PDF 生成及视频录制，适用于网页测试、爬虫及自动化表单填写。

#### 26. ChatKit 机器人构建 (`chatkit-botbuilder`)

构建生产级 ChatKit 机器人。集成 OpenAI Agents SDK、MCP 工具与 FastAPI 后端，实现用户隔离、实时数据同步及多平台部署，支持复杂对话任务。

#### 27. OpenAI/Google 绘图 (`image-gen`)

OpenAI/Google 图像生成工具。支持文生图、利用参考图进行风格迁移、自定义宽高比及多图并行生成，适用于快速创建各种视觉素材。

#### 28. Gemini 3 专业绘图 (`nano-image-generator`)

基于 Gemini 3 Pro 的专业图像生成。专精于图标、UI 图形、营销横幅等生成，支持强力参考图像功能以实现风格迁移与角色一致性保持。

#### 29. 微信消息管理 (`wechat-management`)

微信消息管理与发送（需 WeChatMCP）。管理微信消息发送，遵循短消息原则、Emoji 使用规范及语气适配指南，确保消息内容安全、准确且符合社交礼仪。
