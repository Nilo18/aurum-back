package com.aurum.main.filter;

import com.aurum.main.dto.AuthenticatedUser;
import com.aurum.main.model.Employee;
import com.aurum.main.service.JwtService;
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

        try {
            if (!jwtService.isTokenValid(token)) {
                throw new BadCredentialsException("Invalid token");
            }

            Long userId = jwtService.extractUserId(token);
            String role = jwtService.extractRole(token);

            if (role == null) {
                throw new BadCredentialsException("Missing role");
            }

            Employee.EmployeeRole employeeRole =
                    Employee.EmployeeRole.valueOf(role);

            if (userId == null && employeeRole != Employee.EmployeeRole.DEMO) {
                throw new BadCredentialsException("Missing employee ID");
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
                    "Invalid or expired token"
            );
            return;
        }

        // Keep this outside the try/catch so downstream errors aren't
        // incorrectly treated as invalid tokens.
        filterChain.doFilter(request, response);
    }
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/error") || path.startsWith("/auth");
    }


}
