package com.ecommerce.admin.service;

import com.ecommerce.admin.entity.AuditEntry;
import com.ecommerce.admin.repository.AuditEntryRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    private final AuditEntryRepository auditEntryRepository;

    public AuditService(AuditEntryRepository auditEntryRepository) {
        this.auditEntryRepository = auditEntryRepository;
    }

    public void record(String entityType, Long entityId, String action, String performedBy, String details) {
        auditEntryRepository.save(new AuditEntry(
                entityType, entityId, action, performedBy, Instant.now(), details));
    }
}
