package com.person.zb.alibaba.study.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/6
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@SpringCloudApplication
@Slf4j
@EnableDiscoveryClient
@EnableSwagger2WebMvc
@ComponentScan(value = {"com.person.zb.alibaba.study", "springfox.documentation.schema"})
public class Bootstrap {

    public static void main(String[] args) {
        ConfigurableApplicationContext run =SpringApplication.run(Bootstrap.class, args);
        log.info("Bootstrap started successfully");

        // 5秒 自动退出
       /* try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int exitCode = SpringApplication.exit(run, () -> 0);
        System.exit(exitCode);*/

    }
}
