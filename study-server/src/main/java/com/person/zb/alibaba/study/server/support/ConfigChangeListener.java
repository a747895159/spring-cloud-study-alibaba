package com.person.zb.alibaba.study.server.support;

import com.person.zb.alibaba.study.server.config.threadpool.ThreadPoolAdjustService;
import com.person.zb.alibaba.study.server.config.threadpool.ThreadPoolProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author : ZhouBin
 */
@Slf4j
@Configuration
public class ConfigChangeListener {

    @Autowired
    private ThreadPoolProperties threadPoolProperties;

    @Autowired
    private ThreadPoolAdjustService threadPoolAdjustService;

    /**
     * #动态线程池配置
     * threadpool:
     *   custom:
     *     Single-executor:
     *       corePoolSize: 6
     *       maxPoolSize: 15
     *       queueCapacity: 100000
     *     Exec-executor:
     *       corePoolSize: 8
     *       maxPoolSize: 15
     *       queueCapacity: 100000
     *
     * @param event
     */
    @EventListener(condition = "#event.eventEnum == T(com.person.zb.alibaba.study.server.support.ConfigEventEnum).THREAD_POOL")
    public void handleCustomEvent(ConfigKeyEvent event) {
        log.info("线程池变化的key事件处理");
        List<String> changeKeySet = event.getKeys();
        Set<String> threadNameSet = new HashSet<>();
        if (CollectionUtils.isNotEmpty(changeKeySet)) {
            String threadPoolPrefix = ConfigEventEnum.THREAD_POOL.getPrefixName();
            for (String key : changeKeySet) {
                if (key.startsWith(threadPoolPrefix)) {
                    String temp = key.substring(threadPoolPrefix.length() + 1);
                    String threadName = temp.substring(0, temp.indexOf("."));
                    threadNameSet.add(threadName);
                }

            }
        }
        for (String threadName : threadNameSet) {
            ThreadPoolProperties.PoolConfig poolConfig = threadPoolProperties.getCustom().get(threadName);
            if (poolConfig != null) {
                threadPoolAdjustService.adjust(threadName, poolConfig);
            }
        }
    }

    @EventListener
    public void handleCustomEvent002(ConfigKeyEvent event) {
        log.info("所有变化的key事件处理");
    }
}
