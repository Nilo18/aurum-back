package com.aurum.main.dto.requests;

import com.aurum.main.model.Employee;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record InviteEmployeeRequest(
        @NotNull(message = "Employee type is required")
        Employee.EmployeeType type,
        @NotNull(message = "Salary is required")
        @PositiveOrZero(message = "Salary must be zero or greater")
        BigDecimal salary,
        @NotBlank(message = "Email is required")
        @Email
        String email,
        @NotNull(message = "Employee role is required")
        Employee.EmployeeRole role
) {
}
