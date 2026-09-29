package com.aurum.main.controller;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.ClientService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Data
@RequestMapping(path = "/api/client")
public class ClientController {
    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<PageResponse<ClientDTO>> getClients(@ModelAttribute ClientQuery query) {
        return ResponseEntity.ok(clientService.getClients(query));
    }
}
