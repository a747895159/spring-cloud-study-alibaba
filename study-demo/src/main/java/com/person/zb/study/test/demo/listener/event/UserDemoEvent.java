package com.person.zb.study.test.demo.listener.event;

import com.person.zb.study.test.demo.model.bo.UserDemoBO;
import org.springframework.context.ApplicationEvent;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/10
 */
public class UserDemoEvent extends ApplicationEvent {

    private UserDemoBO userDemoBO;


    public UserDemoEvent(Object source) {
        super(source);
    }


    public UserDemoBO getUserDemoBO() {
        return userDemoBO;
    }

    public void setUserDemoBO(UserDemoBO userDemoBO) {
        this.userDemoBO = userDemoBO;
    }
}
