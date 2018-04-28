package com.menglin.common;

import java.io.Serializable;

public class ActionResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private T data;
    private Integer code;
    private String message;

    public void success(T data) {
        this.setData(data);
        this.setCode(200);
        this.setMessage("success");
    }

    public void fail(Integer code, String errorMessage) {
        this.setData(null);
        this.setCode(code);
        this.setMessage(errorMessage);
    }

    /**
     * 如果success为true，则返回controller action执行的结果，一些业务数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置controller action的执行结果
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取错误码
     */
    public Integer getCode() {
        return code;
    }

    /**
     * 设置错误码
     */
    public void setCode(Integer code) {
        this.code = code;
    }

    /**
     * 获取操作失败时的错误信息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置操作失败时的错误信息
     */
    public void setMessage(String message) {
        this.message = message;
    }
}