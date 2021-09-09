package com.person.zb.alibaba.study.common.functional;

/**
 * @author :  ZhouBin
 * @date :  2021-05-20
 */
@FunctionalInterface
public interface Fun<P> {
    void execute(P p);
}
