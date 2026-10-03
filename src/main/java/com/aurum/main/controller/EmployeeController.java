package com.aurum.main.controller;

import com.aurum.main.dto.EmployeeDTO;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.AuthResponse;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping(path = "/api/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<PageResponse<EmployeeDTO>> getEmployees(@ModelAttribute EmployeeQuery query) {
        return ResponseEntity.ok(employeeService.getEmployees(query));
    }

    @PostMapping(path = "/invite")
    public ResponseEntity<GenericResponse> inviteEmployee(
            @Valid @RequestBody InviteEmployeeRequest request) {
        return ResponseEntity.ok(employeeService.inviteEmployee(request));
    }

    @PostMapping(path = "/validate-invite")
    public ResponseEntity<GenericResponse> validateInvitation(
            @Valid @RequestBody InvitationTokenRequest request) {
        return ResponseEntity.ok(employeeService.validateInvitation(request));
    }

    @PostMapping(path = "/register")
    public ResponseEntity<AuthResponse> completeRegistration(
            @Valid @RequestBody CompleteInviteRegistrationRequest request) {
        return ResponseEntity.ok(employeeService.completeInviteRegistration(request));
    }

    @DeleteMapping
    public ResponseEntity<GenericResponse> deleteEmployee(@Valid @RequestBody EmailRequest request) {
        return ResponseEntity.ok(employeeService.deleteEmployee(request));
    }
}
