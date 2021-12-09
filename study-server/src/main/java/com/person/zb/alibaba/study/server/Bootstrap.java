package com.person.zb.alibaba.study.server;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/6
 */
@SpringBootApplication(exclude= DataSourceAutoConfiguration.class)
@SpringCloudApplication
@Slf4j
@EnableDiscoveryClient
public class Bootstrap {

    public static void main(String[] args) {
        SpringApplication.run(Bootstrap.class, args);
        log.info("Bootstrap started successfully");
    }
}
