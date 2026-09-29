package com.aurum.main.dto.requests;

import com.aurum.main.model.Client;
import lombok.Data;

@Data
public class ClientQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String search = "";
    private Client.ClientType type;
    private String sortBy = "";
    private String sortDirection = "";
}
