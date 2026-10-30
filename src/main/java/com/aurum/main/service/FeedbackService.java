package com.aurum.main.service;

import com.aurum.main.dto.FeedbackDTO;
import com.aurum.main.dto.requests.CreateFeedbackRequest;
import com.aurum.main.dto.requests.FeedbackQuery;
import com.aurum.main.dto.requests.InvitationTokenRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.*;
import com.aurum.main.model.Feedback;
import com.aurum.main.model.FeedbackInvitation;
import com.aurum.main.model.Invitation;
import com.aurum.main.repository.FeedbackInvitationRepository;
import com.aurum.main.repository.FeedbackRepository;
import com.aurum.main.repository.SearchStrategy;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Data
public class FeedbackService {
    private final FeedbackInvitationRepository feedbackInvitationRepository;
    private final FeedbackRepository feedbackRepository;
    private final SearchStrategy<FeedbackDTO, FeedbackQuery> searchStrategy;

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

    public FeedbackDTO createFeedback(CreateFeedbackRequest request) {
        FeedbackInvitation feedbackInvitation = feedbackInvitationRepository
                .findByTokenHash(request.tokenHash())
                .orElseThrow(() -> new InvitationTokenNotFoundException("Feedback invitation not found"));

        if (feedbackInvitation.getUsedAt() != null) {
            throw new FeedbackInvitationAlreadyUsedException(
                    "Feedback invitation has already been used"
            );
        }

        if (feedbackInvitation.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new FeedbackInvitationExpiredException(
                    "Feedback invitation has expired"
            );
        }

        Feedback feedback = new Feedback();
        feedback.setEventId(feedbackInvitation.getEventId());
        String publicId = UUID.randomUUID().toString();
        feedback.setPublicId(publicId);
        feedback.setAuthor(request.author());
        feedback.setCommentDate(LocalDate.now());
        feedback.setRating(request.rating());
        feedback.setDescription(request.description());
        feedback = feedbackRepository.save(feedback);

        feedbackInvitation.setUsedAt(LocalDateTime.now());
        feedbackInvitationRepository.save(feedbackInvitation);

        return new FeedbackDTO(
                feedback.getPublicId(),
                feedback.getAuthor(),
                feedback.getCommentDate(),
                feedback.getRating(),
                feedback.getDescription()
        );
    }

    public PageResponse<FeedbackDTO> getAllFeedback(FeedbackQuery query) {
        return searchStrategy.search(query);
    }
}
