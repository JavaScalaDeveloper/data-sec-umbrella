-- =============================================================================
-- 离线扫描：表级敏感快照（MergeTree）
-- ClickHouse 24.8+
--
-- 【执行顺序】1/3：本文件 → offline_scan_snapshot_column.sql → offline_scan_snapshot_rabbitmq_consumers.sql
-- 每次执行会先 DROP 再 CREATE（可清空数据重建结构，勿用 ALTER）。
-- =============================================================================
-- 分区：自然日 yyyymmdd；TTL：180 天
-- 查询约定：WHERE 必须同时带 engine、scan_kind（与主键 ORDER BY 前缀一致，避免全表扫）
-- =============================================================================

DROP VIEW IF EXISTS data_sec_umbrella.mv_offline_scan_snapshot_table_from_rmq;
DROP TABLE IF EXISTS data_sec_umbrella.offline_scan_snapshot_table;

CREATE TABLE data_sec_umbrella.offline_scan_snapshot_table
(
    event_time          DateTime COMMENT '事件时间，分区键',
    instance_id         UInt64 COMMENT '离线扫描任务实例 ID',
    job_id              UInt64,
    task_name           String,
    dispatch_version    UInt64,
    scan_kind           LowCardinality(String) COMMENT 'RULE / AI',
    engine              LowCardinality(String) COMMENT 'MySQL / ClickHouse 等',
    unique_key          String COMMENT '业务唯一键：实例,库名,表名（逗号分隔）',
    sensitivity_level   String,
    sensitivity_tags    Array(String),
    -- JSON 数组：每列含 column_name、samples、sensitive_samples、sensitivity_level、sensitivity_tags（与 MQ 中 columnDetails 一致）
    column_details      String DEFAULT '[]' CODEC(ZSTD(1)),
    INDEX idx_engine_scan_kind (engine, scan_kind) TYPE minmax GRANULARITY 4
)
ENGINE = MergeTree
PARTITION BY toYYYYMMDD(event_time)
ORDER BY (engine, scan_kind, instance_id, event_time, unique_key)
TTL event_time + INTERVAL 180 DAY
SETTINGS index_granularity = 8192;
