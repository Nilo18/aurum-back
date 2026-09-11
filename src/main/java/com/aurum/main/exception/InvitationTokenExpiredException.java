package com.aurum.main.exception;

public class InvitationTokenExpiredException extends RuntimeException {
    public InvitationTokenExpiredException(String message) {
        super(message);
    }
}
