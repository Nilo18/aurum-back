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
    void updateSavesExistingVehicleAndReturnsUpdatedValues() {
        Vehicle existing = new Vehicle();
        existing.setId(42L);
        existing.setPublicId("vehicle-public-id");
        when(repository.findByPublicId(existing.getPublicId()))
                .thenReturn(Optional.of(existing));
        UpdateVehicleRequest request = new UpdateVehicleRequest(existing.getPublicId(),
                Vehicle.VehicleType.TRUCK, 2L, new BigDecimal("1500"));

        VehicleDTO response = service.updateVehicle(request);

        var order = inOrder(repository);
        order.verify(repository).findByPublicId(existing.getPublicId());
        order.verify(repository).save(existing);
        assertEquals(42L, existing.getId());
        assertEquals(request.type(), existing.getType());
        assertEquals(request.passengerCapacity(), existing.getPassengerCapacity());
        assertEquals(request.cargoWeightLimit(), existing.getCargoWeightLimit());
        assertEquals(new VehicleDTO(request.type(), request.publicId(),
                request.passengerCapacity(), request.cargoWeightLimit()), response);
    }

    @Test
    void missingVehicleIsNotSaved() {
        when(repository.findByPublicId("missing")).thenReturn(Optional.empty());
        UpdateVehicleRequest request = new UpdateVehicleRequest("missing",
                Vehicle.VehicleType.TRUCK, 2L, BigDecimal.TEN);

        assertThrows(VehicleNotFoundException.class, () -> service.updateVehicle(request));
        verify(repository, never()).save(any(Vehicle.class));
    }

    @Test void creationGeneratesPublicIdAndPersistsVehicleDetails() {
        var request = new com.aurum.main.dto.requests.CreateVehicleRequest(Vehicle.VehicleType.TRUCK, 2L, BigDecimal.TEN);
        var result = service.addVehicle(request);
        assertDoesNotThrow(() -> java.util.UUID.fromString(result.publicId()));
        assertEquals(new VehicleDTO(request.type(), result.publicId(), request.passengerCapacity(), request.cargoWeightLimit()), result);
        verify(repository).save(argThat(vehicle -> vehicle.getPublicId().equals(result.publicId())
                && vehicle.getType() == request.type() && vehicle.getCargoWeightLimit().equals(BigDecimal.TEN)));
    }

    @Test void searchReturnsRepositoryPage() {
        var query = new VehicleQuery();
        var page = new com.aurum.main.dto.responses.PageResponse<VehicleDTO>(java.util.List.of(), 0, 10, 0);
        when(strategy.search(query)).thenReturn(page);
        assertSame(page, service.getVehicles(query));
    }

    @Test void deletionDistinguishesMissingAndExistingVehicle() {
        assertThrows(VehicleNotFoundException.class, () -> service.deleteVehicle("missing"));
        when(repository.deleteByPublicId("existing")).thenReturn(1);
        assertEquals(new com.aurum.main.dto.responses.GenericResponse(200, "Vehicle deleted successfully"),
                service.deleteVehicle("existing"));
    }

}
