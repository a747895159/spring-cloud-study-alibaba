package com.person.zb.alibaba.study.server.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/12/9
 */
@RestController
@RequestMapping("/dc/manual")
@Slf4j
@RefreshScope
public class TestController {
    @Value("${testMobile:111}")
    private String  mobile;

    @GetMapping(value = "/test")
    public String test() {

        return mobile;
    }
}
