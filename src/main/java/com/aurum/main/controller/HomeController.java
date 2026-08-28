package com.aurum.main.controller;

import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Data
public class HomeController {
    @GetMapping("/")
    public ResponseEntity<String> greet() {
        return ResponseEntity.ok("Hello world");
    }
}
