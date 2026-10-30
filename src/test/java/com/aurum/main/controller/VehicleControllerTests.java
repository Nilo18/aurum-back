package com.aurum.main.controller;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VehicleControllerTests extends ControllerTestSupport {

    @Test void listBindsPaginationAndReturnsPage() throws Exception {
        when(vehicleService.getVehicles(any(VehicleQuery.class))).thenReturn(new PageResponse<VehicleDTO>(List.of(), 2, 5, 0));
        mvc.perform(get("/api/vehicle").header("Authorization", bearer("STAFF"))
                        .param("page", "2").param("size", "5"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
        verify(vehicleService).getVehicles(argThat((VehicleQuery q) -> q.getPage() == 2 && q.getSize() == 5));
    }

    @Test void ownerCanCreateAndReceivesResponse() throws Exception {
        when(vehicleService.addVehicle(any(CreateVehicleRequest.class))).thenReturn(new VehicleDTO(Vehicle.VehicleType.TRUCK, "item", 2L, new BigDecimal("1500")));
        mvc.perform(post("/api/vehicle").header("Authorization", bearer("OWNER"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"type":"TRUCK","passengerCapacity":2,"cargoWeightLimit":1500}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
        verify(vehicleService).addVehicle(any(CreateVehicleRequest.class));
    }

    @Test void adminCanUpdate() throws Exception {
        when(vehicleService.updateVehicle(any(UpdateVehicleRequest.class))).thenReturn(new VehicleDTO(Vehicle.VehicleType.TRUCK, "item", 2L, new BigDecimal("1500")));
        mvc.perform(put("/api/vehicle").header("Authorization", bearer("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content("""
                        {"publicId":"item","type":"TRUCK","passengerCapacity":2,"cargoWeightLimit":1500}
                        """))
                .andExpect(status().isOk()).andExpect(jsonPath("$.publicId").value("item"));
    }

    @Test void invalidCreateAndUpdateAreRejectedBeforeService() throws Exception {
        String auth = bearer("OWNER");
        mvc.perform(post("/api/vehicle").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        mvc.perform(put("/api/vehicle").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(vehicleService);
    }

    @Test void staffCannotMutateResources() throws Exception {
        String auth = bearer("STAFF");
        mvc.perform(post("/api/vehicle").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"type":"TRUCK","passengerCapacity":2,"cargoWeightLimit":1500}
                """)).andExpect(status().isForbidden());
        mvc.perform(put("/api/vehicle").header("Authorization", auth)
                .contentType(MediaType.APPLICATION_JSON).content("""
                {"publicId":"item","type":"TRUCK","passengerCapacity":2,"cargoWeightLimit":1500}
                """)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/vehicle/item").header("Authorization", auth)).andExpect(status().isForbidden());
        verifyNoInteractions(vehicleService);
    }

    @Test void deletionReturnsSuccessAndMissingResourceReturns404() throws Exception {
        when(vehicleService.deleteVehicle("item")).thenReturn(new GenericResponse(200, "Deleted"));
        when(vehicleService.deleteVehicle("missing")).thenThrow(new VehicleNotFoundException("Missing"));
        String auth = bearer("OWNER");
        mvc.perform(delete("/api/vehicle/item").header("Authorization", auth))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").value("Deleted"));
        mvc.perform(delete("/api/vehicle/missing").header("Authorization", auth)).andExpect(status().isNotFound());
    }

    @Test void anonymousListIsUnauthorized() throws Exception {
        mvc.perform(get("/api/vehicle")).andExpect(status().isUnauthorized());
        verifyNoInteractions(vehicleService);
    }

}
