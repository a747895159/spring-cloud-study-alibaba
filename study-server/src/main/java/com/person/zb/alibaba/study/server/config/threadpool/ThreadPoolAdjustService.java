package com.person.zb.alibaba.study.server.config.threadpool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : ZhouBin
 */
@Component
@Slf4j
public class ThreadPoolAdjustService {

    @Autowired
    private List<ThreadPoolExecutor> executors;

    private final Map<String, ThreadPoolExecutor> executorMap = new HashMap<>(8);

    @PostConstruct
    public void init() {
        for (ThreadPoolExecutor executor : executors) {
            ThreadFactory threadFactory = executor.getThreadFactory();
            if (threadFactory instanceof ThreadPoolConfig.CustomThreadFactory) {
                ThreadPoolConfig.CustomThreadFactory customThreadFactory = (ThreadPoolConfig.CustomThreadFactory) threadFactory;
                executorMap.put(customThreadFactory.getThreadNamePrefix(), executor);
            }
        }
    }

    public void adjust(String threadName, ThreadPoolProperties.PoolConfig config) {
        ThreadPoolExecutor threadPoolExecutor = executorMap.get(threadName);
        if (threadPoolExecutor == null) {
            log.info("线程池名{},对应的线程不存在", threadName);
            return;
        }
        if (config.getMaxPoolSize() <= 0 || config.getCorePoolSize() > config.getMaxPoolSize()) {
            log.info("线程池名{},配置参数不合法", threadName);
            return;
        }
        int oldCoreSize = threadPoolExecutor.getCorePoolSize();
        int oldMaxSize = threadPoolExecutor.getMaximumPoolSize();
        threadPoolExecutor.setCorePoolSize(config.getCorePoolSize());
        threadPoolExecutor.setMaximumPoolSize(config.getMaxPoolSize());

        log.info("线程池:{},线程变化core: {} --》 {}，最大线程数 ：{} --》 {}", threadName, oldCoreSize, config.getCorePoolSize(), oldMaxSize, config.getMaxPoolSize());
    }
}
