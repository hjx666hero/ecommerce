package com.ecommerce.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ecommerce.common", "com.ecommerce.mall"})
@MapperScan("com.ecommerce.mall.mapper")
public class EcommerceMallApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceMallApplication.class, args);
    }
}