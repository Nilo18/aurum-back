package com.aurum.main.service;

import com.aurum.main.model.Employee;
import io.jsonwebtoken.Claims;
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
import java.util.function.Function;

@Service
@Data
public class JwtService {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private String expiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }

    public String generateToken(String subject, Employee.EmployeeRole role, Long employeeId) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("role", role.name());

        if (employeeId != null) {
            claims.put("employeeId", employeeId);
        }

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date())
//                .expiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    public String generateEmployeeToken(Employee employee) {
        return generateToken(
                employee.getEmail(),
                Employee.EmployeeRole.valueOf(employee.getRole().name()),
                employee.getId()
        );
    }

    public String generateDemoToken() {
        return generateToken(
                "demo",
                Employee.EmployeeRole.DEMO,
                null
        );
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final io.jsonwebtoken.Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
