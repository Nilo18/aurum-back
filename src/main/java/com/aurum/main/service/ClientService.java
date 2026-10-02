package com.aurum.main.service;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.requests.EmailRequest;
import com.aurum.main.dto.responses.GenericResponse;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.exception.ClientNotFoundException;
import com.aurum.main.model.Client;
import com.aurum.main.repository.ClientRepository;
import com.aurum.main.repository.ClientRepositoryCustom;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Data
public class ClientService {
    private final ClientRepositoryCustom clientRepositoryCustom;
    private final ClientRepository clientRepository;

    public PageResponse<ClientDTO> getClients(ClientQuery clientQuery) {
        return clientRepositoryCustom.searchClients(clientQuery);
    }

    @Transactional
    public GenericResponse deleteClient(EmailRequest request) {
        int rowsDeleted = clientRepository.deleteByEmail(request.email());

        if (rowsDeleted == 0) {
            throw new ClientNotFoundException("Client not found");
        }

        return new GenericResponse(200, "Client was deleted successfully");
    }
}
