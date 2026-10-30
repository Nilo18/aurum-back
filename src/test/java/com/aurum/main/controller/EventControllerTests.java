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

class EventControllerTests extends ControllerTestSupport {

    @Test void listBindsQueryAndReturnsPage() throws Exception {
        when(eventService.getEvents(any())).thenReturn(new PageResponse<EventDTO>(List.of(), 1, 5, 0));
        mvc.perform(get("/api/event").header("Authorization", bearer("STAFF")).param("page", "1").param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(1));
        verify(eventService).getEvents(argThat(query -> query.getPage() == 1 && query.getSize() == 5));
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/event")).andExpect(status().isUnauthorized());
        verifyNoInteractions(eventService);
    }

    @Test void verificationReturnsTransaction() throws Exception {
        when(eventService.verifyCreateEventRequest(any())).thenReturn(new OtpResponse("key", "123456"));
        mvc.perform(post("/api/event/verify").header("Authorization", bearer("STAFF"))
                .contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transactionKey").value("key"));
        verify(eventService).verifyCreateEventRequest(new EmailRequest("alice@example.com"));
    }

    @Test void creationAcceptsNestedClientAndEvent() throws Exception {
        when(eventService.createEvent(any())).thenReturn(new GenericResponse(200, "Created"));
        String date = LocalDate.now().plusDays(1).toString();
        mvc.perform(post("/api/event").header("Authorization", bearer("STAFF"))
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"client":{"clientType":"PERSON","name":"Alice","email":"alice@example.com","phone":"+995555123456"},
                 "event":{"eventType":"WEDDING","date":"%s","guestCount":10,"location":"HOTEL"},
                 "menuItemPublicIds":["dish"],"transactionKey":"key","otp":"123456"}
                """.formatted(date)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Created"));
        verify(eventService).createEvent(argThat(request -> request.client().name().equals("Alice")
                && request.event().guestCount() == 10 && request.menuItemPublicIds().equals(List.of("dish"))));
    }

    @Test void invalidCreateAndUpdateNeverReachService() throws Exception {
        String auth = bearer("OWNER");
        mvc.perform(post("/api/event").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/event").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(eventService);
    }

    @Test void ownerCanUpdateAndStaffCannot() throws Exception {
        String payload = """
                {"publicId":"event","eventType":"WEDDING","date":"%s","totalCost":10,
                 "guestCount":10,"location":"HOTEL","status":"CONFIRMED"}
                """.formatted(LocalDate.now());
        mvc.perform(put("/api/event").header("Authorization", bearer("STAFF"))
                .contentType(MediaType.APPLICATION_JSON).content(payload)).andExpect(status().isForbidden());
        verifyNoInteractions(eventService);
        when(eventService.updateEvent(any())).thenReturn(new EventDTO("event", "Alice", Event.EventType.WEDDING,
                LocalDate.now(), BigDecimal.TEN, 10, Event.Location.HOTEL, Event.EventStatus.CONFIRMED));
        mvc.perform(put("/api/event").header("Authorization", bearer("OWNER"))
                .contentType(MediaType.APPLICATION_JSON).content(payload))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test void deletionBindsRouteIdAndDelegatesToService() throws Exception {
        when(eventService.deleteEvent(42L)).thenReturn(new GenericResponse(200, "Deleted"));
        mvc.perform(delete("/api/event/42").header("Authorization", bearer("OWNER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        verify(eventService).deleteEvent(42L);
    }

    @Test void staffCannotDeleteEvent() throws Exception {
        mvc.perform(delete("/api/event/42").header("Authorization", bearer("STAFF")))
                .andExpect(status().isForbidden());
        verifyNoInteractions(eventService);
    }

}
