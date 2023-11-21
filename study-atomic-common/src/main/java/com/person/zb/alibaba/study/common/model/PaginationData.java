package com.person.zb.alibaba.study.common.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询封装类
 * 用于前端分页查询时, 返回给前端的记录信息。可以单独使用，也可与ResponseResult配合使用
 */
@Data
public class PaginationData<T> implements Serializable {
    /**
     * 返回的记录
     */
    private List<T> rows;
    /**
     * 记录的总条数(例如: 数据库表中总共有10W条数据)
     */
    private long total;
    /**
     * 请求第几页数据(例如: 请求第5页记录)
     */
    private int pageIndex;
    /**
     * 请求一页返回多少条记录(例如: 请求每页显示50条记录)
     */
    private int pageSize;

    /**
     * 总页数
     */
    private long totalPage;

    /**
     * 分页其他信息
     */
    private String message;

    public PaginationData() {

    }

    public PaginationData(int pageIndex, int pageSize) {
        this.pageIndex = pageIndex;
        this.pageSize = pageSize;
    }


}
