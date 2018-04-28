package com.menglin.dto;

public class JWTCheckInfo {
    private String authorization;
    private String currentName;
    private String requestUrI;

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getCurrentName() {
        return currentName;
    }

    public void setCurrentName(String currentName) {
        this.currentName = currentName;
    }

    public String getRequestUrI() {
        return requestUrI;
    }

    public void setRequestUrI(String requestUrI) {
        this.requestUrI = requestUrI;
    }
}
