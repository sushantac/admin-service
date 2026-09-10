package com.ecommerce.admin.repository;

import com.ecommerce.admin.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {
    boolean existsByEventId(String eventId);
}
