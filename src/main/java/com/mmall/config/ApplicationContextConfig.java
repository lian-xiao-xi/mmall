package com.mmall.config;

import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.mmall")
@EnableAspectJAutoProxy // spring-aop, 启用 AspectJ 自动代理
@EnableTransactionManagement
@Import({ApplicationDatasource.class})
@PropertySource(value = "classpath:datasource.properties", ignoreResourceNotFound = true)
public class ApplicationContextConfig {
  @Bean
  public static PropertySourcesPlaceholderConfigurer placeholderConfigurer() {
    PropertySourcesPlaceholderConfigurer placeholderConfigurer = new PropertySourcesPlaceholderConfigurer();
    placeholderConfigurer.setFileEncoding("UTF-8");
//    placeholderConfigurer.setLocation(new ClassPathResource("datasource.properties"));
//    placeholderConfigurer.setIgnoreResourceNotFound(true);
    return placeholderConfigurer;
  }
}
