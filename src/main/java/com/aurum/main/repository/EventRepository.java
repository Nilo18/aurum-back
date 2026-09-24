package com.aurum.main.repository;

import com.aurum.main.dto.DashboardEventDTO;
import com.aurum.main.model.Event;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.math.BigDecimal;
import java.util.List;

public interface EventRepository extends CrudRepository<Event, Long> {
    @Query("SELECT COUNT(*) FROM event WHERE status IN ('REQUESTED', 'PLANNING')")
    long countByStatusInReview();
    @Query("""
    SELECT e.event_type, e.date, e.guest_count, c.name AS client_name
    FROM event e
    JOIN client c ON c.id = e.client_id
    WHERE e.status IN ('REQUESTED', 'PLANNING')
    """)
    List<DashboardEventDTO> findByStatusInReview();
    @Query("""
    SELECT SUM(e.total_cost) FROM event e WHERE e.status NOT IN ('CANCELLED', 'REJECTED')""")
    BigDecimal sumTotalValue();
}
