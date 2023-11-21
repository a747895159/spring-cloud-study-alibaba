package com.person.zb.alibaba.study.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum YNEnum {

    /**
     *
     */
    UNDEFINED( 9,"未知"),
    NO( 0,"否"),
    YES( 1,"是");

    private final Integer code;
    private final String desc;


}
