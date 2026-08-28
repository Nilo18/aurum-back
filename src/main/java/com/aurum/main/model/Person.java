package com.aurum.main.model;

import com.aurum.main.model.Client.ClientType;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("person")
public class Person {
    @Id
    private Long id;
    private ClientType type = ClientType.PERSON;
    private String personalNumber;
    private String lastName;
    private String firstName;
}
