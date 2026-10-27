package com.aurum.main.repository;

import com.aurum.main.model.FeedbackInvitation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackInvitationRepository extends CrudRepository<FeedbackInvitation, Long> {
    Optional<FeedbackInvitation> findByTokenHash(String tokenHash);
}
