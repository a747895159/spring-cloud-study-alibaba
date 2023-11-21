package com.person.zb.alibaba.study.common;

import com.person.zb.alibaba.study.common.utils.RetryUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/6
 */
@Slf4j
public class A {

    public static void main(String[] args) {
        log.info("测试工具 :{}", 22);
        System.out.println(111);

        RetryUtil.execute(() -> {
            throw new RuntimeException("测试异常");
        }, 3, 1000);
    }
}
