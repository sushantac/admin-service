package com.ecommerce.admin.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "idempotency_keys")
public class IdempotencyKey {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Column(name = "event_type", nullable = false, length = 64)
    private String eventType;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected IdempotencyKey() {}

    public IdempotencyKey(String eventId, String eventType, Instant createdAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.createdAt = createdAt;
    }

    public String getEventId() { return eventId; }
    public String getEventType() { return eventType; }
    public Instant getCreatedAt() { return createdAt; }
}
