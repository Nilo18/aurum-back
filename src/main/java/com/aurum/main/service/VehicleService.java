package com.aurum.main.service;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.VehicleQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.VehicleRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class VehicleService {
    private final SearchStrategy<VehicleDTO, VehicleQuery> strategy;

    public PageResponse<VehicleDTO> getVehicles(VehicleQuery query) {
        return strategy.search(query);
    }
}
