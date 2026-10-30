package com.aurum.main.filter;

import com.aurum.main.dto.AuthenticatedUser;
import com.aurum.main.model.Employee;
import com.aurum.main.service.JwtService;
import com.aurum.main.service.TokenActivityService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final TokenActivityService tokenActivityService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")
                || SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        TokenActivityService.TokenIdentity identity;
        try {
            // Parse and verify once before using any claim for tracking or authentication.
            var claims = jwtService.parseClaims(token);
            Long userId = claims.get("employeeId", Long.class);
            String role = claims.get("role", String.class);
            if (claims.getId() == null || claims.getId().isBlank() || claims.getExpiration() == null) {
                throw new BadCredentialsException("Missing token ID or expiration");
            }
            identity = new TokenActivityService.TokenIdentity(
                    claims.getId(), claims.getExpiration().toInstant(), userId);

            if (role == null) {
                throw new BadCredentialsException("Missing role");
            }

            Employee.EmployeeRole employeeRole =
                    Employee.EmployeeRole.valueOf(role);

            if (userId == null && employeeRole != Employee.EmployeeRole.DEMO) {
                throw new BadCredentialsException("Missing employee ID");
            }

            var decision = tokenActivityService.beforeRequest(identity);
            if (decision == TokenActivityService.Decision.REVOKED) {
                throw new BadCredentialsException("Token revoked");
            }
            if (decision == TokenActivityService.Decision.CAPACITY_REACHED) {
                response.setHeader("Retry-After", "60");
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Authentication temporarily unavailable");
                return;
            }

            AuthenticatedUser principal =
                    new AuthenticatedUser(userId, employeeRole);

            var authentication = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    List.of(new SimpleGrantedAuthority(employeeRole.name()))
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException
                 | BadCredentialsException ex) {
            SecurityContextHolder.clearContext();

            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid, expired, or revoked token"
            );
            return;
        }

        // Keep this outside the try/catch so downstream errors aren't
        // incorrectly treated as invalid tokens.
        try {
            filterChain.doFilter(request, response);
        } finally {
            if (response.getStatus() == HttpServletResponse.SC_FORBIDDEN) {
                tokenActivityService.recordForbidden(identity);
            }
        }
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.equals("/error") || path.startsWith("/error/")
                || path.equals("/api/auth") || path.startsWith("/api/auth/");
    }


}
