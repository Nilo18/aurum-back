package com.aurum.main.service;

import com.aurum.main.dto.EventDTO;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.requests.EventOrderRequest;
import com.aurum.main.dto.requests.EventQuery;
import com.aurum.main.dto.requests.UpdateEventRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.*;
import com.aurum.main.model.Client;
import com.aurum.main.model.Event;
import com.aurum.main.model.FeedbackInvitation;
import com.aurum.main.model.MenuItem;
import com.aurum.main.repository.*;
import com.aurum.main.utils.OtpPurpose;
import lombok.Data;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Data
public class EventService {
    private final ClientRepository clientRepository;
    private final EventRepository eventRepository;
    private final SearchStrategy<EventDTO, EventQuery> eventStrategy;
    private final MenuItemRepository menuItemRepository;
    private final MailStrategy mailStrategy;
    private final OtpService otpService;
    private final FeedbackInvitationRepository feedbackInvitationRepository;

    public OtpResponse verifyCreateEventRequest(EmailRequest request) {
        var otpResponse = otpService.generateOtp(request.email(), OtpPurpose.CREATE_EVENT);

        mailStrategy.formatAndSend(
                "nikalongurashvili@gmail.com",
                request.email(),
                "Event Creation Request Verification",
                "Hello, here's your event creation request verification code: " +
                        otpResponse.otp()
        );

        return new OtpResponse(otpResponse.transactionKey());
    }

    @Transactional
    public GenericResponse createEvent(EventOrderRequest request) {
        boolean otpIsValid = otpService.validateOtp(
                request.transactionKey(), request.client().email(), OtpPurpose.CREATE_EVENT, request.otp());

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
        String publicId = UUID.randomUUID().toString();
        event.setPublicId(publicId);
        event.setClientId(client.getId());
        event.setEventType(request.event().eventType());
        event.setDate(request.event().date());
        List<String> menuItemPublicIds = request.menuItemPublicIds();
        List<MenuItem> menuItems = menuItemRepository.findAllByPublicIdIn(menuItemPublicIds);
        BigDecimal totalCost = menuItems.stream().map(
                item -> item.getPricePerPerson().multiply(
                        BigDecimal.valueOf(request.event().guestCount()
                ))).reduce(BigDecimal.ZERO, BigDecimal::add);
        event.setTotalCost(totalCost);
        event.setGuestCount(request.event().guestCount());
        event.setLocation(request.event().location());
        event.setNotes(request.event().notes());
        eventRepository.save(event);


        return new GenericResponse(200, "Event added successfully!");
    }

    public EventDTO updateEvent(UpdateEventRequest request, HttpServletRequest servlet) {
        Event event = eventRepository.findByPublicId(request.publicId())
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        if (event.getStatus() == Event.EventStatus.COMPLETED
                || event.getStatus() == Event.EventStatus.CANCELLED
                || event.getStatus() == Event.EventStatus.REJECTED) {

            throw new EventCannotBeUpdatedException(
                    "Completed, cancelled, or rejected events cannot be modified"
            );
        }

        if (request.status() == Event.EventStatus.COMPLETED && event.getDate().isAfter(LocalDate.now())) {
            throw new EventCannotBeUpdatedException(
                    "An event cannot be completed before its scheduled date"
            );
        }

        String publicId = UUID.randomUUID().toString();
        event.setPublicId(publicId);
        event.setEventType(request.eventType());
        event.setDate(request.date());
        event.setTotalCost(request.totalCost());
        event.setGuestCount(request.guestCount());
        event.setLocation(request.location());
        event.setStatus(request.status());
        event = eventRepository.save(event);

        Client client = clientRepository.findById(event.getClientId())
                .orElseThrow(() -> new ClientNotFoundException("Client not found"));

        if (request.status() == Event.EventStatus.COMPLETED) {
            String token = UUID.randomUUID().toString();
            FeedbackInvitation feedbackInvitation = new FeedbackInvitation();
            feedbackInvitation.setEventId(event.getId());
            feedbackInvitation.setTokenHash(token);
            feedbackInvitation.setExpiresAt(LocalDateTime.now().plusDays(7));
            feedbackInvitation.setUsedAt(null);
            feedbackInvitationRepository.save(feedbackInvitation);

            String origin = servlet.getHeader("Origin");
            String frontendUrl =
                    "http://localhost:4200".equals(origin)
                            ? "http://localhost:4200"
                            : "https://aurum-front-sigma.vercel.app";

            String link = frontendUrl + "/feedback?token=" + token;

            mailStrategy.formatAndSend(
                    "nikalongurashvili@gmail.com",
                    client.getEmail(),
                    "Feedback Request",
                    """
                            Hello, we're glad to notify you that your event has been completed,
                            hope you had an amazing time, could you please now leave a feedback of your
                            experience?\s
                           \s""" + link
            );
        }

        return new EventDTO(
                event.getPublicId(),
                client.getName(),
                event.getEventType(),
                event.getDate(),
                event.getTotalCost(),
                event.getGuestCount(),
                event.getLocation(),
                event.getStatus()
        );
    }

    public PageResponse<EventDTO> getEvents(EventQuery query) {
        return eventStrategy.search(query);
    }

    public GenericResponse deleteEvent(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Id must not be null");
        }

        int row = eventRepository.deleteEventById(id);

        if (row == 0) {
            throw new EventNotFoundException("Event not found");
        }

        return new GenericResponse(200, "Deleted successfully");
    }
}
