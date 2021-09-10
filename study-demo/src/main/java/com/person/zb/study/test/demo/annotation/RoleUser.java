package com.person.zb.study.test.demo.annotation;

import java.lang.annotation.*;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/2
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)


@Inherited
public @interface RoleUser {

    Role[] value();

}
