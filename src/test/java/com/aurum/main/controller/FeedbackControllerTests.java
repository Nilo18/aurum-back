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

class FeedbackControllerTests extends ControllerTestSupport {

    @Test void invitationVerificationIsPublic() throws Exception {
        when(feedbackService.validateFeedbackInvitation(any())).thenReturn(new GenericResponse(200, "Valid"));
        mvc.perform(post("/api/feedback/verify-invitation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"invitation\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Valid"));
        verify(feedbackService).validateFeedbackInvitation(new InvitationTokenRequest("invitation"));
    }

    @Test void creationIsPublicAndReturnsFeedback() throws Exception {
        when(feedbackService.createFeedback(any())).thenReturn(new FeedbackDTO("feedback", "Alice", LocalDate.now(), (short) 5, "Great"));
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content("""
                {"tokenHash":"invitation","author":"Alice","rating":5,"description":"Great"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("feedback"));
        verify(feedbackService).createFeedback(new CreateFeedbackRequest("invitation", "Alice", (short) 5, "Great"));
    }

    @Test void creationRejectsOutOfRangeRatingAndMissingFields() throws Exception {
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/feedback").contentType(MediaType.APPLICATION_JSON).content("""
                {"tokenHash":"invitation","author":"Alice","rating":6,"description":"Great"}
                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.rating").exists());
        verifyNoInteractions(feedbackService);
    }

    @Test void authenticatedUserCanListFeedback() throws Exception {
        when(feedbackService.getAllFeedback(any())).thenReturn(new PageResponse<FeedbackDTO>(List.of(), 0, 5, 0));
        mvc.perform(get("/api/feedback").header("Authorization", bearer("DEMO")).param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageSize").value(5));
        verify(feedbackService).getAllFeedback(argThat(query -> query.getSize() == 5));
    }

    @Test void anonymousFeedbackListRequiresAuthentication() throws Exception {
        mvc.perform(get("/api/feedback")).andExpect(status().isUnauthorized());
        verifyNoInteractions(feedbackService);
    }

    @Test void demoCannotDeleteAndOwnerCanDelete() throws Exception {
        mvc.perform(delete("/api/feedback/feedback").header("Authorization", bearer("DEMO")))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error").exists());
        verifyNoInteractions(feedbackService);
        when(feedbackService.deleteFeedback("feedback")).thenReturn(new GenericResponse(200, "Deleted"));
        mvc.perform(delete("/api/feedback/feedback").header("Authorization", bearer("OWNER")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
    }

    @Test void invalidInvitationReturns404() throws Exception {
        when(feedbackService.validateFeedbackInvitation(any())).thenThrow(new InvitationTokenNotFoundException("Missing"));
        mvc.perform(post("/api/feedback/verify-invitation").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"missing\"}")).andExpect(status().isNotFound());
    }

}
