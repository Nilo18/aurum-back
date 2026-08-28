package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Embedded;
import java.time.LocalDate;

@Data
@Table("feedback")
public class Feedback {
    @Id
    @Embedded.Nullable
    private Key id;
    private String author;
    private LocalDate commentDate;
    private Short rating;
    private String description;

    public record Key(Long eventId, Long id) {}
}
