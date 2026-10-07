package com.aurum.main.controller;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.VehicleQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.VehicleService;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/vehicle")
@Data
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<PageResponse<VehicleDTO>> getVehicles(@ModelAttribute VehicleQuery query) {
        return ResponseEntity.ok(vehicleService.getVehicles(query));
    }
}
