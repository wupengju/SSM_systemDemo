package com.menglin.enums;

public enum ErrorStateEnum {
    PARAMETER_ERROR(10000, "参数错误"),
    ILLEGAL_OPERATION(10001, "非法操作"), // 带攻击的访问
    DB_EXCEPTION(10002, "系统异常"), // 数据库访问异常
    TOKEN_TIMEOUT(10003, "Token超时，请重新登录"),
    TOKEN_VALIDATE_FAIL(10004, "Token校验不通过,请重新登录"),
    SIGNATURE_VALIDATE_FAIL(10005, "signature 校验不通过,请重新登录"),
    REDIS_ERROR(10006, "缓存异常"), // redis 异常
    DB_EXIST_SAME_RECORD(10007, "新添加记录已存在"),
    BUSINESS_ERROR(10008, "业务逻辑出错");

    private int state;
    private String stateInfo;

    ErrorStateEnum(int state, String stateInfo) {
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
