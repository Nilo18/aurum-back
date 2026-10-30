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
    private Long id;

    private Long eventId;
    private String publicId;
    private String author;
    private LocalDate commentDate;
    private Short rating;
    private String description;

}
