package com.person.zb.study.test.demo.annotation;

import java.lang.annotation.Annotation;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/2
 */

@Role(name = "小组长")
@Role(name = "小组长2")
@Role(name = "小组长3")
public class AnnotationTest {

    public static void main(String[] args) {
        Annotation annotation = AnnotationTest.class.getAnnotation(Role.class);
        System.out.println("Role annotation = " + annotation);
        Annotation annotation2 = AnnotationTest.class.getAnnotation(RoleUser.class);
        System.out.println("RoleUser annotation = " + annotation2);


        Annotation inheritedRoleAnnotation = InheritedAnnotation.class.getAnnotation(Role.class);
        System.out.println("inheritedRoleAnnotation  Role annotation = " + inheritedRoleAnnotation);
        Annotation inheritedRoleUserAnnotation = InheritedAnnotation.class.getAnnotation(RoleUser.class);
        System.out.println("inheritedRoleUserAnnotation  RoleUser annotation = " + inheritedRoleUserAnnotation);

    }

    /**
     * InheritedAnnotation  可以 继承父类的注解（Inherited标记的注解）
     */
    public class InheritedAnnotation extends AnnotationTest {

    }
}
