package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTests {

    private final OtpService service = new OtpService();

    @Test void generatedCodeHasSixDigitsAndUniqueTransactionKey() {
        var first = service.generateOtp();
        var second = service.generateOtp();
        assertTrue(first.otp().matches("[0-9]{6}"));
        assertDoesNotThrow(() -> UUID.fromString(first.transactionKey()));
        assertNotEquals(first.transactionKey(), second.transactionKey());
    }

    @Test void correctCodeCanBeUsedOnlyOnce() {
        var otp = service.generateOtp();
        assertTrue(service.validateOtp(otp.transactionKey(), otp.otp()));
        assertFalse(service.validateOtp(otp.transactionKey(), otp.otp()));
    }

    @Test void wrongAttemptConsumesCode() {
        var otp = service.generateOtp();
        assertFalse(service.validateOtp(otp.transactionKey(), "wrong"));
        assertFalse(service.validateOtp(otp.transactionKey(), otp.otp()));
    }

    @Test void unknownOrInvalidatedTransactionFails() {
        assertFalse(service.validateOtp("unknown", "123456"));
        var otp = service.generateOtp();
        service.invalidateOtp(otp.transactionKey());
        assertFalse(service.validateOtp(otp.transactionKey(), otp.otp()));
    }

    @Test void expiredCodeIsRejectedWithoutWaiting() {
        var otp = service.generateOtp();
        Map<?, ?> storage = (Map<?, ?>) org.springframework.test.util.ReflectionTestUtils
                .getField(service, "inMemoryStorage");
        org.springframework.test.util.ReflectionTestUtils.setField(storage.get(otp.transactionKey()),
                "expiryTime", LocalDateTime.now().minusMinutes(1));
        assertFalse(service.validateOtp(otp.transactionKey(), otp.otp()));
    }

}
