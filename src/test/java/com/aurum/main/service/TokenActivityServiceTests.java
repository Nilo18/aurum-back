package com.aurum.main.service;

import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static com.aurum.main.service.TokenActivityService.Decision.*;

class TokenActivityServiceTests {
    private final Instant now = Instant.parse("2026-10-30T10:00:00Z");
    private final Clock clock = mock(Clock.class);

    private TokenActivityService service(int requests, int denials, int capacity) {
        when(clock.instant()).thenReturn(now);
        return new TokenActivityService(clock, Duration.ofSeconds(60), requests, denials, capacity);
    }

    private TokenActivityService.TokenIdentity token(String id) {
        return new TokenActivityService.TokenIdentity(id, now.plusSeconds(3600), 42L);
    }

    @Test void requestThresholdRevokesOnlyTheOffendingTokenUntilExpiry() {
        var service = service(2, 5, 10);
        var token = token("one");
        assertEquals(ALLOW, service.beforeRequest(token));
        assertEquals(ALLOW, service.beforeRequest(token));
        assertEquals(REVOKED, service.beforeRequest(token));
        when(clock.instant()).thenReturn(now.plusSeconds(61));
        assertEquals(REVOKED, service.beforeRequest(token));
        assertEquals(ALLOW, service.beforeRequest(token("other-login")));
    }

    @Test void repeatedDenialsRevokeTokenAndNormalTrafficDoesNotClearDenials() {
        var service = service(100, 2, 10);
        var token = token("one");
        assertEquals(ALLOW, service.beforeRequest(token));
        service.recordForbidden(token);
        assertEquals(ALLOW, service.beforeRequest(token));
        service.recordForbidden(token);
        assertEquals(REVOKED, service.beforeRequest(token));
    }

    @Test void countersResetAfterWindowButRevocationDoesNot() {
        var service = service(1, 2, 10);
        var token = token("one");
        assertEquals(ALLOW, service.beforeRequest(token));
        service.recordForbidden(token);
        when(clock.instant()).thenReturn(now.plusSeconds(60));
        assertEquals(ALLOW, service.beforeRequest(token));
        service.recordForbidden(token);
        assertEquals(REVOKED, service.beforeRequest(token)); // Second request in this window.
    }

    @Test void missingOrExpiredIdentityIsRejected() {
        var service = service(10, 5, 10);
        assertEquals(REVOKED, service.beforeRequest(new TokenActivityService.TokenIdentity(null, now.plusSeconds(1), 42L)));
        assertEquals(REVOKED, service.beforeRequest(new TokenActivityService.TokenIdentity("one", null, 42L)));
        assertEquals(REVOKED, service.beforeRequest(new TokenActivityService.TokenIdentity("one", now, 42L)));
    }

    @Test void fullStoreDoesNotEvictLiveRevocationAndReclaimsExpiredEntries() {
        var service = service(1, 1, 1);
        var first = token("one");
        assertEquals(ALLOW, service.beforeRequest(first));
        service.recordForbidden(first);
        assertEquals(CAPACITY_REACHED, service.beforeRequest(token("two")));
        assertEquals(REVOKED, service.beforeRequest(first));
        when(clock.instant()).thenReturn(now.plusSeconds(3600));
        var fresh = new TokenActivityService.TokenIdentity("fresh", now.plusSeconds(7200), 42L);
        assertEquals(ALLOW, service.beforeRequest(fresh));
    }

    @Test void concurrentRequestsCannotExceedAllowance() throws Exception {
        var service = new TokenActivityService(Clock.fixed(now, ZoneOffset.UTC), Duration.ofSeconds(60), 3, 20, 10);
        var token = token("one");
        try (var executor = Executors.newFixedThreadPool(4)) {
            var tasks = new java.util.ArrayList<Callable<TokenActivityService.Decision>>();
            for (int i = 0; i < 20; i++) tasks.add(() -> service.beforeRequest(token));
            int allowed = 0;
            for (var result : executor.invokeAll(tasks, 5, TimeUnit.SECONDS)) {
                if (result.get() == ALLOW) allowed++;
            }
            assertEquals(3, allowed);
            assertEquals(REVOKED, service.beforeRequest(token));
        }
    }
}
