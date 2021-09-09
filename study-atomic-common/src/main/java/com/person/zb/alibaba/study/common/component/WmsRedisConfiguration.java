package com.person.zb.alibaba.study.common.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/8
 */

@Configuration
//@ConditionalOnBean(value = RedisTemplate.class)
public class WmsRedisConfiguration {

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate;


    @Bean
    public RedisCacheComponent redisCacheComponent(@Value("${spring.application.name}") String nameSpace) {
        return new RedisCacheComponent(nameSpace, redisTemplate);
    }


}
