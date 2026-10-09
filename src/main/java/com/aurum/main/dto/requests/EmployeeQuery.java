package com.aurum.main.dto.requests;

import com.aurum.main.model.Client;
import com.aurum.main.model.Employee;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EmployeeQuery {
    private Integer page = 0;
    private Integer size = 10;
    private String search = "";
    private Employee.EmployeeRole role;
    private Employee.EmployeeType type;
    private Employee.EmployeeStatus status;
    private BigDecimal salaryFrom;
    private BigDecimal salaryTo;
    private String sortBy = "";
    private String sortDirection = "";
}
