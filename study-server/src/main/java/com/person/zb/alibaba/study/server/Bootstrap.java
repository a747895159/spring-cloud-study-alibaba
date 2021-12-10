package com.person.zb.alibaba.study.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/6
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@SpringCloudApplication
@Slf4j
@EnableDiscoveryClient
@ComponentScan("com.person.zb.alibaba.study")
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
        log.info("Bootstrap started successfully");
    }
}
