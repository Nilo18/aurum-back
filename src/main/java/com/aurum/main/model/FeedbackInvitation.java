package com.aurum.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("feedback_invitation")
public class FeedbackInvitation {

    @Id
    private Long id;

    private Long eventId;

    private String tokenHash;

    private LocalDateTime expiresAt;

    private LocalDateTime usedAt;
}