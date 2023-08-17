package com.example.hioa.workflow.exception;

import lombok.Data;

@Data
public class HioaException extends RuntimeException {
    private String msg;
    private int code = 500;

    public HioaException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public HioaException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public HioaException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public HioaException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }
}
