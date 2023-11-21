package com.person.zb.alibaba.study.common.excel.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportResultDTO {

    private static final long serialVersionUID = 2579848296191150753L;
    /**
     * 备注: 主键
     * 字段名: mst_import_result.id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 备注: 仓库
     * 字段名: mst_import_result.warehouse_id
     */
    private Long warehouseId;

    /**
     * 备注: 关联对象id
     * 字段名: mst_import_result.ref_object_id
     */
    private Long refObjectId;

    /**
     * 备注: 文件名
     * 字段名: mst_import_result.file_name
     */
    private String fileName;

    /**
     * 备注: 状态:mst:lpn:lpn_lead_type
     * 字段名: mst_import_result.import_type
     */
    private Integer importType;

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
     * 备注: 状态:mst:lpn:lpn_lead_state
     * 字段名: mst_import_result.state
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

    /**
     * 备注: 创建时间
     * 字段名: mst_import_result.created_at
     */
    private Date createdAt;

    /**
     * 备注: 创建人
     * 字段名: mst_import_result.created_by
     */
    private String createdBy;

    /**
     * 备注: 修改时间
     * 字段名: mst_import_result.update_at
     */
    private Date updatedAt;

    /**
     * 备注: 修改人
     * 字段名: mst_import_result.update_by
     */
    private String updatedBy;

    /**
     * 备注: 删除:(0:有效,1:删除)
     * 字段名: mst_import_result.is_del
     */
    private Integer isDel;

    /**
     * 导入开始日期
     */
    private Date createStartTime;

    /**
     * 导入结束日期
     */
    private Date createEndTime;
}
