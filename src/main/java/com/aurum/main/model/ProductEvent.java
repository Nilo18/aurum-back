package com.aurum.main.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Embedded;

@Data
@Table("product_event")
public class ProductEvent {
    @Id
    @Embedded.Nullable
    private Key id;

    public record Key(Long productId, Long eventId) {}
}
