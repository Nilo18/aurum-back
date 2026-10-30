package com.aurum.main.repository;

import com.aurum.main.model.Feedback;
import com.aurum.main.model.FeedbackInvitation;
import com.aurum.main.model.Invitation;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FeedbackRepository extends CrudRepository<Feedback, Long> {
    int deleteByPublicId(String publicId);
}
