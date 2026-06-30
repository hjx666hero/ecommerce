package com.ecommerce.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.VelocityTemplateEngine;

import java.sql.Types;
import java.util.Collections;

/**
 * MyBatis-Plus 代码生成器
 */
public class CodeGenerator {

    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/ecommerce?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "root";

        FastAutoGenerator.create(url, username, password)
                .globalConfig(builder -> builder
                        .author("ecommerce")
                        .outputDir("f:\\store\\ecommerce\\ecommerce-mall\\src\\main\\java")
                        .disableOpenDir()
                )
                .packageConfig(builder -> builder
                        .parent("com.ecommerce.mall")
                        .entity("entity")
                        .mapper("mapper")
                        .service("service")
                        .serviceImpl("service.impl")
                        .controller("controller")
                        .pathInfo(Collections.singletonMap(OutputFile.xml,
                                "f:\\store\\ecommerce\\ecommerce-mall\\src\\main\\resources\\mapper"))
                )
                .strategyConfig(builder -> builder
                        .addTablePrefix("")
                        .entityBuilder()
                        .enableLombok()
                        .enableTableFieldAnnotation()
                        .logicDeleteColumnName("deleted")
                        .mapperBuilder()
                        .enableBaseResultMap()
                        .serviceBuilder()
                        .formatServiceFileName("%sService")
                        .controllerBuilder()
                        .enableRestStyle()
                )
                .templateEngine(new VelocityTemplateEngine())
                .execute();
    }
}