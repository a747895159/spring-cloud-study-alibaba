package com.person.zb.alibaba.study.common.functional;

import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/24
 */
@FunctionalInterface
public interface FunPageList<P, R> {
    List<R> execute(P p1,P p2);
}
