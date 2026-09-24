package com.aurum.main.service;

import com.aurum.main.dto.DashboardEventDTO;
import com.aurum.main.dto.responses.DashboardGetResponse;
import com.aurum.main.repository.*;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Data
public class DashboardService {
    private final EventRepository eventRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final VehicleRepository vehicleRepository;
    private final MenuItemRepository menuItemRepository;
    private final SupplierRepository supplierRepository;
    private final FeedbackRepository feedbackRepository;
    private final ProductRepository productRepository;

    public DashboardGetResponse getDashboardData() {
        long eventCount = eventRepository.count();
        List<DashboardEventDTO> eventsInPreparation = eventRepository.findByStatusInReview();
        long awaitingReviewCount = eventsInPreparation.size();
        BigDecimal portfolioValue = eventRepository.sumTotalValue();
        long employeeCount = employeeRepository.count();
        long clientCount = clientRepository.count();
        long vehicleCount = vehicleRepository.count();
        long productCount = productRepository.count();
        long supplierCount = supplierRepository.count();
        long menuCount = menuItemRepository.count();
        long feedbackCount = feedbackRepository.count();

        return new DashboardGetResponse(
                eventCount,
                awaitingReviewCount,
                portfolioValue,
                employeeCount,
                eventsInPreparation,
                clientCount,
                vehicleCount,
                productCount,
                supplierCount,
                menuCount,
                feedbackCount
        );
    }
}
