package com.person.zb.study.test.demo.annotation;

import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Desc: Spring AnnotationUtils 注解方法详释
 * @Author: ZhouBin
 * @Date: 2021/9/10
 * @see AnnotationUtils
 */
@RequestMapping
@Role(name = "小组长3")
public class SpringAnnotationUtils {

    public static void main(String[] args) {

        //本来，接口上的注解我们无论如何都继承不了了，但用了Spring的，你就可以
        //备注：哪怕@MyAnno上没有标注@Inherited，也是能找出来的（这是后面讲解@RequestMapping为何能被子类继承的重要原因）
        AnnotationUtils.findAnnotation(SpringAnnotationUtils.class, RequestMapping.class);


    }


}
