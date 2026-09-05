package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("client")
public class Client {
    @Id
    private Long id;
    private String name;
    private String email;
    private String phone;
    private ClientType type;

    public enum ClientType {
        PERSON,
        ORGANIZATION
    }
}
