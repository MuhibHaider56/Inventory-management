package com.bachat.inventory.controller;

import com.bachat.inventory.domain.AuditLog;
import com.bachat.inventory.service.AuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Audit Log", description = "View audit trail of all system changes.")
@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "List all audit logs (paginated)")
    @GetMapping
    public Page<AuditLog> listAll(@RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "50") int size) {
        return auditService.getAll(PageRequest.of(page, size));
    }

    @Operation(summary = "Get audit logs for a specific entity")
    @GetMapping("/entity/{entityType}/{entityId}")
    public Page<AuditLog> getByEntity(@PathVariable String entityType,
                                      @PathVariable Long entityId,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "50") int size) {
        return auditService.getByEntity(entityType.toUpperCase(), entityId, PageRequest.of(page, size));
    }

    @Operation(summary = "Get audit logs by username")
    @GetMapping("/user/{username}")
    public Page<AuditLog> getByUser(@PathVariable String username,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        return auditService.getByUser(username, PageRequest.of(page, size));
    }

    @Operation(summary = "Get audit logs by date range")
    @GetMapping("/range")
    public Page<AuditLog> getByRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return auditService.getByDateRange(start, end, PageRequest.of(page, size));
    }
}
