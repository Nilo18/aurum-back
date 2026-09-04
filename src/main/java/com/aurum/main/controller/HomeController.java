package com.aurum.main.controller;

import com.aurum.main.dto.requests.ContactRequest;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.OtpResponse;
import com.aurum.main.service.HomeService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Data
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/")
    public ResponseEntity<String> greet() {
        return ResponseEntity.ok("Hello world");
    }

   @PostMapping("/verify-contact-request")
   public ResponseEntity<OtpResponse> verify(@RequestBody EmailRequest email) {
        return ResponseEntity.ok(homeService.sendContactRequestVerification(email));
   }

    @PostMapping("/contact")
    public ResponseEntity<GenericResponse> contact(@Valid @RequestBody ContactRequest request) {
        return ResponseEntity.ok(homeService.sendContactEmail(request));
    }
}
