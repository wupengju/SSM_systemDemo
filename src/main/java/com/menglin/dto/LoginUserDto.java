package com.menglin.dto;

import org.springframework.stereotype.Component;

@Component
public class LoginUserDto {
    private String username;
    private String signature;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}
