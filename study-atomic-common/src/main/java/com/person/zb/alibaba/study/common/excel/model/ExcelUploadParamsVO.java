package com.person.zb.alibaba.study.common.excel.model;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ExcelUploadParamsVO {

    /**
     * 上传文件
     */
    private MultipartFile file;

    /**
     * 服务名称,默认为wms
     */
    private String ownServer;

    /**
     * 有效时间
     */
    private Long invalidTime;

    /**
     * 文件MD5加密
     */
    private String fileMd5;

    /**
     * group id ,默认为excel
     */
    private String groupId;

    /**
     * 操作人,默认为admin
     */
    private String operatorId;
}
