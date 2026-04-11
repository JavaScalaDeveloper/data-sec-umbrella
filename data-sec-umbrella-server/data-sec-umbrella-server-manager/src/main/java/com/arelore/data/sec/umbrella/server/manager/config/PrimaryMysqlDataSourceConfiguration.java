package com.arelore.data.sec.umbrella.server.manager.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * 显式声明主库 {@link DataSource} 且 {@link Primary}，避免在存在 ClickHouse 第二数据源时，
 * MyBatis-Plus 将唯一候选误绑到 ClickHouse，从而对 MySQL 表生成 CH 侧 SQL。
 */
@Configuration
@EnableConfigurationProperties(DataSourceProperties.class)
public class PrimaryMysqlDataSourceConfiguration {

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
    }
}
