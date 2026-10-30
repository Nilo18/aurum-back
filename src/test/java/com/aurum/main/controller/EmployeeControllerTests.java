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

class EmployeeControllerTests extends ControllerTestSupport {

    @Test void listPassesPrincipalRoleToService() throws Exception {
        when(employeeService.getEmployees(any(), eq(Employee.EmployeeRole.DEMO)))
                .thenReturn(new PageResponse<EmployeeDTO>(List.of(), 0, 5, 0));
        mvc.perform(get("/api/employee").header("Authorization", bearer("DEMO")).param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageSize").value(5));
        verify(employeeService).getEmployees(argThat(query -> query.getSize() == 5), eq(Employee.EmployeeRole.DEMO));
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/employee")).andExpect(status().isUnauthorized());
        verifyNoInteractions(employeeService);
    }

    @Test void ownerCanInviteEmployee() throws Exception {
        when(employeeService.inviteEmployee(any(), any(jakarta.servlet.http.HttpServletRequest.class))).thenReturn(new GenericResponse(200, "Invited"));
        mvc.perform(post("/api/employee/invite").header("Authorization", bearer("OWNER"))
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"type":"CHEF","salary":1000,"email":"staff@example.com","role":"STAFF"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Invited"));
    }

    @Test void adminCannotInviteOrDeleteEmployees() throws Exception {
        String auth = bearer("ADMIN");
        mvc.perform(post("/api/employee/invite").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"type":"CHEF","salary":1000,"email":"staff@example.com","role":"STAFF"}
                """))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/employee").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"staff@example.com\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(employeeService);
    }

    @Test void ownerCanDeleteEmployee() throws Exception {
        when(employeeService.deleteEmployee(any())).thenReturn(new GenericResponse(200, "Deleted"));
        mvc.perform(delete("/api/employee").header("Authorization", bearer("OWNER"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"staff@example.com\"}"))
                .andExpect(status().isOk());
        verify(employeeService).deleteEmployee(new EmailRequest("staff@example.com"));
    }

    @Test void invitationValidationAndRegistrationReturnServiceResponses() throws Exception {
        when(employeeService.validateInvitation(any())).thenReturn(new GenericResponse(200, "Valid"));
        when(employeeService.completeInviteRegistration(any())).thenReturn(new AuthResponse(200, "signed-token"));
        String auth = bearer("STAFF");
        mvc.perform(post("/api/employee/validate-invite").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"token\":\"invitation\"}"))
                .andExpect(status().isOk());
        mvc.perform(post("/api/employee/register").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"token":"invitation","name":"Alice","password":"password"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").value("signed-token"));
    }

    @Test void invalidInvitationAndRegistrationPayloadsAreRejected() throws Exception {
        String auth = bearer("OWNER");
        for (String path : List.of("/invite", "/validate-invite", "/register")) {
            mvc.perform(post("/api/employee" + path).header("Authorization", auth)
                    .contentType(MediaType.APPLICATION_JSON).content("{}"))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(employeeService);
    }

}
