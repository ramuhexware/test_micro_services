package com.rapidx.audit.controller;

import com.rapidx.audit.entity.AuditLog;
import com.rapidx.audit.repository.AuditLogRepository;
import com.rapidx.audit.specification.AuditLogSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/audit")
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@RequestBody AuditLog auditLog) {
        if (auditLog.getTimestamp() == null) {
            auditLog.setTimestamp(LocalDateTime.now());
        }
        AuditLog saved = auditLogRepository.save(auditLog);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<AuditLog>> getAllAuditLogs(@PageableDefault(size = 20) Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findAll(pageable);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable String id) {
        return auditLogRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<AuditLog>> filterAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String details,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        Specification<AuditLog> spec = AuditLogSpecification.filterLogs(action, username, details);
        Page<AuditLog> logs = auditLogRepository.findAll(spec, pageable);
        return ResponseEntity.ok(logs);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuditLog> updateAuditLog(@PathVariable String id, @RequestBody AuditLog auditLogDetails) {
        return auditLogRepository.findById(id)
                .map(existingLog -> {
                    if (auditLogDetails.getAction() != null) {
                        existingLog.setAction(auditLogDetails.getAction());
                    }
                    if (auditLogDetails.getUsername() != null) {
                        existingLog.setUsername(auditLogDetails.getUsername());
                    }
                    if (auditLogDetails.getDetails() != null) {
                        existingLog.setDetails(auditLogDetails.getDetails());
                    }
                    if (auditLogDetails.getTimestamp() != null) {
                        existingLog.setTimestamp(auditLogDetails.getTimestamp());
                    }
                    AuditLog updated = auditLogRepository.save(existingLog);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAuditLog(@PathVariable String id) {
        return auditLogRepository.findById(id)
                .map(log -> {
                    auditLogRepository.delete(log);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
