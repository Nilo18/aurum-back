package com.aurum.main.controller;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTests extends ControllerTestSupport {

    @Test void loginIsPublicAndReturnsToken() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(new AuthResponse(200, "signed-token"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                {"email":"staff@example.com","password":"password","role":"STAFF","transactionKey":"key","otp":"123456"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("signed-token"));
        verify(authService).login(new LoginRequest("staff@example.com", "password", Employee.EmployeeRole.STAFF, "key", "123456"));
    }

    @Test void verificationIsPublicAndValidatesEmail() throws Exception {
        when(authService.verifyLoginRequest(any())).thenReturn(new OtpResponse("key", "123456"));
        mvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"staff@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transactionKey").value("key"));
        mvc.perform(post("/api/auth/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"invalid\"}")).andExpect(status().isBadRequest());
        verify(authService, times(1)).verifyLoginRequest(any());
    }

    @Test void demoLoginIsPublic() throws Exception {
        when(authService.loginAsDemo()).thenReturn(new AuthResponse(200, "demo-token"));
        mvc.perform(get("/api/auth/demo")).andExpect(status().isOk()).andExpect(jsonPath("$.token").value("demo-token"));
    }

    @Test void incorrectCredentialsReturnConfiguredBadRequestResponse() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidPasswordException("Invalid password"));
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid password"));
    }

}
