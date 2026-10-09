package com.aurum.main.dto.requests;

import com.aurum.main.model.Event;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EventQuery {
    private Integer page = 0;
    private Integer size = 10;
    private Event.EventType eventType;
    private BigDecimal costFrom;
    private BigDecimal costTo;
    private Integer guestFrom;
    private Integer guestTo;
    private LocalDate eventFrom;
    private LocalDate eventTo;
    private Event.Location location;
    private Event.EventStatus status;
    private String sortBy = "";
    private String sortDirection = "";
    private String search = "";
}
