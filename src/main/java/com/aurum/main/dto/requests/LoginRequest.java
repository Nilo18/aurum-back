package com.aurum.main.dto.requests;

import com.aurum.main.model.Employee;

public record LoginRequest(
        String email,
        String password,
        Employee.EmployeeRole role,
        String transactionKey,
        String otp
) {
}
