package com.menglin.exception;

public class ArgumentsException extends RuntimeException {

    private static final long serialVersionUID = 10000000000L;

    private int code;

    public ArgumentsException(String message) {
        super(message);
        this.code = 500;
    }

    public ArgumentsException(int code, String message) {
        super(message);
        this.code = code;
    }

    public ArgumentsException(String message, Throwable cause) {
        super(message, cause);
        this.code = 500;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
