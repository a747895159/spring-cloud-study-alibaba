package com.person.zb.alibaba.study.common.component;

import com.alibaba.fastjson.JSONObject;
import com.person.zb.alibaba.study.common.exception.SystemException;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;

import java.io.IOException;
import java.util.Objects;


@Slf4j
public class LimitYhErrorDecoder implements ErrorDecoder {

    @Override
    public Exception decode(String methodKey, Response response) {
        /*if (response.status() == 401) {
            UnauthorizedException r = new UnauthorizedException("无访问权限，对方接口配置了@OriginService限定访问来源");
            ExceptionLogger.log(r);
            return r;
        }*/

        if (response.status() >= 400) {
            // 解析json
            JSONObject jsonObject = null;
            String body;
            try {
                body = Util.toString(response.body().asReader());
                log.debug("error body : " + body);
                jsonObject = JSONObject.parseObject(body);
            } catch (IOException e) {
                return new Exception(jsonObject.getString("exception") + " : " + jsonObject.getString("message"));
            }
            // 创建异常
            try {
                if (jsonObject == null) {
                    return new RuntimeException("服务提供异常:" + response.status());
                }
                String limitMsg = jsonObject.getString("message");
                if (Objects.equals(jsonObject.getString("code"), "90000") && limitMsg != null && limitMsg.contains("限流")) {
                    log.warn("接口被限流：{}", JSONObject.toJSONString(jsonObject));
                    return new SystemException(jsonObject.getString("code"), jsonObject.getString("message"));
                }
                // 反序列化异常信息
                Object e = jsonObject.get("exception");
                Exception exception;
                if (e == null) {
                    // 不是异常
                    String message = jsonObject.get("status") + " " + jsonObject.get("path") + " : " + jsonObject.get("error");
                    exception = new RuntimeException(message);
                } else {
                    Class exceptionClass = Class.forName(jsonObject.getString("exception"));
                    String exceptionDetailStr = jsonObject.getString("exception_detail");
                    if (exceptionClass == HttpMessageNotReadableException.class) {
                        exception = new HttpMessageNotReadableException("From Server : " + jsonObject.getString("message"));
                    } else {
                        exception = (Exception) JSONObject.parseObject(exceptionDetailStr, exceptionClass);
                    }
                }
                return exception;
            } catch (Exception e) {
                // 其他异常
                Exception exception = new Exception("服务提供方返回：" + body);
                log.error("异常反序列化失败，服务提供方返回：{}, " + body, e);
                return exception;
            }
        }
        return new Exception("methodKey : " + response.reason());
    }

}
