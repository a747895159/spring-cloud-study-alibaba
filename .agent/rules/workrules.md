---
trigger: always_on
---

+ 1.java类中新增字段逻辑 出入参全部小写;
+ 2.文件名参考当前项目目录层级规则定义;
+ 3.相同含义的字段名复用现有的命令;
+ 4.禁止修改 import、缩进、空行、注释、任何格式化、重排、重构行为; 如果修改了任何无关行，任务视为失败，请停止输出;
+ 5.非代码一定要用中文输出内容;
+ 6.📁项目结构如下:
	- 6.1 buffaloex-overseas-commom项目是公共jar包，负责所有核心业务代码,提供业务接口,核心包结构如下;
		- service层面：com.buffaloex.overseas.common.service.common.app
		- service出入参类层:com.buffaloex.overseas.common.dto.vo
		- 业务辅助Helper类：com.buffaloex.overseas.common.service.common.app.Helper 负责service层通用业务或者业务代码抽取
		- uncode数据库接口层:com.buffaloex.overseas.common.service.common.local
		- uncode数据库接口实现:com.buffaloex.overseas.common.service.common.local.impl
		- 自定义SQL对应Dao层:com.buffaloex.overseas.common.dao
		- 自定义SQLMapper目录:resources/com/buffaloex/mapper 自定义的SQL字段名要和实体类字段名一致;
		- 数据库实体层:com.buffaloex.overseas.common.dto
		- 数据库查询BO层:com.buffaloex.overseas.common.dto.bo.query
		- Excel导出类层:com.buffaloex.overseas.common.dto.export
	- 6.2 buffaloex-overseas-api项目是PDA软件对应的后台,Controller层入口;
	- 6.3 buffaloex-overseas项目是管理端对应的后台,Controller层入口;
	- 6.4 buffaloex-overseas-index项目是客户端对应的后台,Controller层入口;
	- 6.5 buffaloex-overseas-task项目是定时任务服务,XXL-Job入口;
+ 7.java代码风格:
- 7.1单行代码允许最长 200 字符
- 7.2优先保持链式调用单行可读性
- 7.3避免为了换行而拆分逻辑