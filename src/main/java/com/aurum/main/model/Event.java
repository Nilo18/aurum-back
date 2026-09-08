package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table("event")
public class Event {

    @Id
    private Long id;

    private Long clientId;

    private EventType eventType;
    private LocalDate date;
    private BigDecimal totalCost;
    private Integer guestCount;
    private Location location;
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

    public enum EventType {
        WEDDING,
        CORPORATE_EVENT,
        CONFERENCE,
        OFFICIAL_RECEPTION,
        ANNIVERSARY,
        BIRTHDAY,
        GALA_DINNER,
        PRODUCT_LAUNCH,
        PRIVATE_PARTY,
        OTHER
    }

    public enum Location {
        AURUM_BANQUET_HALL,
        AURUM_CONFERENCE_HALL,
        PRIVATE_RESIDENCE,
        PARTNER_VENUE,
        HOTEL,
        RESTAURANT,
        OUTDOOR_VENUE,
        HISTORICAL_VENUE,
        CORPORATE_OFFICE,
        OTHER
    }
}