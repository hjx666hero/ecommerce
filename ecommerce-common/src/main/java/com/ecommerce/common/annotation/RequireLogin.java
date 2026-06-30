package com.ecommerce.common.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireLogin {
    /** 是否需要登录，默认true */
    boolean value() default true;
}