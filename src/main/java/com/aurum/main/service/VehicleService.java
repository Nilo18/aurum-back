package com.aurum.main.service;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.VehicleQuery;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.VehicleNotFoundException;
import com.aurum.main.model.Vehicle;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.VehicleRepository;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Data
@Log
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final SearchStrategy<VehicleDTO, VehicleQuery> strategy;

    public PageResponse<VehicleDTO> getVehicles(VehicleQuery query) {
        return strategy.search(query);
    }

    public GenericResponse addVehicle(VehicleDTO data) {
        Vehicle vehicle = new Vehicle();
        vehicle.setType(data.type());
        vehicle.setPublicId(UUID.randomUUID().toString());
        vehicle.setPassengerCapacity(data.passengerCapacity());
        vehicle.setCargoWeightLimit(data.cargoWeightLimit());
        vehicleRepository.save(vehicle);
        return new GenericResponse(200, "Vehicle created successfully");
    }

    public GenericResponse deleteVehicle(String publicId) {
        int deleted = vehicleRepository.deleteByPublicId(publicId);

        if (deleted == 0) {
            throw new VehicleNotFoundException("Vehicle not found");
        }

        return new GenericResponse(200, "Vehicle deleted successfully");
    }
}