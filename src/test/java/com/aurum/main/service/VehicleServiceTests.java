package com.aurum.main.service;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.UpdateVehicleRequest;
import com.aurum.main.dto.requests.VehicleQuery;
import com.aurum.main.exception.VehicleNotFoundException;
import com.aurum.main.model.Vehicle;
import com.aurum.main.repository.SearchStrategy;
import com.aurum.main.repository.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceTests {
    private final VehicleRepository repository = mock(VehicleRepository.class);
    @SuppressWarnings("unchecked")
    private final SearchStrategy<VehicleDTO, VehicleQuery> strategy = mock(SearchStrategy.class);
    private final VehicleService service = new VehicleService(repository, strategy);

    @Test
    void updateSavesExistingVehicleAndReturnsReloadedValues() {
        Vehicle existing = new Vehicle();
        existing.setId(42L);
        existing.setPublicId("vehicle-public-id");
        Vehicle reloaded = new Vehicle();
        reloaded.setPublicId(existing.getPublicId());
        reloaded.setType(Vehicle.VehicleType.TRUCK);
        reloaded.setPassengerCapacity(2L);
        reloaded.setCargoWeightLimit(new BigDecimal("1500.00"));
        when(repository.findByPublicId(existing.getPublicId()))
                .thenReturn(Optional.of(existing), Optional.of(reloaded));
        UpdateVehicleRequest request = new UpdateVehicleRequest(existing.getPublicId(),
                Vehicle.VehicleType.TRUCK, 2L, new BigDecimal("1500"));

        VehicleDTO response = service.updateVehicle(request);

        var order = inOrder(repository);
        order.verify(repository).findByPublicId(existing.getPublicId());
        order.verify(repository).save(existing);
        order.verify(repository).findByPublicId(existing.getPublicId());
        assertEquals(42L, existing.getId());
        assertEquals(request.type(), existing.getType());
        assertEquals(request.passengerCapacity(), existing.getPassengerCapacity());
        assertEquals(request.cargoWeightLimit(), existing.getCargoWeightLimit());
        assertEquals(new VehicleDTO(reloaded.getType(), reloaded.getPublicId(),
                reloaded.getPassengerCapacity(), reloaded.getCargoWeightLimit()), response);
    }

    @Test
    void missingVehicleIsNotSaved() {
        when(repository.findByPublicId("missing")).thenReturn(Optional.empty());
        UpdateVehicleRequest request = new UpdateVehicleRequest("missing",
                Vehicle.VehicleType.TRUCK, 2L, BigDecimal.TEN);

        assertThrows(VehicleNotFoundException.class, () -> service.updateVehicle(request));
        verify(repository, never()).save(any(Vehicle.class));
    }
}
