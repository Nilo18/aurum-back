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

class DashboardControllerTests extends ControllerTestSupport {

    @Test void anonymousDashboardIsUnauthorized() throws Exception {
        mvc.perform(get("/api/dashboard")).andExpect(status().isUnauthorized());
        verifyNoInteractions(dashboardService);
    }

    @Test void authenticatedDashboardReturnsMetrics() throws Exception {
        when(dashboardService.getDashboardData()).thenReturn(new DashboardGetResponse(1L, 0L, BigDecimal.TEN,
                2L, List.of(), 3L, 4L, 5L, 6L, 7L, 8L));
        mvc.perform(get("/api/dashboard").header("Authorization", bearer("DEMO")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.eventCount").value(1))
                .andExpect(jsonPath("$.portfolioValue").value(10));
        verify(dashboardService).getDashboardData();
    }

    @Test void invalidTokenIsUnauthorizedWithoutCallingService() throws Exception {
        mvc.perform(get("/api/dashboard").header("Authorization", "Bearer invalid"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(dashboardService);
    }

}
