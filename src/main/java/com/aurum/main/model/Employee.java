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
    private String firstName;
    private String lastName;
    private EmployeeType type;
    private BigDecimal salary;

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
}
