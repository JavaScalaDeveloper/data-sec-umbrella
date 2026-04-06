# data-sec-umbrella-server-core

**职责**：可复用的领域与基础设施代码，供 `manager` 与 `worker` 依赖。

**包含**：

- `entity`：数据库实体
- `mapper`：MyBatis-Plus Mapper
- `service` / `service/impl`：业务服务接口与实现
- `dto`：请求/响应模型
- `common`：通用响应封装等
- `util`、`constant`、`enums`
- `strategy`、`service/checker`、`service/matcher` 等规则与连接策略
- `schedule.MySQLAssetScanJob`：资产扫描任务 Bean（`@Scheduled` 仅在启用 `@EnableScheduling` 的进程中生效；管理端可手动调用其方法）

**不包含**：任何 `Controller`、Spring MVC 与面向前端的 Web 配置。

**打包**：普通 `jar`，不可单独 `spring-boot:run`。
