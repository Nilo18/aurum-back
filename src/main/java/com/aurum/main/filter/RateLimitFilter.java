package com.aurum.main.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
@Order(-101)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final int maxBuckets = 10_000;
    private static final long idleExpiryNanos =
            Duration.ofMinutes(15).toNanos();

    private final Map<BucketKey, BucketEntry> buckets = new HashMap<>();

    private enum Group {
        OTP_SEND(10),
        LOGIN(10),
        INVITATION_CHECK(30);

        private final int requestsPerMinute;

        Group(int requestsPerMinute) {
            this.requestsPerMinute = requestsPerMinute;
        }
    }

    private record BucketKey(String ip, Group group) {}

    private static final class BucketEntry {
        private final Bucket bucket;
        private long lastAccess;

        private BucketEntry(Group group, long now) {
            bucket = Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            group.requestsPerMinute,
                            Refill.greedy(
                                    group.requestsPerMinute,
                                    Duration.ofMinutes(1)
                            )
                    ))
                    .build();

            lastAccess = now;
        }
    }

    private Group resolveGroup(HttpServletRequest request) {
        if (!"POST".equals(request.getMethod())) {
            return null;
        }

        // Exclude the application's context path.
        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        return switch (path) {
            case "/api/auth/verify",
                 "/verify-contact-request",
                 "/api/event/verify" -> Group.OTP_SEND;

            case "/api/auth/login" -> Group.LOGIN;

            case "/api/employee/validate-invite",
                 "/api/feedback/verify-invitation" ->
                    Group.INVITATION_CHECK;

            default -> null;
        };
    }

    private synchronized Bucket getBucket(String ip, Group group) {
        long now = System.nanoTime();
        BucketKey key = new BucketKey(ip, group);
        BucketEntry entry = buckets.get(key);

        if (entry != null
                && now - entry.lastAccess < idleExpiryNanos) {
            entry.lastAccess = now;
            return entry.bucket;
        }

        // Reclaim idle buckets before admitting a new one.
        buckets.entrySet().removeIf(
                item -> now - item.getValue().lastAccess
                        >= idleExpiryNanos
        );

        if (buckets.size() >= maxBuckets) {
            return null;
        }

        BucketEntry created = new BucketEntry(group, now);
        buckets.put(key, created);
        return created.bucket;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, java.io.IOException {

        Group group = resolveGroup(request);

        if (group == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = getBucket(request.getRemoteAddr(), group);

        if (bucket == null) {
            reject(response, 60);
            return;
        }

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (!probe.isConsumed()) {
            long retrySeconds = Math.max(
                    1L,
                    (long) Math.ceil(
                            probe.getNanosToWaitForRefill()
                                    / 1_000_000_000.0
                    )
            );

            reject(response, retrySeconds);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(
            HttpServletResponse response,
            long retrySeconds
    ) throws IOException, java.io.IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", Long.toString(retrySeconds));
        response.getWriter().write(
                "{\"error\":\"Too many requests. Please try again later.\"}"
        );
    }
}