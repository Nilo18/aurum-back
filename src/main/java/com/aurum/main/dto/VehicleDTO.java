package com.aurum.main.dto;

import com.aurum.main.model.Vehicle;

import java.math.BigDecimal;

public record VehicleDTO(
        Vehicle.VehicleType type,
        String publicId,

        Long passengerCapacity,

        BigDecimal cargoWeightLimit
) {
}
