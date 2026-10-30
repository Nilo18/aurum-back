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
class AuthServiceTests {

    @Mock OtpService otp;
    @Mock MailService mail;
    @Mock EmployeeRepository employees;
    @Mock org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder;
    @Mock JwtService jwt;
    @InjectMocks AuthService service;

    private final LoginRequest request = new LoginRequest("staff@example.com", "password",
            Employee.EmployeeRole.STAFF, "transaction", "123456");

    private Employee employee() {
        Employee employee = new Employee();
        employee.setEmail(request.email());
        employee.setPassword("encoded");
        employee.setRole(Employee.EmployeeRole.STAFF);
        return employee;
    }

    @Test void verificationSendsGeneratedCodeToRequestedEmail() {
        var response = new OtpResponse("transaction", "123456");
        when(otp.generateOtp()).thenReturn(response);
        assertEquals(response, service.verifyLoginRequest(new EmailRequest(request.email())));
        verify(mail).formatAndSend(anyString(), eq(request.email()), anyString(), contains("123456"));
    }

    @Test void invalidOtpDoesNotLookUpEmployeeOrIssueToken() {
        assertThrows(InvalidOtpException.class, () -> service.login(request));
        verifyNoInteractions(employees, encoder, jwt);
    }

    @Test void unknownEmployeeIsRejected() {
        when(otp.validateOtp("transaction", "123456")).thenReturn(true);
        when(employees.findByEmail(request.email())).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> service.login(request));
        verifyNoInteractions(encoder, jwt);
    }

    @Test void wrongPasswordDoesNotIssueToken() {
        when(otp.validateOtp("transaction", "123456")).thenReturn(true);
        when(employees.findByEmail(request.email())).thenReturn(Optional.of(employee()));
        assertThrows(InvalidPasswordException.class, () -> service.login(request));
        verifyNoInteractions(jwt);
    }

    @Test void wrongRoleDoesNotIssueToken() {
        Employee employee = employee();
        employee.setRole(Employee.EmployeeRole.ADMIN);
        when(otp.validateOtp("transaction", "123456")).thenReturn(true);
        when(employees.findByEmail(request.email())).thenReturn(Optional.of(employee));
        when(encoder.matches("password", "encoded")).thenReturn(true);
        assertThrows(InvalidRoleException.class, () -> service.login(request));
        verifyNoInteractions(jwt);
    }

    @Test void successfulLoginInvalidatesOtpAndIssuesEmployeeToken() {
        Employee employee = employee();
        when(otp.validateOtp("transaction", "123456")).thenReturn(true);
        when(employees.findByEmail(request.email())).thenReturn(Optional.of(employee));
        when(encoder.matches("password", "encoded")).thenReturn(true);
        when(jwt.generateEmployeeToken(employee)).thenReturn("signed-token");
        assertEquals(new AuthResponse(200, "signed-token"), service.login(request));
        verify(otp).invalidateOtp("transaction");
    }

    @Test void demoLoginDoesNotRequireEmployeeCredentials() {
        when(jwt.generateDemoToken()).thenReturn("demo-token");
        assertEquals(new AuthResponse(200, "demo-token"), service.loginAsDemo());
        verifyNoInteractions(otp, employees, encoder);
    }

}
