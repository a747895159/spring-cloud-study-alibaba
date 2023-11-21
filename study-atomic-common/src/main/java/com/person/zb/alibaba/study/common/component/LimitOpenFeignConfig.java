package com.person.zb.alibaba.study.common.component;


import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;

@Slf4j
@Configuration
public class LimitOpenFeignConfig {


    @Bean(name = "limitYhErrorDecoder")
    @Primary
    public ErrorDecoder limitYHErrorDecoder() {

        return new LimitYhErrorDecoder();
    }

    @Bean
    public RequestInterceptor customizeInterceptor() {
        return template -> {
            if (!template.headers().containsKey("Accept")) {
                template.header("Accept", MediaType.APPLICATION_JSON_UTF8_VALUE);
            }
        };
    }
}
