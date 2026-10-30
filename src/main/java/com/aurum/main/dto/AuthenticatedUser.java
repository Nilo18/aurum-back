package com.aurum.main.dto;

import com.aurum.main.model.Employee;

public record AuthenticatedUser(
        Long userId,
        Employee.EmployeeRole role
) {
}
