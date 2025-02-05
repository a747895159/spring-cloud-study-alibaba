package com.person.zb.alibaba.study.server.support;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConfigEventEnum {

    THREAD_POOL("threadpool.custom"),


    ;

    private String prefixName;

}
