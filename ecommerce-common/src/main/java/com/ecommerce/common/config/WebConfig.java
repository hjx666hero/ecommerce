package com.ecommerce.common.config;

import com.ecommerce.common.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    public WebConfig(LoginInterceptor loginInterceptor) {
        this.loginInterceptor = loginInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**", "/admin/**")
                .excludePathPatterns(
                        "/api/auth/**",
                        "/api/home/**",
                        "/api/product/list",
                        "/api/product/detail/**",
                        "/api/product/search",
                        "/api/product/category/**",
                        "/api/review/list/**",
                        "/api/seckill/list",
                        "/api/seckill/detail/**",
                        "/api/internal/**",
                        "/admin/auth/**"
                );
    }
}