package com.hotel.repository;

import com.hotel.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByEntityTypeAndEntityId(String entityType, Long entityId, Pageable pageable);
    Page<AuditLog> findAllByChangedBy(String changedBy, Pageable pageable);
}
