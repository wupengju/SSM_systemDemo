package com.menglin.enums;

public enum ErrorStateEnum {
    PARAMETER_ERROR(10000, "参数错误"),
    ILLEGEL_OPERATION(10001, "非法操作"), // 带攻击的访问
    DB_EXCEPTION(10002, "系统异常"), // 数据库访问异常
    TOKEN_TIMEOUT(10003, "Token超时，请重新登录"),
    TOKEN_VALIDATE_FAIL(10004, "Token校验不通过,请重新登录"),
    SIGNATURE_VALIDATE_FAIL(10005, "signature 校验不通过,请重新登录");

    private int state;
    private String stateInfo;

    private ErrorStateEnum(int state, String stateInfo) {
        this.state = state;
        this.stateInfo = stateInfo;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }
}
