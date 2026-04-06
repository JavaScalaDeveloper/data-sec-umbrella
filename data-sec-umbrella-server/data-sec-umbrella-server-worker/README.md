# data-sec-umbrella-server-worker

**职责**：运行端进程，可多实例水平扩展；**不**提供面向前端的 HTTP 业务 API。

**当前能力**：

- 与 core 相同的数据源与 MyBatis 配置，可操作数据库
- `@EnableScheduling`：执行 core 中 `MySQLAssetScanJob` 等定时任务

**后续可扩展**：

- 在 `pom.xml` 中增加 `spring-boot-starter-amqp`（或 Kafka 等），在 `application.yml` 中配置连接，编写 `@RabbitListener` 等消费者

**依赖**：`data-sec-umbrella-server-core`。

**配置**：`src/main/resources/application.yml`（默认端口 **8081**，避免与 manager 冲突）。

**启动示例**：

```bash
cd ..
mvn -pl data-sec-umbrella-server-worker spring-boot:run
```

主类：`DataSecUmbrellaWorkerApplication`。
