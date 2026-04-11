-- =============================================================================
-- 离线扫描：字段级敏感快照（MergeTree）
-- ClickHouse 24.8+
--
-- 【执行顺序】2/3：在 offline_scan_snapshot_table.sql 之后执行；最后执行 rabbitmq 脚本
-- 每次执行会先 DROP 再 CREATE（可清空数据重建结构，勿用 ALTER）。
-- =============================================================================
-- 分区：自然日 yyyymmdd；TTL：180 天
-- 查询约定：WHERE 必须同时带 engine、scan_kind
-- =============================================================================

DROP VIEW IF EXISTS data_sec_umbrella.mv_offline_scan_snapshot_column_from_rmq;
DROP TABLE IF EXISTS data_sec_umbrella.offline_scan_snapshot_column;

CREATE TABLE data_sec_umbrella.offline_scan_snapshot_column
(
    event_time          DateTime,
    instance_id         UInt64,
    job_id              UInt64,
    task_name           String,
    dispatch_version    UInt64,
    scan_kind           LowCardinality(String) COMMENT 'RULE / AI',
    engine              LowCardinality(String) COMMENT 'MySQL / ClickHouse 等',
    unique_key          String COMMENT '业务唯一键：实例,库名,表名,列名（逗号分隔）',
    sensitivity_level   String,
    sensitivity_tags    Array(String),
    samples             Array(String) DEFAULT [] COMMENT '列样例列表',
    sensitive_samples   Array(String) DEFAULT [] COMMENT '列敏感样例列表',
    INDEX idx_engine_scan_kind (engine, scan_kind) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree
PARTITION BY toYYYYMMDD(event_time)
ORDER BY (engine, scan_kind, instance_id, event_time, unique_key)
TTL event_time + INTERVAL 180 DAY
SETTINGS index_granularity = 8192;
