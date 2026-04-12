package com.arelore.data.sec.umbrella.server.core.schedule;

import com.arelore.data.sec.umbrella.server.core.constant.RSAKeyConstants;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.ClickhouseTableInfo;
import com.arelore.data.sec.umbrella.server.core.entity.mysql.DataSource;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseDatabaseInfoService;
import com.arelore.data.sec.umbrella.server.core.service.ClickhouseTableInfoService;
import com.arelore.data.sec.umbrella.server.core.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategyFactory;
import com.arelore.data.sec.umbrella.server.core.util.RSACryptoUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * ClickHouse 资产扫描（写入 db_asset_clickhouse_*）
 */
@Slf4j
@Component
public class ClickHouseAssetScanJob {

    private static final String CLICKHOUSE_TYPE = "Clickhouse";

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private ClickhouseDatabaseInfoService clickhouseDatabaseInfoService;

    @Autowired
    private ClickhouseTableInfoService clickhouseTableInfoService;

    @Scheduled(cron = "0 30 2 * * ?")
    public void scanClickHouseAssetsScheduled() {
        log.info("开始执行 ClickHouse 资产扫描定时任务");
        try {
            scanDatabases();
            scanTables();
            log.info("ClickHouse 资产扫描定时任务执行完成");
        } catch (Exception e) {
            log.error("ClickHouse 资产扫描定时任务执行失败", e);
        }
    }

    public void scanDatabases() throws Exception {
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataSource::getDataSourceType, CLICKHOUSE_TYPE);
        List<DataSource> dataSources = dataSourceService.list(queryWrapper);

        DatabaseScanStrategy scanStrategy = DatabaseScanStrategyFactory.getStrategy(CLICKHOUSE_TYPE);

        for (DataSource dataSource : dataSources) {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                String password = RSACryptoUtil.decrypt(dataSource.getPassword(), RSAKeyConstants.PRIVATE_KEY);
                String url = scanStrategy.buildDatabaseUrl(dataSource.getInstance());
                connection = DriverManager.getConnection(url, dataSource.getUsername(), password);
                statement = connection.createStatement();
                resultSet = statement.executeQuery(scanStrategy.getDatabaseQuery());

                while (resultSet.next()) {
                    String databaseName = resultSet.getString("SCHEMA_NAME");

                    ClickhouseDatabaseInfo databaseInfo = new ClickhouseDatabaseInfo();
                    databaseInfo.setInstance(dataSource.getInstance());
                    databaseInfo.setDatabaseName(databaseName);
                    databaseInfo.setDescription("");

                    LambdaQueryWrapper<ClickhouseDatabaseInfo> dbQueryWrapper = new LambdaQueryWrapper<>();
                    dbQueryWrapper.eq(ClickhouseDatabaseInfo::getInstance, dataSource.getInstance())
                            .eq(ClickhouseDatabaseInfo::getDatabaseName, databaseName);
                    ClickhouseDatabaseInfo existingDb = clickhouseDatabaseInfoService.getOne(dbQueryWrapper);

                    if (existingDb != null) {
                        databaseInfo.setId(existingDb.getId());
                        databaseInfo.setCreateTime(existingDb.getCreateTime());
                        databaseInfo.setManualReview(existingDb.getManualReview());
                        clickhouseDatabaseInfoService.updateById(databaseInfo);
                    } else {
                        databaseInfo.setCreateTime(new Date());
                        clickhouseDatabaseInfoService.save(databaseInfo);
                    }
                }
            } catch (Exception e) {
                log.error("扫描 ClickHouse 数据源 {} 的数据库失败", dataSource.getInstance(), e);
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }

    public void scanTables() throws Exception {
        List<ClickhouseDatabaseInfo> databaseInfos = clickhouseDatabaseInfoService.list();
        DatabaseScanStrategy scanStrategy = DatabaseScanStrategyFactory.getStrategy(CLICKHOUSE_TYPE);

        for (ClickhouseDatabaseInfo databaseInfo : databaseInfos) {
            LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataSource::getInstance, databaseInfo.getInstance());
            queryWrapper.eq(DataSource::getDataSourceType, CLICKHOUSE_TYPE);
            DataSource dataSource = dataSourceService.getOne(queryWrapper);

            if (dataSource == null) {
                log.warn("找不到 ClickHouse 数据源实例: {}", databaseInfo.getInstance());
                continue;
            }

            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                String password = RSACryptoUtil.decrypt(dataSource.getPassword(), RSAKeyConstants.PRIVATE_KEY);
                String url = scanStrategy.buildTableUrl(dataSource.getInstance(), databaseInfo.getDatabaseName());
                connection = DriverManager.getConnection(url, dataSource.getUsername(), password);
                statement = connection.createStatement();

                String tableQuery = scanStrategy.getTableQuery(databaseInfo.getDatabaseName());
                resultSet = statement.executeQuery(tableQuery);

                while (resultSet.next()) {
                    String[] tableInfoArr = scanStrategy.getTableInfo(resultSet);
                    String tableName = tableInfoArr[0];
                    String tableComment = tableInfoArr[1];

                    Statement columnStatement = connection.createStatement();
                    String columnQuery = scanStrategy.getColumnQuery(databaseInfo.getDatabaseName(), tableName);
                    ResultSet columnResultSet = columnStatement.executeQuery(columnQuery);
                    String columnInfoJson = scanStrategy.getColumnInfo(columnResultSet);
                    columnResultSet.close();
                    columnStatement.close();

                    ClickhouseTableInfo row = new ClickhouseTableInfo();
                    row.setInstance(databaseInfo.getInstance());
                    row.setDatabaseName(databaseInfo.getDatabaseName());
                    row.setTableName(tableName);
                    row.setDescription(tableComment != null ? tableComment : "");
                    row.setColumnInfo(columnInfoJson);

                    LambdaQueryWrapper<ClickhouseTableInfo> tableQueryWrapper = new LambdaQueryWrapper<>();
                    tableQueryWrapper.eq(ClickhouseTableInfo::getInstance, databaseInfo.getInstance())
                            .eq(ClickhouseTableInfo::getDatabaseName, databaseInfo.getDatabaseName())
                            .eq(ClickhouseTableInfo::getTableName, tableName);
                    ClickhouseTableInfo existingTable = clickhouseTableInfoService.getOne(tableQueryWrapper);

                    if (existingTable != null) {
                        row.setId(existingTable.getId());
                        row.setCreateTime(existingTable.getCreateTime());
                        row.setManualReview(existingTable.getManualReview());
                        row.setColumnScanInfo(existingTable.getColumnScanInfo());
                        row.setColumnAiScanInfo(existingTable.getColumnAiScanInfo());
                        row.setSensitivityLevel(existingTable.getSensitivityLevel());
                        row.setSensitivityTags(existingTable.getSensitivityTags());
                        row.setAiSensitivityLevel(existingTable.getAiSensitivityLevel());
                        row.setAiSensitivityTags(existingTable.getAiSensitivityTags());
                        clickhouseTableInfoService.updateById(row);
                    } else {
                        row.setCreateTime(new Date());
                        clickhouseTableInfoService.save(row);
                    }
                }
            } catch (Exception e) {
                log.error("扫描 ClickHouse 库 {} 的表失败", databaseInfo.getDatabaseName(), e);
            } finally {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            }
        }
    }
}
