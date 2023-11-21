package com.person.zb.alibaba.study.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImportResultEnum {

    /**
     *
     */
    EXECUTING(1,"执行中"),
    EXECUTINGSUCCESS(2,"执行成功"),
    EXECUTINGFAILED(3,"执行失败"),
    EXECUTING_SUCCEED_DOWNLOAD_DETAIL(4, "执行成功,详情请下载明细查看"),
    EXECUTING_PARTIAL_SUCCEED_DOWNLOAD_DETAIL(5, "部分成功,详情请下载明细查看"),
    ;

    private Integer id;
    private String name;
}
