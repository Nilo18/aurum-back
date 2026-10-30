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
class DashboardServiceTests {

    @Mock EventRepository events;
    @Mock ClientRepository clients;
    @Mock EmployeeRepository employees;
    @Mock VehicleRepository vehicles;
    @Mock MenuItemRepository menu;
    @Mock SupplierRepository suppliers;
    @Mock FeedbackRepository feedback;
    @Mock ProductRepository products;
    @InjectMocks DashboardService service;

    @Test void aggregatesIndependentCountsAndReviewEvents() {
        var review = List.of(new DashboardEventDTO(Event.EventType.WEDDING, LocalDate.now(), 25, "Alice"));
        when(events.count()).thenReturn(11L);
        when(events.findByStatusInReview()).thenReturn(review);
        when(events.sumTotalValue()).thenReturn(new BigDecimal("1234.50"));
        when(employees.count()).thenReturn(2L);
        when(clients.count()).thenReturn(3L);
        when(vehicles.count()).thenReturn(4L);
        when(products.count()).thenReturn(5L);
        when(suppliers.count()).thenReturn(6L);
        when(menu.count()).thenReturn(7L);
        when(feedback.count()).thenReturn(8L);
        assertEquals(new DashboardGetResponse(11L, 1L, new BigDecimal("1234.50"), 2L,
                review, 3L, 4L, 5L, 6L, 7L, 8L), service.getDashboardData());
    }

    @Test void emptyDashboardHasNoReviewEvents() {
        when(events.findByStatusInReview()).thenReturn(List.of());
        when(events.sumTotalValue()).thenReturn(BigDecimal.ZERO);
        var result = service.getDashboardData();
        assertEquals(0L, result.awaitingReviewCount());
        assertEquals(0L, result.eventCount());
        assertTrue(result.eventsInPreparation().isEmpty());
        assertEquals(BigDecimal.ZERO, result.portfolioValue());
    }

}
