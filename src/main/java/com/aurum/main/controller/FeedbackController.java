package com.aurum.main.controller;

import com.aurum.main.dto.FeedbackDTO;
import com.aurum.main.dto.requests.CreateFeedbackRequest;
import com.aurum.main.dto.requests.FeedbackQuery;
import com.aurum.main.dto.requests.InvitationTokenRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping(path = "/api/feedback")
public class FeedbackController {
    private final FeedbackService feedbackService;

    @PostMapping(path = "/verify-invitation")
    public ResponseEntity<GenericResponse> verifyFeedbackInvitation
            (@Valid @RequestBody InvitationTokenRequest request) {
        return ResponseEntity.ok(feedbackService.validateFeedbackInvitation(request));
    }

    @PostMapping
    public ResponseEntity<FeedbackDTO> createFeedback(@Valid @RequestBody CreateFeedbackRequest request) {
        return ResponseEntity.ok(feedbackService.createFeedback(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<FeedbackDTO>> getAllFeedback(@Valid @ModelAttribute FeedbackQuery query) {
        return ResponseEntity.ok(feedbackService.getAllFeedback(query));
    }

    @DeleteMapping(path = "/{publicId}")
    public ResponseEntity<GenericResponse> deleteFeedback(@PathVariable String publicId) {
        return ResponseEntity.ok(feedbackService.deleteFeedback(publicId));
    }
}
