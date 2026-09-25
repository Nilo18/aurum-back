package com.aurum.main.repository;

import com.aurum.main.dto.EventDTO;
import com.aurum.main.dto.requests.EventQuery;
import com.aurum.main.dto.responses.PageResponse;
import com.aurum.main.model.Event;

public interface EventRepositoryCustom {
    PageResponse<EventDTO> searchEvents(EventQuery query);
}
