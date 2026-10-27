package com.aurum.main.controller;

import com.aurum.main.dto.requests.InvitationTokenRequest;
import com.aurum.main.dto.responses.GenericResponse;
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
}
