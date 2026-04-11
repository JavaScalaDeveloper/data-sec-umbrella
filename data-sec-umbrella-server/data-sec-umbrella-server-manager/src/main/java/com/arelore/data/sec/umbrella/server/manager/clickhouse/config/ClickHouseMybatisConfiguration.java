package com.arelore.data.sec.umbrella.server.manager.clickhouse.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;

/**
 * ClickHouse 独立数据源 + MyBatis-Plus（与 MySQL 主库隔离，仅用于离线快照查询）。
 */
@Configuration
@ConditionalOnProperty(name = "clickhouse.enabled", havingValue = "true")
@MapperScan(
        basePackages = "com.arelore.data.sec.umbrella.server.manager.mapper.clickhouse",
        sqlSessionFactoryRef = "clickHouseSqlSessionFactory"
)
public class ClickHouseMybatisConfiguration {

    @Bean(name = "clickHouseDataSource")
    public DataSource clickHouseDataSource(
            @Value("${clickhouse.jdbc-url}") String jdbcUrl,
            @Value("${clickhouse.username:default}") String username,
            @Value("${clickhouse.password:}") String password
    ) {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(username);
        ds.setPassword(password == null ? "" : password);
        ds.setPoolName("clickhouse-offline-snapshot");
        ds.setMaximumPoolSize(4);
        ds.setMinimumIdle(0);
        return ds;
    }

    @Bean(name = "clickHouseSqlSessionFactory")
    public SqlSessionFactory clickHouseSqlSessionFactory(@Qualifier("clickHouseDataSource") DataSource ds) throws Exception {
        MybatisSqlSessionFactoryBean bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(ds);
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.CLICK_HOUSE);
        page.setOverflow(true);
        interceptor.addInnerInterceptor(page);
        bean.setPlugins(interceptor);
        bean.setTypeAliasesPackage("com.arelore.data.sec.umbrella.server.core.entity.clickhouse");
        bean.setTypeHandlersPackage("com.arelore.data.sec.umbrella.server.core.typehandler");
        GlobalConfig globalConfig = new GlobalConfig();
        bean.setGlobalConfig(globalConfig);
        return bean.getObject();
    }
}
