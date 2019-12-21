package com.mmall.config;

import org.springframework.context.annotation.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan(basePackages = "com.mmall")
@EnableAspectJAutoProxy // spring-aop, 启用 AspectJ 自动代理
@EnableTransactionManagement
@Import({ApplicationDatasource.class})
public class ApplicationContextConfig {

}
