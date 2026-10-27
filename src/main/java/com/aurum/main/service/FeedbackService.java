package com.aurum.main.service;

import com.aurum.main.dto.requests.InvitationTokenRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.exception.BadInvitationRequestException;
import com.aurum.main.exception.InvitationTokenExpiredException;
import com.aurum.main.exception.InvitationTokenNotFoundException;
import com.aurum.main.model.FeedbackInvitation;
import com.aurum.main.model.Invitation;
import com.aurum.main.repository.FeedbackInvitationRepository;
import com.aurum.main.repository.FeedbackRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;

@Service
@Data
public class FeedbackService {
    private final FeedbackInvitationRepository feedbackInvitationRepository;

    public GenericResponse validateFeedbackInvitation(InvitationTokenRequest request) {
        FeedbackInvitation suggestedInvitation = feedbackInvitationRepository.findByTokenHash(request.token())
                .orElseThrow(() -> new InvitationTokenNotFoundException("Feedback invitation token not found"));

        if (!suggestedInvitation.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvitationTokenExpiredException("Feedback invitation expired");
        }

        if (suggestedInvitation.getUsedAt() != null) {
            throw new BadInvitationRequestException("Feedback invitation has already been used");
        }

        return new GenericResponse(200, "Feedback invitation validated successfully!");
    }
}
