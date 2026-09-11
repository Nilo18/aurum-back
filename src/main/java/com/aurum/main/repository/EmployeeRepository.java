package com.aurum.main.repository;

import com.aurum.main.model.Employee;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
}
