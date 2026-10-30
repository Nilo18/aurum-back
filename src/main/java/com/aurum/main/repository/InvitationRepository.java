package com.aurum.main.repository;

import com.aurum.main.model.Invitation;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface InvitationRepository extends CrudRepository<Invitation, Long> {
    boolean existsByEmail(String email);
    Optional<Invitation> findByToken(String token);

    // Called inside registration's transaction so the token cannot be consumed twice.
    @Query("SELECT * FROM invitations WHERE token = :token FOR UPDATE")
    Optional<Invitation> findByTokenForUpdate(String token);
}
