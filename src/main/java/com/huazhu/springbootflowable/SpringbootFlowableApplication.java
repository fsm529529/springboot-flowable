package com.huazhu.springbootflowable;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "com.huazhu")
@MapperScan(basePackages="com.huazhu.springbootflowable.modules.**.mapper")
public class SpringbootFlowableApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootFlowableApplication.class, args);
    }

}
