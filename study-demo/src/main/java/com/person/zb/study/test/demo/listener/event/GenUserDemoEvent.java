package com.person.zb.study.test.demo.listener.event;

import com.person.zb.study.test.demo.model.bo.UserDemoBO;
import org.springframework.context.ApplicationEvent;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/10
 */
public class GenUserDemoEvent extends ApplicationEvent {

    private UserDemoBO userBO;

    public GenUserDemoEvent(Object source, UserDemoBO userBO) {
        super(source);
        this.userBO = userBO;
    }

    public UserDemoBO getUserBO() {
        return userBO;
    }

    public void setUserBO(UserDemoBO userBO) {
        this.userBO = userBO;
    }
}
