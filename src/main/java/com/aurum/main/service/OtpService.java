package com.aurum.main.service;

import com.aurum.main.dto.responses.OtpResponse;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final int otpExpiry = 5;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpData> inMemoryStorage = new ConcurrentHashMap<>();

    private static class OtpData {
        String code;
        LocalDateTime expiryTime;

        OtpData(String code, LocalDateTime expiryTime) {
            this.code = code;
            this.expiryTime = expiryTime;
        }
    }

    public OtpResponse generateOtp() {
        String transactionKey = UUID.randomUUID().toString();

        int code = 100000 + secureRandom.nextInt(900000);
        String otpStr = String.valueOf(code);

        LocalDateTime expiry = LocalDateTime.now().plusMinutes(otpExpiry);
        inMemoryStorage.put(transactionKey, new OtpData(otpStr, expiry));

        return new OtpResponse(transactionKey, otpStr);
    }

    public boolean validateOtp(String key, String userOtp) {
        OtpData otpData = inMemoryStorage.get(key);

        if (otpData == null) {
            return false;
        }

        inMemoryStorage.remove(key);

        if (otpData.expiryTime.isBefore(LocalDateTime.now())) {
            return false;
        }

        return otpData.code.equals(userOtp);
    }
}
