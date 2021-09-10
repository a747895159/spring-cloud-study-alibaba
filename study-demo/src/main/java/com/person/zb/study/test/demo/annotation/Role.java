package com.person.zb.study.test.demo.annotation;


import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// Repeatable: 一个注解的标记，可以重复的加在同一个目标。  多个Role 会映射到 RoleUser.value里面
@Repeatable(RoleUser.class)
// Inherited: 继承意思，其标记的类 可以被继承 只能发生在类上，而不能发生在接口上
@Inherited
@RequestMapping
public @interface Role {
    String name() default "普通学生";
}
