package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Table("vehicle")
public class Vehicle {
    @Id
    private Long id;
    private String publicId;
    private VehicleType type;
    private Long passengerCapacity;
    private BigDecimal cargoWeightLimit;

    public enum VehicleType {
        TRUCK,
        PASSENGER_VEHICLE
    }
}
