package com.person.zb.alibaba.study.server.controller;

import com.person.zb.alibaba.study.server.event.BaseEvent;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/12/9
 */
@Api(tags = {"测试代码"})
@RestController
@RequestMapping("/dc/manual/event")
@Slf4j
@RefreshScope
public class EventTestController {

    @Resource
    private ApplicationContext applicationContext;

    @GetMapping(value = "/test01")
    public String test01() {
        applicationContext.publishEvent(new BaseEvent<>("001"));
        applicationContext.publishEvent(new BaseEvent<>(1));
        return "SUCCESS";
    }
}
