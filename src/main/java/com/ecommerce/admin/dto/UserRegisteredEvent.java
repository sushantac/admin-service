package com.ecommerce.admin.dto;

import java.time.Instant;

public record UserRegisteredEvent(
        String eventId,
        Long userId,
        String email,
        String fullName,
        Instant occurredAt
) {}
