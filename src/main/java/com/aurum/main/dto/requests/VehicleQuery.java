package com.aurum.main.dto.requests;

import com.aurum.main.model.Client;
import com.aurum.main.model.Vehicle;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class VehicleQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String search = "";
    private Vehicle.VehicleType type;
    private Long passengerFrom;
    private Long passengerTo;
    private BigDecimal weightFrom;
    private BigDecimal weightTo;
    private String sortBy = "";
    private String sortDirection = "";
}
