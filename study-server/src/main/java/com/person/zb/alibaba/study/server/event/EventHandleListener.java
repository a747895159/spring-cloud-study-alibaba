package com.person.zb.alibaba.study.server.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author : ZhouBin
 */
@Slf4j
@Component
public class EventHandleListener {

    @EventListener
    public void handleEventStr(BaseEvent<String> event) {
        log.info("监听String事件:{}", event.getData());
    }

    @EventListener
    public void handleEventInt(BaseEvent<Integer> event) {
        log.info("监听Integer事件:{}", event.getData());
    }
}
