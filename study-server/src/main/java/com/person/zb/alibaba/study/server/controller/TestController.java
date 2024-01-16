package com.person.zb.alibaba.study.server.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/12/9
 */
@Api(tags = {"测试代码"})
@RestController
@RequestMapping("/dc/manual")
@Slf4j
@RefreshScope
public class TestController {
    @Value("${testMobile:111}")
    private String mobile;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping(value = "/test")
    public String test() {
        String key = "111";
        redisTemplate.opsForValue().set(key, "你哈哈");
        log.info("  {} 测试 0----", redisTemplate.opsForValue().get(key));
        String s = stringRedisTemplate.opsForValue().get("111");
        return mobile + "_" + s;
    }
}
