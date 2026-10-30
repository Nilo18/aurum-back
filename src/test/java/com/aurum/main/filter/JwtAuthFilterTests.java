package com.aurum.main.filter;

import com.aurum.main.dto.AuthenticatedUser;
import com.aurum.main.model.Employee;
import com.aurum.main.service.*;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.*;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Base64;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

class JwtAuthFilterTests {
    private final JwtService jwt = new JwtService();
    private final TokenActivityService activity = new TokenActivityService(60, 3, 2, 10);
    private final JwtAuthFilter filter = new JwtAuthFilter(jwt, activity);

    @BeforeEach void setup() {
        SecurityContextHolder.clearContext();
        jwt.setSecretKey(Base64.getEncoder().encodeToString(
                "0123456789abcdef0123456789abcdef".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
    }
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    private MockHttpServletResponse request(String token, int downstreamStatus) throws Exception {
        SecurityContextHolder.clearContext();
        var request = new MockHttpServletRequest("GET", "/api/dashboard");
        request.addHeader("Authorization", "Bearer " + token);
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
            var principal = (AuthenticatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            assertEquals(Employee.EmployeeRole.DEMO, principal.role());
            ((jakarta.servlet.http.HttpServletResponse) res).setStatus(downstreamStatus);
        });
        return response;
    }

    @Test void requestBurstRevokesTokenAndAnotherLoginStillWorks() throws Exception {
        String token = jwt.generateDemoToken();
        for (int i = 0; i < 3; i++) assertEquals(200, request(token, 200).getStatus());
        assertEquals(401, request(token, 200).getStatus());
        assertEquals(401, request(token, 200).getStatus());
        assertEquals(200, request(jwt.generateDemoToken(), 200).getStatus());
    }

    @Test void forbiddenResponsesTriggerRevocationOnSubsequentRequest() throws Exception {
        String token = jwt.generateDemoToken();
        assertEquals(403, request(token, 403).getStatus());
        assertEquals(403, request(token, 403).getStatus());
        assertEquals(401, request(token, 200).getStatus());
    }

    @Test void invalidSignatureCannotConsumeValidTokensAllowance() throws Exception {
        String valid = jwt.generateDemoToken();
        var other = new JwtService();
        other.setSecretKey(Base64.getEncoder().encodeToString(
                "abcdef0123456789abcdef0123456789".getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        var claims = jwt.parseClaims(valid);
        String forged = io.jsonwebtoken.Jwts.builder().id(claims.getId()).claim("role", "DEMO")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(other.getSecretKey())))
                .compact();
        for (int i = 0; i < 5; i++) assertEquals(401, request(forged, 200).getStatus());
        for (int i = 0; i < 3; i++) assertEquals(200, request(valid, 200).getStatus());
    }

    @Test void tokensWithoutTrackingIdAreRejected() throws Exception {
        String legacy = io.jsonwebtoken.Jwts.builder().claim("role", "DEMO")
                .expiration(new Date(System.currentTimeMillis() + 3_600_000))
                .signWith(io.jsonwebtoken.security.Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwt.getSecretKey())))
                .compact();
        assertEquals(401, request(legacy, 200).getStatus());
    }

    @Test void fullStoreReturns503InsteadOfDiscardingRevocations() throws Exception {
        var smallFilter = new JwtAuthFilter(jwt, new TokenActivityService(60, 1, 1, 1));
        var first = new MockHttpServletRequest("GET", "/api/dashboard");
        first.addHeader("Authorization", "Bearer " + jwt.generateDemoToken());
        smallFilter.doFilter(first, new MockHttpServletResponse(), (req, res) -> {});
        SecurityContextHolder.clearContext();
        var second = new MockHttpServletRequest("GET", "/api/dashboard");
        second.addHeader("Authorization", "Bearer " + jwt.generateDemoToken());
        var response = new MockHttpServletResponse();
        smallFilter.doFilter(second, response, (req, res) -> fail("Must not reach application"));
        assertEquals(503, response.getStatus());
    }

    @Test void revokedOrInvalidTokenDoesNotPreventLogin() throws Exception {
        var request = new MockHttpServletRequest("POST", "/api/auth/login");
        request.addHeader("Authorization", "Bearer invalid");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> ((jakarta.servlet.http.HttpServletResponse) res).setStatus(200));
        assertEquals(200, response.getStatus());
    }
}
