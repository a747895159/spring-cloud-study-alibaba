package com.person.zb.alibaba.study.common.functional;

import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/7/6
 */
@FunctionalInterface
public interface FunListRtn<P, R> {

    List<R> execute(List<P> p);
}
