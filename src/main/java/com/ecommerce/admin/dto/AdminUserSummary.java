package com.ecommerce.admin.dto;

import java.time.Instant;

public record AdminUserSummary(
        Long userId,
        String email,
        String fullName,
        Instant registeredAt
) {}
