package com.aurum.main.service;

import com.aurum.main.model.Employee;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Data
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms:43200000}")
    private long expiration = 43_200_000L;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateToken(
            String subject, Employee.EmployeeRole role,
            Long employeeId, Date expiration) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", role.name());

        if (employeeId != null) {
            claims.put("employeeId", employeeId);
        }

        Date now = new Date();

        JwtBuilder builder = Jwts.builder()
                .claims(claims)
                .subject(subject)
                .id(UUID.randomUUID().toString())
                .issuedAt(new Date());

        if (expiration != null) {
            if (!expiration.after(now)) {
                throw new IllegalArgumentException("Expiration must be in the future");
            }

            builder.expiration(expiration);
        }

        return builder.signWith(getSigningKey())
                .compact();
    }

    public String generateEmployeeToken(Employee employee) {
        return generateToken(
                employee.getEmail(),
                Employee.EmployeeRole.valueOf(employee.getRole().name()),
                employee.getId(),
                new Date(System.currentTimeMillis() + expiration)
        );
    }

    public String generateDemoToken() {
        return generateToken(
                "demo",
                Employee.EmployeeRole.DEMO,
                null,
                new Date(System.currentTimeMillis() + 60 * 60 * 1000L)
        );
    }

    public String extractRole(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

    public Long extractUserId(String token) {
        return extractClaim(
                token,
                claims -> claims.get("employeeId", Long.class)
        );
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaim(token, Claims::getSubject);
            return true;
        } catch (io.jsonwebtoken.JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(parseClaims(token));
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
