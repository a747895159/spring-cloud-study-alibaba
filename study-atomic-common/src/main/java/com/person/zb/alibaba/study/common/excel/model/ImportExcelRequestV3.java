package com.person.zb.alibaba.study.common.excel.model;

import lombok.Data;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2022/5/5
 */
@Data
public class ImportExcelRequestV3 extends ImportExcelRequest {

    /**
     * 记录id
     */
    private Long id;

    /**
     * 仓库Id
     */
    private Long warehouseId;
    /**
     * 用户
     */
    private String userId;

    /**
     * 文件大小(MB)
     */
    private Integer maxFileSize;

    /**
     * 每个sheet中最大行数
     */
    private Integer maxLineNum;

    /**
     * 真实有效行数
     */
    private Integer realRowNum;

    /**
     * excel解析后数据行大小
     */
    private Integer excelDataSize;
}
