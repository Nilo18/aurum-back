package com.aurum.main.controller;

import com.aurum.main.dto.AuthenticatedUser;
import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.ClientService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@Data
@RequestMapping(path = "/api/client")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<PageResponse<ClientDTO>> getClients(
            @AuthenticationPrincipal AuthenticatedUser user,
            @ModelAttribute ClientQuery query) {
        return ResponseEntity.ok(clientService.getClients(query, user.role()));
    }

    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
    @DeleteMapping
    public ResponseEntity<GenericResponse> deleteClient(@RequestBody EmailRequest request) {
        return ResponseEntity.ok(clientService.deleteClient(request));
    }
}
