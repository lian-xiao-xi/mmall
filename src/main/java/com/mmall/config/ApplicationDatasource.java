package com.mmall.config;

import com.github.pagehelper.PageHelper;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.mapper.MapperScannerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class ApplicationDatasource {

  @Bean
  @Profile("dev")
  public BasicDataSource dataSource(
      @Value("${db.driverClassName}") String driverClassName,
      @Value("${db.url}") String url,
      @Value("${db.username}") String username,
      @Value("${db.password}") String password,
      @Value("${db.initialSize}") Integer initialSize,
      @Value("${db.maxActive}") Integer maxActive,
      @Value("${db.maxIdle}") Integer maxIdle,
      @Value("${db.minIdle}") Integer minIdle,
      @Value("${db.maxWait}") Integer maxWait,
      @Value("${db.defaultAutoCommit}") Boolean defaultAutoCommit
  ) {
    BasicDataSource basicDataSource = new BasicDataSource();
    basicDataSource.setDriverClassName(driverClassName);
    basicDataSource.setUrl(url);
    basicDataSource.setUsername(username);
    basicDataSource.setPassword(password);
    basicDataSource.setInitialSize(initialSize);
    basicDataSource.setMaxActive(maxActive);
    basicDataSource.setMaxIdle(maxIdle);
    basicDataSource.setMinIdle(minIdle);
    basicDataSource.setMaxWait(maxWait);
    basicDataSource.setDefaultAutoCommit(defaultAutoCommit);
    basicDataSource.setTimeBetweenEvictionRunsMillis(40000L);
    basicDataSource.setTestWhileIdle(true);
    basicDataSource.setValidationQuery("SELECT 1 FROM dual");
    return basicDataSource;
  }

  @Bean
  public PageHelper pageHelper() {
    PageHelper pageHelper = new PageHelper();
    Properties properties = new Properties();
    properties.setProperty("dialect", "mysql");
    pageHelper.setProperties(properties);
    return pageHelper;
  }

  @Bean
  public MapperScannerConfigurer mapperScannerConfigurer() {
    MapperScannerConfigurer scannerConfigurer = new MapperScannerConfigurer();
    scannerConfigurer.setBasePackage("com.mmall.dao");
    return scannerConfigurer;
  }

  @Bean
  public SqlSessionFactoryBean sqlSessionFactory(DataSource dataSource, PageHelper pageHelper) {
    SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
    sqlSessionFactoryBean.setDataSource(dataSource);
    sqlSessionFactoryBean.setMapperLocations(new ClassPathResource[] {new ClassPathResource("/mappers/*Mapper.xml")});
    sqlSessionFactoryBean.setPlugins(new Interceptor[]{pageHelper});
    return sqlSessionFactoryBean;
  }

  @Bean
  public DataSourceTransactionManager dataSourceTransactionManager(DataSource dataSource) {
    DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
    dataSourceTransactionManager.setRollbackOnCommitFailure(true);
    dataSourceTransactionManager.setDataSource(dataSource);
    return dataSourceTransactionManager;
  }


}
