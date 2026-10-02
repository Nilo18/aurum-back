package com.aurum.main.dto;

import com.aurum.main.model.Employee;

import java.math.BigDecimal;

public record EmployeeDTO(
        Employee.EmployeeType specialty,
        String name,
        BigDecimal salary,
        String Email,
        Employee.EmployeeRole role,
        Employee.EmployeeStatus status
) {
}
