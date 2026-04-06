# 数据安全保护伞 · 后端（多模块）

聚合工程：`data-sec-umbrella-server-core`（领域与持久化）、`data-sec-umbrella-server-manager`（管理 API）、`data-sec-umbrella-server-worker`（运行时任务，可多节点）。

## 构建

```bash
cd data-sec-umbrella-server
mvn clean install
```

## 启动

- **管理端**（默认端口 8080，对接前端）：

  ```bash
  mvn -pl data-sec-umbrella-server-manager spring-boot:run
  ```

  主类：`com.arelore.data.sec.umbrella.server.DataSecUmbrellaManagerApplication`

- **运行端**（默认端口 8081，定时扫描等，勿与 manager 同端口部署到同一机器时冲突）：

  ```bash
  mvn -pl data-sec-umbrella-server-worker spring-boot:run
  ```

  主类：`com.arelore.data.sec.umbrella.server.DataSecUmbrellaWorkerApplication`

配置分别位于各模块的 `src/main/resources/application.yml`。

## 模块说明

详见各子目录下的 `README.md`。
