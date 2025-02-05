package com.person.zb.alibaba.study.server.config.threadpool;

import lombok.Getter;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author : ZhouBin
 */
@Configuration
public class ThreadPoolConfig {

    /**
     * 通用异步-单线程使用
     */
    @Bean
    @RefreshScope
    public ThreadPoolExecutor singleAsyncExecutor(ThreadPoolProperties properties) {
        ThreadPoolProperties.PoolConfig poolConfig = properties.getCustom().get("Single-executor");
        return createPool(poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(), 10, "Single-executor");
    }

    /**
     * 通用多线程-快速执行使用
     */
    @Bean
    @RefreshScope
    public ThreadPoolExecutor commonExecExecutor(ThreadPoolProperties properties) {
        ThreadPoolProperties.PoolConfig poolConfig = properties.getCustom().get("Exec-executor");
        return createPool(poolConfig.getCorePoolSize(), poolConfig.getMaxPoolSize(), 10, "Exec-executor");
    }


    private ThreadPoolExecutor createPool(int coreSize, int maxPoolSize, int queueSize, String threadName) {
        return new ThreadPoolExecutor(coreSize, maxPoolSize, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(queueSize),
                new CustomThreadFactory(threadName), new ThreadPoolExecutor.CallerRunsPolicy());
    }


    @Getter
    public static class CustomThreadFactory implements ThreadFactory {
        private final String threadNamePrefix;
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        CustomThreadFactory(String threadNamePrefix) {
            this.threadNamePrefix = threadNamePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, threadNamePrefix + "-" + threadNumber.getAndIncrement());
            t.setDaemon(false);
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
