package com.person.zb.study.test.demo.listener;

import com.person.zb.study.test.demo.listener.event.UserDemoEvent;
import org.springframework.context.event.EventListener;

/**
 * @Desc: 注解方式监听 Listener
 * @Author: ZhouBin
 * @Date: 2021/9/10
 */
public class SpringEventListener {

    @EventListener
    public void genShelvedTask(UserDemoEvent event) {

    }
}
