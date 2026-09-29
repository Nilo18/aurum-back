package com.aurum.main.service;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.repository.ClientRepository;
import com.aurum.main.repository.ClientRepositoryCustom;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
@Data
public class ClientService {
    private final ClientRepositoryCustom clientRepositoryCustom;

    public PageResponse<ClientDTO> getClients(ClientQuery clientQuery) {
        return clientRepositoryCustom.searchClients(clientQuery);
    }
}
