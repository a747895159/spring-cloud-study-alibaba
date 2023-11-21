package com.person.zb.alibaba.study.common.excel.model;

import lombok.Builder;
import lombok.Data;

/**
 * @Desc:
 * @Author: ZhouBin
 * @Date: 2022/5/5
 */
@Data
@Builder
public class ImportResultV3DTO {

    private Long id;

    private Long warehouseId;

    /**
     * 备注: 导入文件下载链接
     */
    private String importUrl;

    /**
     * 备注: 成功文件下载链接
     */
    private String successUrl;

    /**
     * 备注: 失败文件下载链接
     */
    private String failUrl;

    /**
     * 备注: 导入总数量
     */
    private Integer importNum;

    /**
     * 备注: 导入成功数量
     */
    private Integer successNum;

    /**
     * 备注: 导入失败数量
     */
    private Integer failNum;


    /**
     * 状态:  参考 MstImpResultState
     */
    private Integer state;

    /**
     * 备注: 结果
     * 字段名: mst_import_result.result
     */
    private String result;

    /**
     * 备注: 备注
     * 字段名: mst_import_result.comments
     */
    private String comments;

}
