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
class JwtServiceTests {

    private final JwtService service = new JwtService();

    @BeforeEach void configureSigningKey() {
        service.setSecretKey(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }

    @Test void employeeTokenUsesEmailSubjectAndNumericEmployeeClaim() {
        var employee = new Employee();
        employee.setId(42L);
        employee.setEmail("staff@example.com");
        employee.setRole(Employee.EmployeeRole.STAFF);
        String token = service.generateEmployeeToken(employee);
        assertTrue(service.isTokenValid(token));
        assertEquals(42L, service.extractUserId(token));
        assertEquals("STAFF", service.extractRole(token));
        assertEquals(employee.getEmail(), service.extractClaim(token, io.jsonwebtoken.Claims::getSubject));
    }

    @Test void demoTokenHasNoEmployeeIdAndExpiresInOneHour() {
        long before = System.currentTimeMillis();
        String token = service.generateDemoToken();
        assertTrue(service.isTokenValid(token));
        assertNull(service.extractUserId(token));
        assertEquals("DEMO", service.extractRole(token));
        long expires = service.extractClaim(token, io.jsonwebtoken.Claims::getExpiration).getTime();
        assertTrue(expires >= before + 3_599_000L);
        assertTrue(expires <= System.currentTimeMillis() + 3_600_000L);
    }

    @Test void malformedAndDifferentlySignedTokensAreRejected() {
        assertFalse(service.isTokenValid("not-a-jwt"));
        assertFalse(service.isTokenValid(""));
        assertFalse(service.isTokenValid(null));
        String token = service.generateDemoToken();
        service.setSecretKey(Base64.getEncoder().encodeToString(
                "abcdef0123456789abcdef0123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        assertFalse(service.isTokenValid(token));
    }

    @Test void expiredSignedTokenIsRejected() {
        var key = io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(service.getSecretKey()));
        String expired = io.jsonwebtoken.Jwts.builder().subject("demo")
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(key).compact();
        assertFalse(service.isTokenValid(expired));
    }

    @Test void generationRejectsPastExpiration() {
        assertThrows(IllegalArgumentException.class, () -> service.generateToken("demo",
                Employee.EmployeeRole.DEMO, null, new Date(System.currentTimeMillis() - 60_000)));
    }

}
