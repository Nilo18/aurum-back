package com.aurum.main.service;

import com.aurum.main.dto.*;
import com.aurum.main.dto.requests.*;
import com.aurum.main.dto.responses.*;
import com.aurum.main.exception.*;
import com.aurum.main.model.*;
import com.aurum.main.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTests {

    @Mock ClientRepository repository;
    @Mock SearchStrategy<ClientDTO, ClientQuery> search;
    @Mock MaskedSearchStrategy<ClientDTO, ClientQuery> masked;
    @InjectMocks ClientService service;

    @Test void demoReceivesOnlyMaskedResults() {
        var query = new ClientQuery();
        var page = new PageResponse<ClientDTO>(List.of(), 2, 5, 0);
        when(masked.searchMasked(query)).thenReturn(page);
        assertSame(page, service.getClients(query, Employee.EmployeeRole.DEMO));
        verifyNoInteractions(search);
    }

    @Test void staffReceivesRegularResults() {
        var query = new ClientQuery();
        var page = new PageResponse<ClientDTO>(List.of(), 0, 10, 0);
        when(search.search(query)).thenReturn(page);
        assertSame(page, service.getClients(query, Employee.EmployeeRole.STAFF));
        verifyNoInteractions(masked);
    }

    @Test void deletingMissingClientFails() {
        assertThrows(ClientNotFoundException.class,
                () -> service.deleteClient(new EmailRequest("missing@example.com")));
    }

    @Test void deletingExistingClientSucceeds() {
        when(repository.deleteByEmail("client@example.com")).thenReturn(1);
        assertEquals(new GenericResponse(200, "Client was deleted successfully"),
                service.deleteClient(new EmailRequest("client@example.com")));
    }

}
