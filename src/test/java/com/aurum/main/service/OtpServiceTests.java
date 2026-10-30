package com.aurum.main.service;

import com.aurum.main.exception.OtpCapacityExceededException;
import com.aurum.main.utils.OtpPurpose;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OtpServiceTests {
    private static final String EMAIL = "alice@example.com";
    private final Instant now = Instant.parse("2026-10-30T10:00:00Z");
    private final Clock clock = mock(Clock.class);
    private final OtpService service = new OtpService(clock, 10);

    private OtpService.GeneratedOtp generate() {
        when(clock.instant()).thenReturn(now);
        return service.generateOtp(EMAIL, OtpPurpose.LOGIN);
    }

    private boolean validate(OtpService.GeneratedOtp otp) {
        return service.validateOtp(otp.transactionKey(), EMAIL, OtpPurpose.LOGIN, otp.otp());
    }

    @Test void generatesSixDigitCodeAndUniqueTransactionKey() {
        var first = generate();
        var second = generate();
        assertTrue(first.otp().matches("[0-9]{6}"));
        assertDoesNotThrow(() -> UUID.fromString(first.transactionKey()));
        assertNotEquals(first.transactionKey(), second.transactionKey());
        assertFalse(first.toString().contains(first.otp()));
    }

    @Test void correctCodeCanOnlyBeUsedOnce() {
        var otp = generate();
        assertTrue(validate(otp));
        assertFalse(validate(otp));
    }

    @Test void incorrectCodeConsumesChallenge() {
        var otp = generate();
        assertFalse(service.validateOtp(otp.transactionKey(), EMAIL, OtpPurpose.LOGIN, "wrong"));
        assertFalse(validate(otp));
    }

    @Test void codeCannotVerifyAnotherEmail() {
        var otp = generate();
        assertFalse(service.validateOtp(otp.transactionKey(), "other@example.com", OtpPurpose.LOGIN, otp.otp()));
        assertFalse(validate(otp));
    }

    @Test void codeCannotBeUsedForAnotherPurpose() {
        when(clock.instant()).thenReturn(now);
        for (var purpose : OtpPurpose.values()) {
            for (var other : OtpPurpose.values()) {
                var otp = service.generateOtp(EMAIL, purpose);
                assertEquals(purpose == other,
                        service.validateOtp(otp.transactionKey(), EMAIL, other, otp.otp()));
                assertFalse(service.validateOtp(otp.transactionKey(), EMAIL, purpose, otp.otp()));
            }
        }
    }

    @Test void expiryBoundaryIsRejectedAndConsumed() {
        var otp = generate();
        when(clock.instant()).thenReturn(now.plusSeconds(300));
        assertFalse(validate(otp));
        when(clock.instant()).thenReturn(now);
        assertFalse(validate(otp));
    }

    @Test void codeWorksImmediatelyBeforeExpiry() {
        var otp = generate();
        when(clock.instant()).thenReturn(now.plusSeconds(299));
        assertTrue(validate(otp));
    }

    @Test void missingOrInvalidatedChallengeFails() {
        assertFalse(service.validateOtp("unknown", EMAIL, OtpPurpose.LOGIN, "123456"));
        assertFalse(service.validateOtp(null, EMAIL, OtpPurpose.LOGIN, "123456"));
        var otp = generate();
        service.invalidateOtp(otp.transactionKey());
        assertFalse(validate(otp));
    }

    @Test void nullSubmittedValuesDoNotAuthenticate() {
        var otp = generate();
        assertFalse(service.validateOtp(otp.transactionKey(), null, OtpPurpose.LOGIN, otp.otp()));
        otp = generate();
        assertFalse(service.validateOtp(otp.transactionKey(), EMAIL, null, otp.otp()));
        otp = generate();
        assertFalse(service.validateOtp(otp.transactionKey(), EMAIL, OtpPurpose.LOGIN, null));
    }

    @Test void issuanceRequiresEmailAndPurpose() {
        assertThrows(IllegalArgumentException.class, () -> service.generateOtp(null, OtpPurpose.LOGIN));
        assertThrows(IllegalArgumentException.class, () -> service.generateOtp(" ", OtpPurpose.LOGIN));
        assertThrows(IllegalArgumentException.class, () -> service.generateOtp(EMAIL, null));
    }

    @Test void storageIsBoundedAndExpiredChallengesAreReclaimed() {
        var bounded = new OtpService(clock, 1);
        when(clock.instant()).thenReturn(now);
        var old = bounded.generateOtp(EMAIL, OtpPurpose.LOGIN);
        assertThrows(OtpCapacityExceededException.class, () -> bounded.generateOtp(EMAIL, OtpPurpose.CONTACT));
        when(clock.instant()).thenReturn(now.plusSeconds(300));
        var fresh = bounded.generateOtp(EMAIL, OtpPurpose.CONTACT);
        assertFalse(bounded.validateOtp(old.transactionKey(), EMAIL, OtpPurpose.LOGIN, old.otp()));
        assertTrue(bounded.validateOtp(fresh.transactionKey(), EMAIL, OtpPurpose.CONTACT, fresh.otp()));
    }

    @Test void concurrentAttemptsCanOnlySucceedOnce() throws Exception {
        // Fixed clock allows the concurrent test to exercise the actual store, without mock timing.
        var concurrent = new OtpService(Clock.fixed(now, ZoneOffset.UTC), 10);
        var otp = concurrent.generateOtp(EMAIL, OtpPurpose.LOGIN);
        var ready = new CountDownLatch(2);
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            java.util.concurrent.Callable<Boolean> attempt = () -> {
                ready.countDown();
                if (!start.await(5, TimeUnit.SECONDS)) throw new AssertionError("Start timed out");
                return concurrent.validateOtp(otp.transactionKey(), EMAIL, OtpPurpose.LOGIN, otp.otp());
            };
            var first = executor.submit(attempt);
            var second = executor.submit(attempt);
            try {
                assertTrue(ready.await(5, TimeUnit.SECONDS));
            } finally {
                start.countDown();
            }
            int successes = (first.get(5, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(5, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, successes);
        }
    }
}
