package com.person.zb.alibaba.study.common.annotation;


import java.lang.annotation.*;

/**
 * @author :  ZhouBin
 * @date :  2021-1-21
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = {ElementType.METHOD})
@Documented
public @interface PrintLog {
    /**
     * 打印日志中追加内容
     *
     */
    String value() default "";

    /**
     * 返回值字符串打印的长度, 小于 1 代表不打印返回值
     *
     */
    int prtRtnMaxLength() default 3000;
}



