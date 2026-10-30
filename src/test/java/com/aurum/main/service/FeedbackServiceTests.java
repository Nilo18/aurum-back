package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceTests {

    @Mock FeedbackInvitationRepository invitations;
    @Mock FeedbackRepository feedback;
    @Mock SearchStrategy<FeedbackDTO, FeedbackQuery> search;
    @InjectMocks FeedbackService service;
    private final CreateFeedbackRequest request = new CreateFeedbackRequest("token", "Alice", (short) 5, "Great");

    private FeedbackInvitation invitation() {
        var invitation = new FeedbackInvitation();
        invitation.setEventId(9L);
        invitation.setExpiresAt(LocalDateTime.now().plusDays(1));
        when(invitations.findByTokenHash("token")).thenReturn(Optional.of(invitation));
        return invitation;
    }

    @Test void missingInvitationIsRejectedForValidationAndCreation() {
        assertThrows(InvitationTokenNotFoundException.class,
                () -> service.validateFeedbackInvitation(new InvitationTokenRequest("token")));
        assertThrows(InvitationTokenNotFoundException.class, () -> service.createFeedback(request));
        verifyNoInteractions(feedback);
    }

    @Test void expiredInvitationIsRejected() {
        invitation().setExpiresAt(LocalDateTime.now().minusDays(1));
        assertThrows(InvitationTokenExpiredException.class,
                () -> service.validateFeedbackInvitation(new InvitationTokenRequest("token")));
        assertThrows(FeedbackInvitationExpiredException.class, () -> service.createFeedback(request));
        verifyNoInteractions(feedback);
    }

    @Test void usedInvitationCannotBeReused() {
        invitation().setUsedAt(LocalDateTime.now().minusHours(1));
        assertThrows(BadInvitationRequestException.class,
                () -> service.validateFeedbackInvitation(new InvitationTokenRequest("token")));
        assertThrows(FeedbackInvitationAlreadyUsedException.class, () -> service.createFeedback(request));
        verifyNoInteractions(feedback);
    }

    @Test void validInvitationCanBeValidatedWithoutConsumingIt() {
        var invitation = invitation();
        assertEquals(new GenericResponse(200, "Feedback invitation validated successfully!"),
                service.validateFeedbackInvitation(new InvitationTokenRequest("token")));
        assertNull(invitation.getUsedAt());
        verify(invitations, never()).save(any());
    }

    @Test void creationPersistsFeedbackAndMarksInvitationUsed() {
        var invitation = invitation();
        when(feedback.save(any(Feedback.class))).thenAnswer(call -> call.getArgument(0));
        var result = service.createFeedback(request);
        assertEquals("Alice", result.author());
        assertEquals((short) 5, result.rating());
        assertEquals("Great", result.description());
        assertEquals(LocalDate.now(), result.commentDate());
        assertDoesNotThrow(() -> UUID.fromString(result.publicId()));
        verify(feedback).save(argThat(row -> row.getEventId().equals(9L)));
        assertNotNull(invitation.getUsedAt());
        verify(invitations).save(invitation);
    }

    @Test void searchReturnsRequestedPage() {
        var query = new FeedbackQuery();
        var page = new PageResponse<FeedbackDTO>(List.of(), 1, 5, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getAllFeedback(query));
    }

    @Test void deleteDistinguishesMissingAndExistingFeedback() {
        assertThrows(FeedbackNotFoundException.class, () -> service.deleteFeedback("missing"));
        when(feedback.deleteByPublicId("existing")).thenReturn(1);
        assertEquals(new GenericResponse(200, "Feedback deleted successfully"), service.deleteFeedback("existing"));
    }

}
