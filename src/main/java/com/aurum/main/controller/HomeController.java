package com.aurum.main.controller;

import com.aurum.main.dto.requests.ContactRequest;
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

    @PostMapping("/contact")
    public ResponseEntity<Map<String, String>> contact(@Valid @RequestBody ContactRequest request) {
        homeService.sendContactEmail(request);
        return ResponseEntity.ok(Map.of("message", "Contact received successfully"));
    }
}
