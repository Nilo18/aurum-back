package com.aurum.main.service;

import com.aurum.main.exception.OtpCapacityExceededException;
import com.aurum.main.utils.OtpPurpose;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {
    private static final Duration OTP_LIFETIME = Duration.ofMinutes(5);
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpChallenge> challenges = new ConcurrentHashMap<>();
    private final Clock clock;
    private final int capacity;

    public OtpService() {
        this(Clock.systemUTC(), 10_000);
    }

    OtpService(Clock clock, int capacity) {
        this.clock = Objects.requireNonNull(clock);
        if (capacity <= 0) {
            throw new IllegalArgumentException("OTP capacity must be positive");
        }
        this.capacity = capacity;
    }

    // Internal delivery data only. API responses must use OtpResponse instead.
    public record GeneratedOtp(String transactionKey, String otp) {
        @Override
        public String toString() {
            return "GeneratedOtp[transactionKey=" + transactionKey + ", otp=REDACTED]";
        }
    }

    private record OtpChallenge(String email, OtpPurpose purpose, String code, Instant expiresAt) {}

    public synchronized GeneratedOtp generateOtp(String email, OtpPurpose purpose) {
        if (email == null || email.isBlank() || purpose == null) {
            throw new IllegalArgumentException("Email and OTP purpose are required");
        }
        Instant now = clock.instant();
        challenges.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
        if (challenges.size() >= capacity) {
            throw new OtpCapacityExceededException("Verification is busy. Please try again later.");
        }

        String key = UUID.randomUUID().toString();
        String code = String.valueOf(100000 + secureRandom.nextInt(900000));
        challenges.put(key, new OtpChallenge(email, purpose, code, now.plus(OTP_LIFETIME)));
        return new GeneratedOtp(key, code);
    }

    // Every attempt consumes the challenge, including a mismatched email, purpose, or code.
    public boolean validateOtp(String key, String email, OtpPurpose purpose, String userOtp) {
        if (key == null) {
            return false;
        }
        OtpChallenge challenge = challenges.remove(key);
        return challenge != null
                && challenge.expiresAt().isAfter(clock.instant())
                && challenge.email().equals(email)
                && challenge.purpose() == purpose
                && challenge.code().equals(userOtp);
    }

    public void invalidateOtp(String transactionKey) {
        if (transactionKey != null) {
            challenges.remove(transactionKey);
        }
    }
}
