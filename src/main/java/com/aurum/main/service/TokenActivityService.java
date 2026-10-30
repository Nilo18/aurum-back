package com.aurum.main.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/** Single-instance activity tracking. Entries, including revocations, live until JWT expiration. */
@Service
public class TokenActivityService {
    private static final Logger log = LoggerFactory.getLogger(TokenActivityService.class);
    public enum Decision { ALLOW, REVOKED, CAPACITY_REACHED }
    public record TokenIdentity(String id, Instant expiresAt, Long userId) {}

    private static final class Activity {
        final Instant expiresAt;
        Instant windowStart;
        long requests;
        long denials;
        boolean revoked;

        Activity(Instant expiresAt, Instant now) {
            this.expiresAt = expiresAt;
            this.windowStart = now;
        }
    }

    private final Map<String, Activity> activity = new HashMap<>();
    private final Clock clock;
    private final Duration window;
    private final int maxRequests;
    private final int maxDenials;
    private final int capacity;

    @Autowired
    public TokenActivityService(
            @Value("${security.token-activity.window-seconds:60}") long windowSeconds,
            @Value("${security.token-activity.max-requests:300}") int maxRequests,
            @Value("${security.token-activity.max-denials:20}") int maxDenials,
            @Value("${security.token-activity.capacity:10000}") int capacity) {
        this(Clock.systemUTC(), Duration.ofSeconds(windowSeconds), maxRequests, maxDenials, capacity);
    }

    TokenActivityService(Clock clock, Duration window, int maxRequests, int maxDenials, int capacity) {
        if (window.isZero() || window.isNegative() || maxRequests <= 0 || maxDenials <= 0 || capacity <= 0) {
            throw new IllegalArgumentException("Token activity limits must be positive");
        }
        this.clock = clock;
        this.window = window;
        this.maxRequests = maxRequests;
        this.maxDenials = maxDenials;
        this.capacity = capacity;
    }

    public synchronized Decision beforeRequest(TokenIdentity token) {
        Instant now = clock.instant();
        if (token.id() == null || token.id().isBlank()
                || token.expiresAt() == null || !token.expiresAt().isAfter(now)) {
            return Decision.REVOKED;
        }
        Activity state = activity.get(token.id());
        if (state == null) {
            activity.entrySet().removeIf(entry -> !entry.getValue().expiresAt.isAfter(now));
            // Never evict a live revocation to make room for another token.
            if (activity.size() >= capacity) {
                return Decision.CAPACITY_REACHED;
            }
            state = new Activity(token.expiresAt(), now);
            activity.put(token.id(), state);
        }
        if (state.revoked) {
            return Decision.REVOKED;
        }
        resetWindowIfElapsed(state, now);
        if (++state.requests > maxRequests) {
            revoke(state, token, "request_threshold");
            return Decision.REVOKED;
        }
        return Decision.ALLOW;
    }

    public synchronized void recordForbidden(TokenIdentity token) {
        Activity state = activity.get(token.id());
        Instant now = clock.instant();
        if (state == null || state.revoked || !state.expiresAt.isAfter(now)) {
            return;
        }
        resetWindowIfElapsed(state, now);
        if (++state.denials >= maxDenials) {
            revoke(state, token, "permission_denial_threshold");
        }
    }

    private void resetWindowIfElapsed(Activity state, Instant now) {
        if (!now.isBefore(state.windowStart.plus(window))) {
            state.windowStart = now;
            state.requests = 0;
            state.denials = 0;
        }
    }

    private void revoke(Activity state, TokenIdentity token, String reason) {
        state.revoked = true;
        // Only generated token IDs and numeric user IDs are logged; never the bearer token.
        log.warn("JWT revoked: tokenId={}, userId={}, reason={}", token.id(), token.userId(), reason);
    }
}
