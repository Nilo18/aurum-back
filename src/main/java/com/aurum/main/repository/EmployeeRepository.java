package com.aurum.main.repository;

import com.aurum.main.model.Employee;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface EmployeeRepository extends CrudRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    int deleteByEmail(String email);
    @Modifying
    @Query("""
            DELETE FROM employees
            WHERE email = :email AND role <> 'OWNER';
    """)
    int deleteNonOwner(String email);
}
