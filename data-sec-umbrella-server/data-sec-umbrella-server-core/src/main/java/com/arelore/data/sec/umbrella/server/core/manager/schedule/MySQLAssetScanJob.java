package com.arelore.data.sec.umbrella.server.core.schedule;

import com.arelore.data.sec.umbrella.server.core.entity.DataSource;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLDatabaseInfo;
import com.arelore.data.sec.umbrella.server.core.entity.MySQLTableInfo;
import com.arelore.data.sec.umbrella.server.core.service.DataSourceService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLDatabaseInfoService;
import com.arelore.data.sec.umbrella.server.core.service.MySQLTableInfoService;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategy;
import com.arelore.data.sec.umbrella.server.core.strategy.DatabaseScanStrategyFactory;
import com.arelore.data.sec.umbrella.server.core.constant.RSAKeyConstants;
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
 * MySQL资产扫描定时任务
 */
@Slf4j
@Component
public class MySQLAssetScanJob {

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private MySQLDatabaseInfoService mySQLDatabaseInfoService;

    @Autowired
    private MySQLTableInfoService mySQLTableInfoService;

    /**
     * 定时扫描MySQL数据源，获取数据库和表信息
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void scanMySQLAssets() {
        log.info("开始执行MySQL资产扫描任务");
        try {
            // 扫描数据库
            scanDatabases();
            // 扫描表
            scanTables();
            log.info("MySQL资产扫描任务执行完成");
        } catch (Exception e) {
            log.error("MySQL资产扫描任务执行失败", e);
        }
    }

    /**
     * 扫描数据库
     */
    public void scanDatabases() throws Exception {
        // 获取所有MySQL数据源
        LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DataSource::getDataSourceType, "MySQL");
        List<DataSource> dataSources = dataSourceService.list(queryWrapper);

        for (DataSource dataSource : dataSources) {
            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                // 解密密码
                String password = RSACryptoUtil.decrypt(dataSource.getPassword(), RSAKeyConstants.PRIVATE_KEY);
                // 获取数据库类型
                String databaseType = dataSource.getDataSourceType();
                
                // 获取数据库扫描策略
                DatabaseScanStrategy scanStrategy = DatabaseScanStrategyFactory.getStrategy(databaseType);
                
                // 使用策略模式构建JDBC连接URL
                String url = scanStrategy.buildDatabaseUrl(dataSource.getInstance());
                
                // 创建数据库连接
                connection = DriverManager.getConnection(url, dataSource.getUsername(), password);
                statement = connection.createStatement();
                
                // 查询所有数据库，排除系统数据库
                String databaseQuery = scanStrategy.getDatabaseQuery();
                resultSet = statement.executeQuery(databaseQuery);
                
                while (resultSet.next()) {
                    String databaseName = resultSet.getString("SCHEMA_NAME");
                    
                    // 查询数据库信息
                    MySQLDatabaseInfo databaseInfo = new MySQLDatabaseInfo();
                    databaseInfo.setInstance(dataSource.getInstance());
                    databaseInfo.setDatabaseName(databaseName);
                    databaseInfo.setDescription("");
                    
                    // 保存或更新数据库信息
                    LambdaQueryWrapper<MySQLDatabaseInfo> dbQueryWrapper = new LambdaQueryWrapper<>();
                    dbQueryWrapper.eq(MySQLDatabaseInfo::getInstance, dataSource.getInstance())
                            .eq(MySQLDatabaseInfo::getDatabaseName, databaseName);
                    MySQLDatabaseInfo existingDb = mySQLDatabaseInfoService.getOne(dbQueryWrapper);
                    
                    if (existingDb != null) {
                        databaseInfo.setId(existingDb.getId());
                        databaseInfo.setCreateTime(existingDb.getCreateTime());
                        databaseInfo.setManualReview(existingDb.getManualReview());
                        mySQLDatabaseInfoService.updateById(databaseInfo);
                    } else {
                        databaseInfo.setCreateTime(new Date());
                        mySQLDatabaseInfoService.save(databaseInfo);
                    }
                }
            } catch (Exception e) {
                log.error("扫描数据源 {} 的数据库失败", dataSource.getInstance(), e);
            } finally {
                // 关闭资源
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            }
        }
    }

    /**
     * 扫描表
     */
    public void scanTables() throws Exception {
        // 获取所有MySQL数据库信息
        List<MySQLDatabaseInfo> databaseInfos = mySQLDatabaseInfoService.list();

        for (MySQLDatabaseInfo databaseInfo : databaseInfos) {
            // 获取对应的数据源
            LambdaQueryWrapper<DataSource> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DataSource::getInstance, databaseInfo.getInstance());
            DataSource dataSource = dataSourceService.getOne(queryWrapper);
            
            if (dataSource == null) {
                log.warn("找不到数据源实例: {}", databaseInfo.getInstance());
                continue;
            }

            Connection connection = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                // 解密密码
                String password = RSACryptoUtil.decrypt(dataSource.getPassword(), RSAKeyConstants.PRIVATE_KEY);
                
                // 获取数据库类型
                String databaseType = dataSource.getDataSourceType();
                
                // 获取数据库扫描策略
                DatabaseScanStrategy scanStrategy = DatabaseScanStrategyFactory.getStrategy(databaseType);
                
                // 使用策略模式构建JDBC连接URL
                String url = scanStrategy.buildTableUrl(dataSource.getInstance(), databaseInfo.getDatabaseName());
                
                // 创建数据库连接
                connection = DriverManager.getConnection(url, dataSource.getUsername(), password);
                statement = connection.createStatement();
                
                // 查询所有表，排除系统表
                String tableQuery = scanStrategy.getTableQuery(databaseInfo.getDatabaseName());
                resultSet = statement.executeQuery(tableQuery);
                
                while (resultSet.next()) {
                    // 获取表信息
                    String[] tableInfo = scanStrategy.getTableInfo(resultSet);
                    String tableName = tableInfo[0];
                    String tableComment = tableInfo[1];
                    
                    // 查询表的列信息
                    Statement columnStatement = connection.createStatement();
                    String columnQuery = scanStrategy.getColumnQuery(databaseInfo.getDatabaseName(), tableName);
                    ResultSet columnResultSet = columnStatement.executeQuery(columnQuery);
                    
                    // 获取列信息
                    String columnInfoJson = scanStrategy.getColumnInfo(columnResultSet);
                    
                    columnResultSet.close();
                    columnStatement.close();
                    
                    // 查询表信息
                    MySQLTableInfo mysqlTableInfo = new MySQLTableInfo();
                    mysqlTableInfo.setInstance(databaseInfo.getInstance());
                    mysqlTableInfo.setDatabaseName(databaseInfo.getDatabaseName());
                    mysqlTableInfo.setTableName(tableName);
                    mysqlTableInfo.setDescription(tableComment);
                    mysqlTableInfo.setColumnInfo(columnInfoJson);
                    
                    // 保存或更新表信息
                    LambdaQueryWrapper<MySQLTableInfo> tableQueryWrapper = new LambdaQueryWrapper<>();
                    tableQueryWrapper.eq(MySQLTableInfo::getInstance, databaseInfo.getInstance())
                            .eq(MySQLTableInfo::getDatabaseName, databaseInfo.getDatabaseName())
                            .eq(MySQLTableInfo::getTableName, tableName);
                    MySQLTableInfo existingTable = mySQLTableInfoService.getOne(tableQueryWrapper);
                    
                    if (existingTable != null) {
                        mysqlTableInfo.setId(existingTable.getId());
                        mysqlTableInfo.setCreateTime(existingTable.getCreateTime());
                        mysqlTableInfo.setManualReview(existingTable.getManualReview());
                        mySQLTableInfoService.updateById(mysqlTableInfo);
                    } else {
                        mysqlTableInfo.setCreateTime(new Date());
                        mySQLTableInfoService.save(mysqlTableInfo);
                    }
                }
            } catch (Exception e) {
                log.error("扫描数据库 {} 的表失败", databaseInfo.getDatabaseName(), e);
            } finally {
                // 关闭资源
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
                if (connection != null) connection.close();
            }
        }
    }
}
