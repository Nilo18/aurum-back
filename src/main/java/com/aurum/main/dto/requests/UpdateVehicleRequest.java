package com.aurum.main.dto.requests;

import com.aurum.main.model.Vehicle;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateVehicleRequest(
        @NotBlank(message = "Vehicle public ID is required")
        String publicId,

        @NotNull(message = "Vehicle type is required")
        Vehicle.VehicleType type,

        @NotNull(message = "Passenger capacity is required")
        @Min(value = 0, message = "Passenger capacity cannot be negative")
        @Max(value = 100000, message = "Passenger capacity must not exceed 100000")
        Long passengerCapacity,

        @NotNull(message = "Cargo weight limit is required")
        @Min(value = 0, message = "Cargo weight limit cannot be negative")
        @DecimalMax(value = "100000", message = "Cargo weight must not exceed 100000")
        BigDecimal cargoWeightLimit
) {
}
