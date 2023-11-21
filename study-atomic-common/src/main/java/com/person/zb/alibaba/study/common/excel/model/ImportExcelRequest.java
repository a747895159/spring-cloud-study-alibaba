package com.person.zb.alibaba.study.common.excel.model;

import lombok.Data;


@Data
public class ImportExcelRequest {

    /**
     * 文件唯一标识
     */
    private String fileuuid;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件组
     */
    private String groupId;

    /**
     * 文件尺寸B
     */
    private Long fileSize;

    /**
     * 文件类型/后缀
     */
    private String fileType;

    /**
     * 导入类型
     */
    private Integer type;

    /**
     * 备注: 业务ID
     */
    private Long refObjectId;

    private Integer importMaxLimit;

    /**
     * 忽略excel校验逻辑
     */
    private Boolean ignoreValidateFlag;








}
