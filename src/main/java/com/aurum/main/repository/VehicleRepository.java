package com.aurum.main.repository;

import com.aurum.main.model.Vehicle;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface VehicleRepository extends CrudRepository<Vehicle, Long> {
    int deleteByPublicId(String publicId);
    Optional<Vehicle> findByPublicId(String publicId);
}
