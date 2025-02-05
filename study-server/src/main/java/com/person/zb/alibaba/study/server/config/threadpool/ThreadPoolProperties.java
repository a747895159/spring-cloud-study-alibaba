package com.person.zb.alibaba.study.server.config.threadpool;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

import java.util.Map;

/**
 * @author : ZhouBin
 */
@Data
@ConfigurationProperties(prefix = "threadpool")
@RefreshScope
@Slf4j
public class ThreadPoolProperties {

    private Map<String, PoolConfig> custom;


    @Data
    public static class PoolConfig {
        private int corePoolSize;
        private int maxPoolSize;
        private int queueCapacity;
    }
}
