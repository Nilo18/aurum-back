package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Table("invitations")
@Data
public class Invitation {
    @Id
    private Long id;
    private String email;
    private String token;
    private Employee.EmployeeType type;
    private BigDecimal salary;
    private Employee.EmployeeRole role;
    private Instant expiresAt;
}
