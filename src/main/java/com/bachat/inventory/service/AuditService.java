package com.bachat.inventory.service;

import com.bachat.inventory.domain.AuditLog;
import com.bachat.inventory.repository.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final AuditLogRepository auditRepo;

    public AuditService(AuditLogRepository auditRepo) {
        this.auditRepo = auditRepo;
    }

    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "system";
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, String details) {
        AuditLog entry = new AuditLog(entityType, entityId, action, getCurrentUsername(), details);
        auditRepo.save(entry);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, String details,
                    String oldValue, String newValue) {
        AuditLog entry = new AuditLog(entityType, entityId, action, getCurrentUsername(), details);
        entry.setOldValue(oldValue);
        entry.setNewValue(newValue);
        auditRepo.save(entry);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByEntity(String entityType, Long entityId, Pageable pageable) {
        return auditRepo.findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByUser(String username, Pageable pageable) {
        return auditRepo.findByUsernameOrderByCreatedAtDesc(username, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getByDateRange(LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return auditRepo.findByCreatedAtBetweenOrderByCreatedAtDesc(start, end, pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAll(Pageable pageable) {
        return auditRepo.findAllByOrderByCreatedAtDesc(pageable);
    }
}
