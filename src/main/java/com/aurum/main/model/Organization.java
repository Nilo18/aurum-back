package com.aurum.main.model;

import com.aurum.main.model.Client.ClientType;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("organization")
public class Organization {
    @Id
    private Long id;
    private ClientType type = ClientType.ORGANIZATION;
    private String organizationName;
    private String organizationType;
    private String manager;
    private String contactPerson;
}
