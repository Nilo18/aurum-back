package com.aurum.main.controller;

import com.aurum.main.dto.AuthenticatedUser;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<PageResponse<FeedbackDTO>> getAllFeedback(
//            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute FeedbackQuery query) {
        return ResponseEntity.ok(feedbackService.getAllFeedback(query));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @DeleteMapping(path = "/{publicId}")
    public ResponseEntity<GenericResponse> deleteFeedback(@PathVariable String publicId) {
        return ResponseEntity.ok(feedbackService.deleteFeedback(publicId));
    }
}
