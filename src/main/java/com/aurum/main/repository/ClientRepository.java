package com.aurum.main.repository;

import com.aurum.main.model.Client;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Long> {
    boolean existsByEmail(String email);
    Optional<Client> findFirstByEmailOrPhone(String email, String phone);
    Optional<Client> findByEmail(String email);
    int deleteByEmail(String email);
    @Query("SELECT name FROM client WHERE id = :id")
    String findNameById(@Param("id") Long id);
}
