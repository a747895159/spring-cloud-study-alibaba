package com.person.zb.alibaba.study.common.utils;

import com.google.common.collect.Lists;
import com.person.zb.alibaba.study.common.exception.SystemException;
import com.person.zb.alibaba.study.common.functional.*;
import com.person.zb.alibaba.study.common.model.PaginationData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/9/24
 */
@Slf4j
public class PageQueryUtil {

    public static final int MAX_SIZE = 50000;

    public static <R> List<R> query(int total, int pageSize, FunPageList<Integer, R> fun) {
        return query(total, pageSize, MAX_SIZE, fun);
    }

    public static <R> List<R> query(int total, int pageSize, int maxSize, FunPageList<Integer, R> fun) {
        int t = total;
        int pageNo = 1;
        List<R> list = new ArrayList<>();
        if (total > maxSize) {
            throw new SystemException("数据量不能超过" + maxSize);
        }
        while (t > 0) {
            List<R> rList = fun.execute(pageNo, pageSize);
            list.addAll(rList);
            pageNo++;
            t = t - pageSize;
        }
        log.info("查询总页数：{},总条数: {}", pageNo - 1, total);
        return list;
    }


    public static <R> List<R> query(FunPage<Integer, R> fun) {
        return query(MAX_SIZE, fun);
    }

    public static <R> List<R> query(int maxSize, FunPage<Integer, R> fun) {
        int pageIndex = 1;
        int pageSize = 500;
        PaginationData<R> page = fun.execute(pageIndex, pageSize);
        List<R> list = new ArrayList<>(page.getRows());
        long total = page.getTotal();
        if (total > maxSize) {
            throw new SystemException("数据量不能超过" + maxSize);
        }
        long t = total - pageIndex * pageSize;
        while (t > 0) {
            pageIndex++;
            page = fun.execute(pageIndex, pageSize);
            list.addAll(page.getRows());
            t = t - pageSize;

        }
        log.info("查询总页数：{},初始总条数: {},结束总条数：{}", pageIndex, total, page.getTotal());
        return list;
    }

    public <R> void queryAndWork(int total, int pageSize, FunPageList<Integer, R> queryFun, Fun<R> workFun) {
        int t = total;
        int pageNo = 1;
        while (t > 0) {
            List<R> list = queryFun.execute(pageNo, pageSize);
            pageNo++;
            t = t - pageSize;
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            CompletableUtil.runAndWaitAsync(list, workFun);
        }
    }

    public static <R> PaginationData<R> newPageInstance(int pageIndex, int pageSize, Class<R> clazz) {
        PaginationData<R> page = new PaginationData<R>();
        page.setTotal(0);
        page.setRows(Lists.newArrayList());
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    public static <R> PaginationData<R> listToPage(int pageIndex, int pageSize, List<R> sourceList) {
        PaginationData<R> page = new PaginationData<R>();
        page.setTotal(sourceList.size());
        page.setRows(sourceList);
        page.setPageIndex(pageIndex);
        page.setPageSize(pageSize);
        return page;
    }

    public static <P, K, R> Map<K, R> queryFunSet(Set<P> conditionSet, FunSetListRtn<P, R> fun, Function<R, K> function) {
        Map<K, R> rtnMap = new HashMap<>(8);
        if (CollectionUtils.isEmpty(conditionSet)) {
            return rtnMap;
        }
        conditionSet.remove(null);
        if (CollectionUtils.isEmpty(conditionSet)) {
            return rtnMap;
        }
        List<R> list = fun.execute(conditionSet);
        if (CollectionUtils.isEmpty(list)) {
            return rtnMap;
        }
        return list.stream().collect(Collectors.toMap(function, Function.identity(), (v1, v2) -> v1));
    }

    public static <P, K, R> Map<K, R> queryFunList(Set<P> conditionSet, FunListRtn<P, R> fun, Function<R, K> function) {
        Map<K, R> rtnMap = new HashMap<>(8);
        if (CollectionUtils.isEmpty(conditionSet)) {
            return rtnMap;
        }
        conditionSet.remove(null);
        if (CollectionUtils.isEmpty(conditionSet)) {
            return rtnMap;
        }
        List<R> list = fun.execute(new ArrayList<>(conditionSet));

        if (CollectionUtils.isEmpty(list)) {
            return rtnMap;
        }
        return list.stream().collect(Collectors.toMap(function, Function.identity(), (v1, v2) -> v1));
    }

}
