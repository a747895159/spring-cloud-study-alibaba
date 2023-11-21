package com.person.zb.alibaba.study.common.utils;

import com.person.zb.alibaba.study.common.functional.FunListRtn;
import com.person.zb.alibaba.study.common.model.PaginationData;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2021/8/4
 */
public class PageConvertUtil {

    public static  <P, R> PaginationData<R> convertPage(PaginationData<P> paginationData, FunListRtn<P, R> convert) {
        PaginationData<R> page = new PaginationData<>();
        page.setTotalPage(paginationData.getTotalPage());
        page.setTotalPage(paginationData.getTotalPage());
        page.setTotal(paginationData.getTotal());
        page.setPageSize(paginationData.getPageSize());
        page.setPageIndex(paginationData.getPageIndex());
        if (CollectionUtils.isEmpty(paginationData.getRows())) {
            page.setRows(new ArrayList<>());
            return page;
        }
        List<R> list = convert.execute(paginationData.getRows());
        page.setRows(list);
        return page;
    }
}
