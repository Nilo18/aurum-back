package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import com.aurum.main.utils.OtpPurpose;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTests {

    @Mock ClientRepository clients;
    @Mock EventRepository events;
    @Mock SearchStrategy<EventDTO, EventQuery> search;
    @Mock MenuItemRepository menu;
    @Mock MailService mail;
    @Mock OtpService otp;
    @Mock FeedbackInvitationRepository invitations;
    @InjectMocks EventService service;

    private EventOrderRequest order() {
        return new EventOrderRequest(new ClientDTO(Client.ClientType.PERSON, "Alice", "alice@example.com", "+995555123456"),
                new EventCreationRequest(Event.EventType.WEDDING, LocalDate.now().plusDays(1), null,
                        10, Event.Location.HOTEL, "Notes"), List.of("dish"), "transaction", "123456");
    }

    private UpdateEventRequest update(Event.EventStatus status) {
        return new UpdateEventRequest("event", Event.EventType.WEDDING, LocalDate.now(),
                BigDecimal.TEN, 10, Event.Location.HOTEL, status);
    }

    @Test void verificationEmailsGeneratedCode() {
        var response = new OtpService.GeneratedOtp("transaction", "123456");
        when(otp.generateOtp("alice@example.com", OtpPurpose.CREATE_EVENT)).thenReturn(response);
        assertEquals(new OtpResponse(response.transactionKey()), service.verifyCreateEventRequest(new EmailRequest("alice@example.com")));
        verify(mail).formatAndSend(anyString(), eq("alice@example.com"), anyString(), contains("123456"));
    }

    @Test void invalidOtpPreventsAllPersistence() {
        assertThrows(InvalidOtpException.class, () -> service.createEvent(order()));
        verifyNoInteractions(clients, events, menu);
    }

    @Test void creationComputesCostFromMenuAndGuestCountAndCreatesClient() {
        var request = order();
        when(otp.validateOtp("transaction", "alice@example.com", OtpPurpose.CREATE_EVENT, "123456")).thenReturn(true);
        when(clients.save(any(Client.class))).thenAnswer(call -> {
            Client client = call.getArgument(0); client.setId(42L); return client;
        });
        var dish = new MenuItem(); dish.setPricePerPerson(new BigDecimal("12.50"));
        var drink = new MenuItem(); drink.setPricePerPerson(new BigDecimal("2.50"));
        when(menu.findAllByPublicIdIn(request.menuItemPublicIds())).thenReturn(List.of(dish, drink));
        assertEquals(new GenericResponse(200, "Event added successfully!"), service.createEvent(request));
        verify(events).save(argThat(event -> event.getClientId().equals(42L)
                && event.getTotalCost().compareTo(new BigDecimal("150")) == 0
                && event.getGuestCount() == 10 && event.getStatus() == Event.EventStatus.REQUESTED));
        verify(clients).save(argThat(client -> client.getEmail().equals(request.client().email())));
    }

    @Test void existingClientIsReused() {
        var request = order();
        var client = new Client(); client.setId(42L);
        when(otp.validateOtp("transaction", "alice@example.com", OtpPurpose.CREATE_EVENT, "123456")).thenReturn(true);
        when(clients.findFirstByEmailOrPhone(request.client().email(), request.client().phone())).thenReturn(Optional.of(client));
        when(clients.save(client)).thenReturn(client);
        when(menu.findAllByPublicIdIn(request.menuItemPublicIds())).thenReturn(List.of());
        service.createEvent(request);
        verify(clients).save(same(client));
        verify(events).save(argThat(event -> event.getTotalCost().equals(BigDecimal.ZERO)));
    }

    @Test void missingEventCannotBeUpdated() {
        assertThrows(EventNotFoundException.class, () -> service.updateEvent(update(Event.EventStatus.CONFIRMED)));
        verify(events, never()).save(any());
    }

    @Test void terminalEventsCannotBeModified() {
        for (var status : List.of(Event.EventStatus.COMPLETED, Event.EventStatus.CANCELLED, Event.EventStatus.REJECTED)) {
            var event = new Event(); event.setStatus(status);
            when(events.findByPublicId("event")).thenReturn(Optional.of(event));
            assertThrows(EventCannotBeUpdatedException.class, () -> service.updateEvent(update(Event.EventStatus.CONFIRMED)));
        }
        verify(events, never()).save(any());
    }

    @Test void futureEventCannotBeCompleted() {
        var event = new Event(); event.setDate(LocalDate.now().plusDays(1));
        when(events.findByPublicId("event")).thenReturn(Optional.of(event));
        assertThrows(EventCannotBeUpdatedException.class, () -> service.updateEvent(update(Event.EventStatus.COMPLETED)));
        verify(events, never()).save(any());
    }

    @Test void completingEventSendsExpiringFeedbackInvitation() {
        var event = new Event(); event.setId(5L); event.setClientId(42L); event.setDate(LocalDate.now().minusDays(1));
        var client = new Client(); client.setName("Alice"); client.setEmail("alice@example.com");
        when(events.findByPublicId("event")).thenReturn(Optional.of(event));
        when(events.save(event)).thenReturn(event);
        when(clients.findById(42L)).thenReturn(Optional.of(client));
        var result = service.updateEvent(update(Event.EventStatus.COMPLETED));
        assertEquals(Event.EventStatus.COMPLETED, result.status());
        assertEquals("Alice", result.clientName());
        verify(invitations).save(argThat(invitation -> invitation.getEventId().equals(5L)
                && invitation.getUsedAt() == null && invitation.getExpiresAt().isAfter(LocalDateTime.now().plusDays(6))));
        verify(mail).formatAndSend(anyString(), eq(client.getEmail()), eq("Feedback Request"), contains("feedback?token="));
    }

    @Test void ordinaryUpdateDoesNotSendFeedbackInvitation() {
        var event = new Event(); event.setClientId(42L);
        when(events.findByPublicId("event")).thenReturn(Optional.of(event));
        when(events.save(event)).thenReturn(event);
        when(clients.findById(42L)).thenReturn(Optional.of(new Client()));
        assertEquals(Event.EventStatus.CONFIRMED, service.updateEvent(update(Event.EventStatus.CONFIRMED)).status());
        verifyNoInteractions(invitations, mail);
    }

    @Test void searchReturnsRequestedPage() {
        var query = new EventQuery();
        var page = new PageResponse<EventDTO>(List.of(), 0, 10, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getEvents(query));
    }

    @Test void deletionRejectsNullOrMissingIdAndDeletesExistingEvent() {
        assertThrows(IllegalArgumentException.class, () -> service.deleteEvent(null));
        verifyNoInteractions(events);
        assertThrows(EventNotFoundException.class, () -> service.deleteEvent(42L));
        when(events.deleteEventById(42L)).thenReturn(1);
        assertEquals(new GenericResponse(200, "Deleted successfully"), service.deleteEvent(42L));
    }

}
