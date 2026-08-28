package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Embedded;

@Data
@Table("employee_event")
public class EmployeeEvent {
    @Id
    @Embedded.Nullable
    private Key id;
    private String role;

    public record Key(Long employeeId, Long eventId) {}
}
