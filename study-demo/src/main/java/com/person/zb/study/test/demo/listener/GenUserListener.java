package com.person.zb.study.test.demo.listener;


import com.person.zb.study.test.demo.listener.event.GenUserDemoEvent;
import org.springframework.context.ApplicationListener;

/**
 * @Desc: 继承类方式监听 Listener
 * @Author: ZhouBin
 * @Date: 2021/9/10
 */
public class GenUserListener implements ApplicationListener<GenUserDemoEvent> {

    @Override
    public void onApplicationEvent(GenUserDemoEvent event) {

    }


}
