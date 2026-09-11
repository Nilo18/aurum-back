package com.aurum.main.repository;

import com.aurum.main.model.Invitation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface InvitationRepository extends CrudRepository<Invitation, Long> {
    boolean existsByEmail(String email);
    Optional<Invitation> findByToken(String token);
}
