package com.aurum.main.repository;

import com.aurum.main.model.Client;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ClientRepository extends CrudRepository<Client, Long> {
    boolean existsByEmail(String email);
    Optional<Client> findFirstByEmailOrPhone(String email, String phone);
}
