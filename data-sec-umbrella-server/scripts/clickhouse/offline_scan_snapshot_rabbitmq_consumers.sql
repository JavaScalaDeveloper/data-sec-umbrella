-- =============================================================================
-- 由 ClickHouse 直接消费 RabbitMQ（与 OfflineScanSnapshotPublisher 发出的 JSON 对齐）
-- ClickHouse 24.8+
--
-- 【执行顺序】3/3：必须在 offline_scan_snapshot_table.sql、offline_scan_snapshot_column.sql 之后执行
-- 每次执行会先 DROP 物化视图、再 DROP RabbitMQ 引擎表、再 CREATE（DROP VIEW 与 1/2 步重复时 IF EXISTS 幂等）。
-- =============================================================================
-- 消息体为 Fastjson 序列化的 Java 字段名（camelCase），故 RabbitMQ 引擎表列名使用反引号保持大小写。
-- 交换机、路由键须与 Java 中 OfflineScanConstants 一致。
--
-- 凭据：可在本 DDL 的 SETTINGS 中填写，或仅在 clickhouse-server 的 config.xml 中配置 <rabbitmq> 块
--       （推荐生产用 config，避免 SQL 落库明文密码）。
--
-- 与 Spring 声明的 durable 队列关系：rabbitmq_queue_consume 默认为 0 时，ClickHouse 会自行声明
-- 绑定到同一 exchange + routing key 的消费队列，与 Java 侧队列并行消费，同一条消息会投递到各队列各一份。
-- 若希望仅由 CK 消费，可移除 manager 里对快照队列的 Bean 声明，并改用 rabbitmq_queue_consume=1
-- 绑定已有队列名（见官方文档 rabbitmq_queue_consume）。
--
-- -----------------------------------------------------------------------------
-- 【排错：Code 530 CANNOT_CONNECT_RABBITMQ】
--
-- 1) 连通性是谁发起的？
--    CREATE TABLE … ENGINE=RabbitMQ 在「ClickHouse 服务端进程」里建连，不是在你笔记本上。
--    本机能 ping/telnet book-n95，只说明「你的电脑 → RabbitMQ」通，不代表「ClickHouse 所在容器/主机 → RabbitMQ」通。
--
-- 2) telnet 立刻被断开？
--    RabbitMQ 只接受 AMQP 握手；纯 telnet 连上后常被服务端主动断开，属正常现象，不能据此判断 AMQP 失败。
--
-- 3) ClickHouse 跑在 Docker 里时最常见原因：
--    - 容器内解析不到 book-n95（/etc/hosts 只在宿主机有、未挂进容器）。
--    - 容器所在 bridge 访问宿主机映射端口要用宿主机局域网 IP（如 telnet 里出现的 192.168.x.x），
--      或 docker-compose 同一 network 下直接用服务名 rabbitmq，而不是宿主机 hostname。
--    - 防火墙只允许某网段访问 5672，未放行「ClickHouse 容器出口 IP」。
--
-- 4) 建议在「运行 ClickHouse 的同一环境」里验证，例如：
--      docker exec -it <clickhouse容器名> bash -c 'getent hosts book-n95 || true; nc -zv 192.168.1.163 5672'
--    若此处失败，请把下面 SETTINGS 中的 host 改成 RabbitMQ 可达的 IP（与 rabbitmq_address 二选一）。
--
-- 5) 可选：整 URL（与 rabbitmq_host_port 二选一，不要同时写）
--      rabbitmq_address = 'amqp://admin:123456@192.168.1.163:5672/',
--      rabbitmq_vhost = '/',
-- -----------------------------------------------------------------------------
-- 凭据与地址请按环境修改（下面 host 请改为 ClickHouse 进程实际能解析且能 TCP 连通的值）

DROP VIEW IF EXISTS data_sec_umbrella.mv_offline_scan_snapshot_table_from_rmq;
DROP VIEW IF EXISTS data_sec_umbrella.mv_offline_scan_snapshot_column_from_rmq;
DROP TABLE IF EXISTS data_sec_umbrella.offline_scan_snapshot_table_rmq;
DROP TABLE IF EXISTS data_sec_umbrella.offline_scan_snapshot_column_rmq;

CREATE TABLE data_sec_umbrella.offline_scan_snapshot_table_rmq
(
    `instanceId` UInt64,
    `jobId` UInt64,
    `dispatchVersion` UInt64,
    `taskName` String,
    `scanKind` String,
    `engine` String,
    `uniqueKey` String,
    `sensitivityLevel` String,
    `sensitivityTags` Array(String),
    `columnDetails` Nullable(String),
    `eventTime` Int64
)
ENGINE = RabbitMQ
SETTINGS
    rabbitmq_host_port = 'rabbitmq:5672',
    rabbitmq_vhost = '/',
    rabbitmq_username = 'admin',
    rabbitmq_password = '123456',
    rabbitmq_exchange_name = 'offline.mysql.scan.exchange',
    rabbitmq_exchange_type = 'direct',
    rabbitmq_routing_key_list = 'OFFLINE_SCAN_SNAPSHOT_TABLE',
    rabbitmq_format = 'JSONEachRow',
    rabbitmq_num_consumers = 1,
    rabbitmq_flush_interval_ms = 1000,
    rabbitmq_skip_broken_messages = 1024;

CREATE MATERIALIZED VIEW data_sec_umbrella.mv_offline_scan_snapshot_table_from_rmq
TO data_sec_umbrella.offline_scan_snapshot_table
AS
SELECT
    if(toInt64(`eventTime`) > 0, fromUnixTimestamp64Milli(toInt64(`eventTime`)), now()) AS event_time,
    toUInt64(`instanceId`) AS instance_id,
    toUInt64(`jobId`) AS job_id,
    `taskName` AS task_name,
    toUInt64(`dispatchVersion`) AS dispatch_version,
    `scanKind` AS scan_kind,
    `engine` AS engine,
    `uniqueKey` AS unique_key,
    `sensitivityLevel` AS sensitivity_level,
    `sensitivityTags` AS sensitivity_tags,
    ifNull(`columnDetails`, '[]') AS column_details
FROM data_sec_umbrella.offline_scan_snapshot_table_rmq;

CREATE TABLE data_sec_umbrella.offline_scan_snapshot_column_rmq
(
    `instanceId` UInt64,
    `jobId` UInt64,
    `dispatchVersion` UInt64,
    `taskName` String,
    `scanKind` String,
    `engine` String,
    `uniqueKey` String,
    `sensitivityLevel` String,
    `sensitivityTags` Array(String),
    `samples` Array(String),
    `sensitiveSamples` Array(String),
    `eventTime` Int64
)
ENGINE = RabbitMQ
SETTINGS
    rabbitmq_host_port = 'rabbitmq:5672',
    rabbitmq_vhost = '/',
    rabbitmq_username = 'admin',
    rabbitmq_password = '123456',
    rabbitmq_exchange_name = 'offline.mysql.scan.exchange',
    rabbitmq_exchange_type = 'direct',
    rabbitmq_routing_key_list = 'OFFLINE_SCAN_SNAPSHOT_COLUMN',
    rabbitmq_format = 'JSONEachRow',
    rabbitmq_num_consumers = 1,
    rabbitmq_flush_interval_ms = 1000,
    rabbitmq_skip_broken_messages = 1024;

CREATE MATERIALIZED VIEW data_sec_umbrella.mv_offline_scan_snapshot_column_from_rmq
TO data_sec_umbrella.offline_scan_snapshot_column
AS
SELECT
    if(toInt64(`eventTime`) > 0, fromUnixTimestamp64Milli(toInt64(`eventTime`)), now()) AS event_time,
    toUInt64(`instanceId`) AS instance_id,
    toUInt64(`jobId`) AS job_id,
    `taskName` AS task_name,
    toUInt64(`dispatchVersion`) AS dispatch_version,
    `scanKind` AS scan_kind,
    `engine` AS engine,
    `uniqueKey` AS unique_key,
    `sensitivityLevel` AS sensitivity_level,
    `sensitivityTags` AS sensitivity_tags,
    ifNull(`samples`, []) AS samples,
    ifNull(`sensitiveSamples`, []) AS sensitive_samples
FROM data_sec_umbrella.offline_scan_snapshot_column_rmq;
