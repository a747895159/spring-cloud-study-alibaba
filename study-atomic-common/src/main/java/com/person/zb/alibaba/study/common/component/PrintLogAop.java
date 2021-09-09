package com.person.zb.alibaba.study.common.component;

import com.alibaba.fastjson.JSONObject;
import com.yonghui.wms.framework.annotation.PrintLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author :  ZhouBin
 * @date :  2021-1-21
 */
@Component
@Aspect
@Slf4j
public class PrintLogAop {

    @Around(value = "@annotation(printLog)")
    public Object doLog(ProceedingJoinPoint joinPoint, PrintLog printLog) throws Throwable {
        String targetClassName = null;
        String methodName = null;
        String logPrefix;
        try {
            targetClassName = joinPoint.getTarget().getClass().getSimpleName();
            String value = printLog.value();
            methodName = joinPoint.getSignature().getName();
            Object[] params = joinPoint.getArgs();
            logPrefix = "【 {} --> {} 】 " + value;
            if (params != null && params.length != 0) {
                List<Object> paramList = new ArrayList<>();
                for (Object param : params) {
                    if (!(param instanceof ServletRequest || param instanceof ServletResponse
                            || param instanceof InputStream || param instanceof OutputStream)) {
                        paramList.add(param);
                    }
                }
                if (paramList.size() > 0) {
                    log.info(logPrefix + ",请求参数：{}", targetClassName, methodName, JSONObject.toJSONString(paramList));
                }
            }
        } catch (Exception e) {
            logPrefix = null;
            log.warn("PrintLogAop 入参打印异常", e);
        }
        Object rtnObj = joinPoint.proceed();
        try {
            if (logPrefix != null && printLog.prtRtnMaxLength() > 0) {
                String logStr = JSONObject.toJSONString(rtnObj);
                if (logStr.length() > printLog.prtRtnMaxLength()) {
                    logStr = logStr.substring(0, printLog.prtRtnMaxLength()) + " ...";
                }
                log.info(logPrefix + ",返回数据: {}", targetClassName, methodName, logStr);
            }
        } catch (Exception e) {
            log.warn("PrintLogAop 返回数据打印异常", e);
        }
        return rtnObj;
    }
}
