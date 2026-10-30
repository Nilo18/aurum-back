package com.aurum.main.service;

public interface MailStrategy {
    void formatAndSend(String from, String to, String subject, String message);
}
