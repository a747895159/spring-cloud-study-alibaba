package com.person.zb.alibaba.study.common.utils;


import com.person.zb.alibaba.study.common.exception.SystemException;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.HibernateValidator;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Set;

public class ValidateUtil {

    /**
     * 使用hibernate的注解来进行验证
     */
    private static javax.validation.Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure().failFast(true)
            .buildValidatorFactory().getValidator();

    /**
     * 验证对象
     * 验证不通过 抛出 GlobalException
     *
     * @param obj
     * @param <T>
     */
    public static <T> void validate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        // 抛出检验异常
        if (constraintViolations.size() > 0) {
            ConstraintViolation<T> item = constraintViolations.iterator().next();
            throw new SystemException(String.format("参数校验失败:[%s] %s", item.getPropertyPath(), item.getMessage()));
        }
    }

    /**
     * 字符串  验证是否为空字符串
     *
     * @param parameter 请求参数
     * @param message   错误描述
     */
    public static void validateStr(String parameter, String message) {
        if (StringUtils.isBlank(parameter)) {
            throw new SystemException(message);
        }
    }

    /**
     * 字符串  验证是否为空字符串
     *
     * @param parameter 请求参数
     * @param message   错误描述
     */
    public static void validateNotNull(Object parameter, String message) {
        if (parameter==null) {
            throw new SystemException(message);
        }
    }
}
