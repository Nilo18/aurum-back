package com.aurum.main.controller;

import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SecurityPolicyTests extends ControllerTestSupport {
    @Test void anonymousStaffReadsAndUnknownEndpointsRequireAuthentication() throws Exception {
        for (String path : List.of("/api/client", "/api/employee", "/api/dashboard", "/api/event",
                "/api/supplier", "/api/vehicle", "/api/product", "/api/feedback", "/api/menu", "/future-endpoint")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(eventService, clientService, employeeService, dashboardService,
                supplierService, vehicleService, productService, feedbackService, menuItemService);
    }

    @Test void publicPostRoutesRemainReachableWithoutAuthentication() throws Exception {
        for (String path : List.of("/api/auth/verify", "/contact", "/api/event/verify", "/api/event",
                "/api/feedback", "/api/feedback/verify-invitation",
                "/api/employee/validate-invite", "/api/employee/register")) {
            // Invalid payload gets validation's 400, proving authentication did not block it.
            mvc.perform(post(path).contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test void invitationCanBeAcceptedBeforeEmployeeHasLoginToken() throws Exception {
        when(employeeService.validateInvitation(any())).thenReturn(new GenericResponse(200, "Valid"));
        when(employeeService.completeInviteRegistration(any())).thenReturn(new AuthResponse(200, "signed-token"));
        mvc.perform(post("/api/employee/validate-invite").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"invitation\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/employee/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"invitation\",\"name\":\"Alice\",\"password\":\"password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("signed-token"));
    }

    @Test void publicExceptionsApplyOnlyToIntendedHttpMethods() throws Exception {
        mvc.perform(get("/api/auth/login")).andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/event")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/feedback/verify-invitation")).andExpect(status().isUnauthorized());
    }

    @Test void unexpectedErrorDetailsAreNotExposedInHttpResponse() throws Exception {
        when(dashboardService.getDashboardData()).thenThrow(new IllegalStateException("database-password=private-value"));
        mvc.perform(get("/api/dashboard").header("Authorization", bearer("STAFF")))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("An unexpected error occurred. Please try again later."))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("private-value"))));
    }

    @Test void unexpectedExceptionWithoutMessageStillProducesSafeResponse() {
        var response = new GlobalExceptionHandler().genericExceptionHandler(new RuntimeException());
        assertEquals(500, response.getStatusCode().value());
        assertEquals(java.util.Map.of("error", "An unexpected error occurred. Please try again later."), response.getBody());
    }
}
