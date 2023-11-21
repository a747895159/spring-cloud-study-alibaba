package com.person.zb.alibaba.study.common.exception;

import lombok.Data;

/**
 * @author : ZhouBin
 */

@Data
public class SystemException extends RuntimeException {

    private String code;

    private String message;

    public SystemException() {
        defaultErrCode();
    }

    public SystemException(String message) {
        super(message);
        this.message = message;
        defaultErrCode();
    }

    public SystemException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        defaultErrCode();
    }

    public SystemException(Throwable cause) {
        super(cause);
        defaultErrCode();
    }

    public SystemException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    private void defaultErrCode() {
        this.code = "50000";
    }


}
