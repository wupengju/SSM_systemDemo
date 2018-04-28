package com.menglin.enums;

public enum ResultCodeEnum {

    RESULT_CODE_SUCCESS(200, "成功处理请求"),
    RESULT_CODE_UNAUTHORIZED(401, "无权限"),
    RESULT_CODE_FORBIDDEN(403, "禁止访问"),
    RESULT_CODE_NOT_FOUND(404, "未找到资源"),
    RESULT_CODE_SERVER_ERROR(500, "服务端出错");


    private int state;
    private String stateInfo;

    private ResultCodeEnum(int state, String stateInfo) {
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
