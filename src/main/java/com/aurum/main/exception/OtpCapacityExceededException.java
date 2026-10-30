package com.aurum.main.exception;

public class OtpCapacityExceededException extends RuntimeException {
    public OtpCapacityExceededException(String message) {
        super(message);
    }
}
