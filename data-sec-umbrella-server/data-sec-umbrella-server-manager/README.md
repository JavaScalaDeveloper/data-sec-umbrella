# data-sec-umbrella-server-manager

**职责**：管理端 Web 服务，与前端交互的 HTTP API。

**包含**：

- `controller`：全部 REST 控制器
- `config.WebConfig` 等 Web 相关配置
- `DataSecUmbrellaManagerApplication`：启动类（`scanBasePackages` + `MapperScan` 覆盖 core 与当前模块）

**依赖**：`data-sec-umbrella-server-core`。

**配置**：`src/main/resources/application.yml`（默认端口 **8080**）。离线扫描分发依赖 **Redis**（默认 `127.0.0.1:6379`）与 **RocketMQ NameServer**（默认 `127.0.0.1:9876`），可通过环境变量 `REDIS_HOST` / `REDIS_PORT` / `ROCKETMQ_NAMESRV_ADDR` 覆盖。

**启动示例**：

```bash
cd ..
mvn -pl data-sec-umbrella-server-manager spring-boot:run
```
