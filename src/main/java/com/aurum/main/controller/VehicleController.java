package com.aurum.main.controller;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.VehicleQuery;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.service.VehicleService;
import jakarta.validation.Valid;
import lombok.Data;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/vehicle")
@Data
public class VehicleController {
    private final VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<PageResponse<VehicleDTO>> getVehicles(@ModelAttribute VehicleQuery query) {
        return ResponseEntity.ok(vehicleService.getVehicles(query));
    }

    @PostMapping
    public ResponseEntity<GenericResponse> createVehicle(@Valid @RequestBody VehicleDTO body) {
        return ResponseEntity.ok(vehicleService.addVehicle(body));
    }

    @DeleteMapping(path = "/{publicId}")
    public ResponseEntity<GenericResponse> deleteVehicle(@PathVariable String publicId) {
        return ResponseEntity.ok(vehicleService.deleteVehicle(publicId));
    }
}
