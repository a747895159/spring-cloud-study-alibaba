package com.person.zb.alibaba.study.common.functional;

import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/8/9
 */
@FunctionalInterface
public interface FunList<R> {

    List<R> execute();
}
