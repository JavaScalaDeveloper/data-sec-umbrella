package com.arelore.data.sec.umbrella.server.manager.config;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

/**
 * 在存在 ClickHouse 第二数据源时，显式声明主库 {@link SqlSessionFactory} 且 {@link Primary}，
 * 并强制绑定 {@code dataSource}（MySQL），避免 core.mapper 误用 {@code clickHouseSqlSessionFactory}。
 * <p>
 * MyBatis-Plus 自动配置在「多 DataSource」场景下依赖「唯一候选或带 @Primary 的 DataSource」；
 * 若主库 Bean 未被识别为唯一候选，可能不注册默认 {@code sqlSessionFactory}，导致 Mapper 落错库。
 * <p>
 * 条件必须使用「按 Bean 名称」判断：{@code clickHouseSqlSessionFactory} 的类型也是
 * {@link SqlSessionFactory}，若使用 {@code @ConditionalOnMissingBean(SqlSessionFactory.class)}
 * 会误判为已存在，从而既不注册本 Bean，MP 也因已有 SqlSessionFactory 而跳过，最终缺少名为
 * {@code sqlSessionFactory} 的 Bean。
 */
@Configuration
public class PrimaryMysqlSqlSessionFactoryConfiguration {

    @Bean(name = "sqlSessionFactory")
    @Primary
    @DependsOn("dataSource")
    @ConditionalOnMissingBean(name = "sqlSessionFactory")
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dataSource") DataSource mysqlDataSource,
            MybatisPlusProperties properties,
            ObjectProvider<MybatisPlusInterceptor> mybatisPlusInterceptorProvider
    ) throws Exception {
        MybatisSqlSessionFactoryBean factoryBean = new MybatisSqlSessionFactoryBean();
        factoryBean.setDataSource(mysqlDataSource);

        Resource[] mapperLocations = properties.resolveMapperLocations();
        if (mapperLocations != null && mapperLocations.length > 0) {
            factoryBean.setMapperLocations(mapperLocations);
        }
        if (properties.getTypeAliasesPackage() != null) {
            factoryBean.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }
        if (properties.getTypeHandlersPackage() != null) {
            factoryBean.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }

        MybatisConfiguration configuration = new MybatisConfiguration();
        if (properties.getConfiguration() != null) {
            properties.getConfiguration().applyTo(configuration);
        }
        factoryBean.setConfiguration(configuration);
        factoryBean.setGlobalConfig(properties.getGlobalConfig());

        MybatisPlusInterceptor interceptor = mybatisPlusInterceptorProvider.getIfAvailable();
        if (interceptor != null) {
            factoryBean.setPlugins(interceptor);
        }

        SqlSessionFactory factory = factoryBean.getObject();
        if (factory == null) {
            throw new IllegalStateException("MySQL SqlSessionFactory 初始化失败");
        }
        return factory;
    }
}
