package com.person.zb.alibaba.study.common.excel.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author : ZhouBin
 */
@Data
public class UploadFileInfoVO {

    @JsonProperty("fileuuid")
    private String fileUuid;

    @JsonProperty("filename")
    private String fileName;

    @JsonProperty("groupid")
    private String groupId;

    @JsonProperty("filesize")
    private Long fileSize;

    @JsonProperty("filetype")
    private String fileType;
}
