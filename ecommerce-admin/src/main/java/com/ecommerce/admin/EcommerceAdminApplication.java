package com.ecommerce.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.ecommerce.common", "com.ecommerce.admin"})
@MapperScan({"com.ecommerce.mall.mapper", "com.ecommerce.admin.mapper"})
public class EcommerceAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceAdminApplication.class, args);
    }
}