package com.aurum.main.service;

import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.requests.LoginRequest;
import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.exception.EmployeeNotFoundException;
import com.aurum.main.exception.InvalidOtpException;
import com.aurum.main.exception.InvalidPasswordException;
import com.aurum.main.exception.InvalidRoleException;
import com.aurum.main.model.Employee;
import com.aurum.main.repository.EmployeeRepository;
import lombok.Data;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Data
public class AuthService {
    private final OtpService otpService;
    private final MailService mailService;
    private final EmployeeRepository employeeRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public OtpResponse verifyLoginRequest(EmailRequest request) {
        OtpResponse otpRes = otpService.generateOtp();

        mailService.formatAndSend(
                "nikalongurashvili@gmail.com",
                request.email(),
                "Login Request Verification",
                "Hello, here's your login request verification code: " + otpRes.otp()
        );

        return new OtpResponse(otpRes.transactionKey(), otpRes.otp());
    }

    public AuthResponse login(LoginRequest request) {
        boolean otpIsValid = otpService.validateOtp(request.transactionKey(), request.otp());

        if (!otpIsValid) {
            throw new InvalidOtpException("Invalid verification code");
        }

        Employee employee = employeeRepository.findByEmail(request.email())
                .orElseThrow(() -> new EmployeeNotFoundException("Employee not found"));

        boolean passwordIsCorrect = passwordEncoder.matches(request.password(), employee.getPassword());

        if (!passwordIsCorrect) {
            throw new InvalidPasswordException("Invalid password");
        }

        boolean roleIsCorrect = employee.getRole().equals(request.role());

        if (!roleIsCorrect) {
            throw new InvalidRoleException("Invalid role");
        }

        otpService.invalidateOtp(request.transactionKey());
        String token = jwtService.generateEmployeeToken(employee);

        return new AuthResponse(200, token);
    }
}