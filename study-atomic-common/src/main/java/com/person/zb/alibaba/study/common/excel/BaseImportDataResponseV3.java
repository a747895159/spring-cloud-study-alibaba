package com.person.zb.alibaba.study.common.excel;

import lombok.Data;

import java.util.List;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2022/5/5
 */
@Data
public class BaseImportDataResponseV3<T> {
    /**
     * 执行导入成功的记录
     */
    private List<T> sucList;

    /**
     * 执行导入失败的记录
     */
    private List<T> failList;
}
