package com.aurum.main.exception;

public class FeedbackInvitationAlreadyUsedException extends RuntimeException {
    public FeedbackInvitationAlreadyUsedException(String message) {
        super(message);
    }
}
