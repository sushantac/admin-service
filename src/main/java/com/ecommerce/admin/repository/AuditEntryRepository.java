package com.ecommerce.admin.repository;

import com.ecommerce.admin.entity.AuditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEntryRepository extends JpaRepository<AuditEntry, Long> {
}
