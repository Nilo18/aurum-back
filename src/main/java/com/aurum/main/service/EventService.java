package com.aurum.main.service;

import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.requests.EventOrderRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.exception.InvalidOtpException;
import com.aurum.main.model.Client;
import com.aurum.main.model.Event;
import com.aurum.main.model.MenuItem;
import com.aurum.main.repository.ClientRepository;
import com.aurum.main.repository.EventRepository;
import com.aurum.main.repository.MenuItemRepository;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Data
public class EventService {
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;
    private final MenuItemRepository menuItemRepository;
    private final MailService mailService;
    private final OtpService otpService;

    public OtpResponse verifyCreateEventRequest(EmailRequest request) {
        OtpResponse otpResponse = otpService.generateOtp();

        mailService.formatAndSend(
                "nikalongurashvili@gmail.com",
                request.email(),
                "Event Creation Request Verification",
                "Hello, here's your event creation request verification code: " +
                        otpResponse.otp()
        );

        return new OtpResponse(otpResponse.transactionKey(), otpResponse.otp());
    }

    @Transactional
    public GenericResponse createEvent(EventOrderRequest request) {
        boolean otpIsValid = otpService.validateOtp(request.transactionKey(), request.otp());

        if (!otpIsValid) {
            throw new InvalidOtpException("Invalid verification code");
        }

        Client client = clientRepository.findFirstByEmailOrPhone(
                request.client().email(), request.client().phone()
        ).orElseGet(() -> {
            Client newClient = new Client();
            newClient.setType(request.client().clientType());
            newClient.setName(request.client().name());
            newClient.setEmail(request.client().email());
            newClient.setPhone(request.client().phone());
            return newClient;
        });
        client = clientRepository.save(client);

        Event event = new Event();
        event.setClientId(client.getId());
        event.setEventType(request.event().eventType());
        event.setDate(request.event().date());
        List<Long> menuItemIds = request.menuItemIds();
        List<MenuItem> menuItems = menuItemRepository.findAllById(menuItemIds);
        BigDecimal totalCost = menuItems.stream().map(
                item -> item.getPricePerPerson().multiply(
                        BigDecimal.valueOf(request.event().guestCount()
                ))).reduce(BigDecimal.ZERO, BigDecimal::add);
        event.setTotalCost(totalCost);
        event.setGuestCount(request.event().guestCount());
        event.setLocation(request.event().location());
        event.setNotes(request.event().notes());
        eventRepository.save(event);

        otpService.invalidateOtp(request.transactionKey());

        return new GenericResponse(200, "Event added successfully!");
    }
}
