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

class ClientControllerTests extends ControllerTestSupport {

    @Test void listPassesAuthenticatedRoleAndQueryToService() throws Exception {
        when(clientService.getClients(any(), eq(Employee.EmployeeRole.DEMO)))
                .thenReturn(new PageResponse<ClientDTO>(List.of(), 0, 5, 0));
        mvc.perform(get("/api/client").header("Authorization", bearer("DEMO")).param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageSize").value(5));
        verify(clientService).getClients(argThat(query -> query.getSize() == 5), eq(Employee.EmployeeRole.DEMO));
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/client")).andExpect(status().isUnauthorized());
        verifyNoInteractions(clientService);
    }

    @Test void staffCannotDeleteClient() throws Exception {
        mvc.perform(delete("/api/client").header("Authorization", bearer("STAFF"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@example.com\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(clientService);
    }

    @Test void adminCanDeleteAndMissingClientReturns404() throws Exception {
        when(clientService.deleteClient(new EmailRequest("alice@example.com"))).thenReturn(new GenericResponse(200, "Deleted"));
        when(clientService.deleteClient(new EmailRequest("missing@example.com"))).thenThrow(new ClientNotFoundException("Missing"));
        String auth = bearer("ADMIN");
        mvc.perform(delete("/api/client").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        mvc.perform(delete("/api/client").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"missing@example.com\"}"))
                .andExpect(status().isNotFound());
    }

}
