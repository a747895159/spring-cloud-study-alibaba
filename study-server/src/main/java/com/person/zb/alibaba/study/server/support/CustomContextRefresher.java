package com.person.zb.alibaba.study.server.support;

import com.person.zb.alibaba.study.common.utils.CompletableUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author : ZhouBin
 */
@Slf4j
@Component
public class CustomContextRefresher extends ContextRefresher {


    public CustomContextRefresher(ConfigurableApplicationContext context,
                                  RefreshScope scope) {
        super(context, scope);
    }

    @Override
    public synchronized Set<String> refresh() {
        Set<String> keys = super.refresh();
        //处理变化的key
        handleChangeKey(keys);
        return keys;
    }


    public void handleChangeKey(Set<String> changeKeySet) {
        CompletableUtil.runSingleAsync(() -> {
            Map<ConfigEventEnum, List<String>> eventMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(changeKeySet)) {
                List<String> changeKeyList = new ArrayList<>();
                for (ConfigEventEnum value : ConfigEventEnum.values()) {
                    for (String key : changeKeySet) {
                        if (key.startsWith(value.getPrefixName())) {
                            changeKeyList.add(key);
                        }
                    }
                }
                if (CollectionUtils.isNotEmpty(changeKeyList)) {
                    eventMap.put(ConfigEventEnum.THREAD_POOL, changeKeyList);
                }
            }
            eventMap.forEach((k, v) -> super.getContext().publishEvent(new ConfigKeyEvent(k, v)));
        });
    }
}
