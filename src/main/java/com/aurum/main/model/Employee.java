package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table("employees")
@Data
public class Employee {
    @Id
    private Long id;
    private String name;
    private EmployeeType type;
    private BigDecimal salary;
    private String email;
    private String password;
    private EmployeeRole role;

    public enum EmployeeType {
        SALES_MANAGER,
        CLIENT_MANAGER,
        PURCHASING_MANAGER,
        MUSICIAN,
        ACTOR,
        DRIVER,
        HOST,
        CHEF
    }

    public enum EmployeeRole {
        OWNER,
        ADMIN,
        STAFF,
        DEMO
    }
}
