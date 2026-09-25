package com.aurum.main.controller;

import com.aurum.main.dto.EventDTO;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.requests.EventOrderRequest;
import com.aurum.main.dto.requests.EventQuery;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.EventService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping(path = "/api/event")
public class EventController {
    private final EventService eventService;

    @PostMapping(path = "/verify")
    public ResponseEntity<OtpResponse> verifyEventRequest(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(eventService.verifyCreateEventRequest(request));
    }

    @PostMapping
    public ResponseEntity<GenericResponse> createEvent(@Valid @RequestBody EventOrderRequest request) {
        return ResponseEntity.ok(eventService.createEvent(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<EventDTO>> getEvents(@ModelAttribute EventQuery query) {
        return ResponseEntity.ok(eventService.getEvents(query));
    }
}
