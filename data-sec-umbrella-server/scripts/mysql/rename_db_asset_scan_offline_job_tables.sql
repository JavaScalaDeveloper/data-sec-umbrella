-- 将离线扫描任务表重命名为引擎无关命名（若库中仍为旧表名则执行一次）
-- 应用已使用 database_type 区分 MySQL / Clickhouse

RENAME TABLE db_asset_mysql_scan_offline_job TO db_asset_scan_offline_job;
RENAME TABLE db_asset_mysql_scan_offline_job_instance TO db_asset_scan_offline_job_instance;
