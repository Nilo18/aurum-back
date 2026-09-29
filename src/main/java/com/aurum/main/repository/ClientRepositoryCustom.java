package com.aurum.main.repository;

import com.aurum.main.dto.ClientDTO;
import com.aurum.main.dto.requests.ClientQuery;
import com.aurum.main.dto.responses.PageResponse;

public interface ClientRepositoryCustom {
    PageResponse<ClientDTO> searchClients(ClientQuery query);
}
