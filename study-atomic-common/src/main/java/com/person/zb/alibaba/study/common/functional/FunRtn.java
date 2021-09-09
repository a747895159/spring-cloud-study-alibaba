package com.person.zb.alibaba.study.common.functional;

/**
 * @author :  ZhouBin
 * @date :  2021-05-20
 */
@FunctionalInterface
public interface FunRtn<P, R> {
    R execute(P p);
}
