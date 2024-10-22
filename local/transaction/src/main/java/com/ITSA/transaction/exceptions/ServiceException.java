package com.ITSA.transaction.exceptions;

public class ServiceException extends Exception {
    private final int errorCode;

    public ServiceException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
