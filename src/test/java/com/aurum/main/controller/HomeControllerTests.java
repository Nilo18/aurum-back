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

class HomeControllerTests extends ControllerTestSupport {

    @Test void rootIsPublic() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk()).andExpect(content().string("Hello world"));
    }

    @Test void contactVerificationIsPublic() throws Exception {
        when(homeService.sendContactRequestVerification(any())).thenReturn(new OtpResponse("key"));
        mvc.perform(post("/verify-contact-request").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"alice@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.transactionKey").value("key"))
                .andExpect(jsonPath("$.otp").doesNotExist());
        verify(homeService).sendContactRequestVerification(new EmailRequest("alice@example.com"));
    }

    @Test void validContactIsPublic() throws Exception {
        when(homeService.sendContactEmail(any())).thenReturn(new GenericResponse(200, "Received"));
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Alice","phoneNumber":"+995555123456","emailAddress":"alice@example.com",
                 "message":"Question","transactionKey":"key","otp":"123456"}
                """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Received"));
    }

    @Test void invalidContactIsRejectedBeforeService() throws Exception {
        mvc.perform(post("/contact").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(homeService);
    }

}
