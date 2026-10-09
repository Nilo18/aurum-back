package com.aurum.main.controller;

import com.aurum.main.dto.VehicleDTO;
import com.aurum.main.dto.requests.CreateVehicleRequest;
import com.aurum.main.dto.requests.UpdateVehicleRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.exception.GlobalExceptionHandler;
import com.aurum.main.model.Vehicle;
import com.aurum.main.service.VehicleService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleControllerTests {
    private final VehicleService service = mock(VehicleService.class);
    private final MockMvc mvc = MockMvcBuilders.standaloneSetup(new VehicleController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();

    @Test
    void createDoesNotRequirePublicId() throws Exception {
//        when(service.addVehicle(any(CreateVehicleRequest.class)))
//                .thenReturn(new GenericResponse(200, "Vehicle created successfully"));

        mvc.perform(post("/api/vehicle").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"TRUCK","passengerCapacity":2,"cargoWeightLimit":1500}
                                """))
                .andExpect(status().isOk());
        verify(service).addVehicle(any(CreateVehicleRequest.class));
    }

    @Test
    void updateRejectsMissingAndBlankPublicId() throws Exception {
        for (String idField : new String[]{"", "\"publicId\":null,", "\"publicId\":\"   \","}) {
            mvc.perform(put("/api/vehicle").contentType(MediaType.APPLICATION_JSON)
                            .content("{" + idField
                                    + "\"type\":\"TRUCK\",\"passengerCapacity\":2,\"cargoWeightLimit\":1500}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.publicId").value("Vehicle public ID is required"));
        }
        verifyNoInteractions(service);
    }

    @Test
    void updateReturnsVehicleResponse() throws Exception {
        when(service.updateVehicle(any(UpdateVehicleRequest.class)))
                .thenReturn(new VehicleDTO(Vehicle.VehicleType.TRUCK, "vehicle-public-id",
                        2L, new BigDecimal("1500")));

        mvc.perform(put("/api/vehicle").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"publicId":"vehicle-public-id","type":"TRUCK",
                                 "passengerCapacity":2,"cargoWeightLimit":1500}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicId").value("vehicle-public-id"))
                .andExpect(jsonPath("$.type").value("TRUCK"))
                .andExpect(jsonPath("$.passengerCapacity").value(2))
                .andExpect(jsonPath("$.cargoWeightLimit").value(1500));
    }
}
