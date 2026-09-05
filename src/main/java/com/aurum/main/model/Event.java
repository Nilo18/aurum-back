package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("events")
public class Event {

    @Id
    private Long id;

    private Long clientId;

    private String eventType;
    private LocalDate date;
    private BigDecimal totalCost;
    private Integer guestCount;
    private String location;

    private String cuisineType;
    private String appetizer;
    private String mainCourse;
    private String dessert;
    private String drinks;

    private String notes;

    private EventStatus status = EventStatus.REQUESTED;

    public enum EventStatus {
        REQUESTED,
        PLANNING,
        CONFIRMED,
        COMPLETED,
        REJECTED,
        CANCELLED
    }
}