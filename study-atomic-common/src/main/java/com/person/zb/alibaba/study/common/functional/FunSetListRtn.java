package com.person.zb.alibaba.study.common.functional;

import java.util.List;
import java.util.Set;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/8/9
 */
@FunctionalInterface
public interface FunSetListRtn<P, R> {

    List<R> execute(Set<P> set);

}
