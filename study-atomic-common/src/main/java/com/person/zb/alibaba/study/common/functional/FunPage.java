package com.person.zb.alibaba.study.common.functional;


import com.person.zb.alibaba.study.common.model.PaginationData;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/24
 */
@FunctionalInterface
public interface FunPage<P, R> {
    PaginationData<R> execute(P p1, P p2);
}
