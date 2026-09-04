package com.aurum.main.dto.requests;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String emailOrPhone;
    private String otp;
}
