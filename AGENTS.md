# Repository Guidelines

## 项目目标与需求边界

本仓库服务于“招聘与人才管理平台”需求文档，优先交付可演示、可验收的招聘主流程：职位发布、候选人/简历管理、投递筛选、面试安排、Offer、入职办理与员工档案。系统角色包括系统管理员、HR、面试官和候选人。AI 功能仅作为辅助参考，不直接决定录用结果。

## Project Structure & Module Organization

后端位于 `recruit-smart-backend/`，采用 Java 21、Spring Boot、Spring Cloud 与 Maven 多模块结构。前端位于 `frontend/`，采用 Vue 3、TypeScript、Vite 与 npm。

- `recruit-gateway/`：统一入口与路由转发。
- `recruit-biz-service/`：传统业务域，负责招聘主流程和最终业务状态落库。
- `recruit-ai-service/`：AI 智能域，负责简历匹配、面试题生成、反馈摘要和离职倾向预测。
- `recruit-common/`：统一返回、分页、异常和通用枚举。
- `feign-api/`：服务间调用契约。
- `sql/recruit_smart_init.sql`：数据库初始化脚本。
- `frontend/`：招聘平台前端 SPA，包含登录、注册、工作台、候选人、流程、面试和 AI 审批页面。

生产代码放在各模块 `src/main/java`，配置放在 `src/main/resources/application.yml`，测试放在 `src/test/java`。

## Build, Test, and Development Commands

在 `recruit-smart-backend/` 下执行：

- `mvn clean install`：构建全部模块。
- `mvn test`：运行全部测试。
- `mvn -pl recruit-biz-service -am test`：测试业务服务及依赖模块。
- `mvn -pl recruit-biz-service -am spring-boot:run`：启动业务服务。
- `mvn -pl recruit-gateway -am spring-boot:run`：启动网关服务。

本地启动前确认 MySQL、Nacos 和各模块 `application.yml` 配置可用。

在 `frontend/` 下执行：

- `npm install`：安装前端依赖。
- `npm run dev`：启动 Vite 开发服务器。
- `npm run type-check`：运行 TypeScript 类型检查。
- `npm run lint`：运行并自动修复前端 lint。
- `npm test -- --run`：一次性运行前端单元测试。
- `npm run build`：执行前端生产构建。

## Coding Style & Domain Rules

使用 4 空格缩进、UTF-8、`com.recruit.<module>` 包路径。按 `entity`、`mapper`、`controller`、`service` 分层组织代码。候选人业务资料必须使用 `Candidate`，不要直接复用登录用户表 `SysUser`。投递、面试、Offer、入职和员工档案应保持清晰状态流转；AI 结果可保存为评分、摘要或建议，但最终筛选、录用和入职状态由业务服务控制。

## Testing Guidelines

新增功能必须覆盖核心业务规则：角色权限、职位发布、简历投递、状态流转、面试反馈、Offer 和入职转员工档案。测试类命名为 `*Test`，集成测试可命名为 `*IT`。提交前至少运行受影响模块的 Maven 测试命令。

## Commit & Pull Request Guidelines

沿用简短直接的提交风格，例如 `update backend structure and data access layer`。PR 需说明关联需求模块、影响服务、数据库变更、验证命令和接口示例。涉及表结构时同步更新 `sql/recruit_smart_init.sql`。

## Security & Configuration Tips

不要提交真实数据库密码、Nacos 凭据、JWT 密钥或 AI 服务密钥。涉及候选人简历、面试评价和员工档案时，避免在日志中输出敏感个人信息。
