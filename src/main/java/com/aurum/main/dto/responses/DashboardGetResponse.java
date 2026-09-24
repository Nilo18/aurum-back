package com.aurum.main.dto.responses;

import com.aurum.main.dto.DashboardEventDTO;

import java.math.BigDecimal;
import java.util.List;

public record DashboardGetResponse(
        Long eventCount,
        Long awaitingReviewCount,
        BigDecimal portfolioValue,
        Long employeeCount,
        List<DashboardEventDTO> eventsInPreparation,
        Long clientCount,
        Long vehicleCount,
        Long productCount,
        Long supplierCount,
        Long menuCount,
        Long feedbackCount
) {
}
