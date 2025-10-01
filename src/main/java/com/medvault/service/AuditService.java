package com.medvault.service;

import com.medvault.model.AuditLog;
import com.medvault.model.MedicalRecord;
import com.medvault.model.User;
import com.medvault.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Small helper to write audit log entries in one line.
 */
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    public void log(User actor, MedicalRecord rec, AuditLog.Action action) {
        repo.save(AuditLog.builder()
                .actor(actor)
                .record(rec)
                .action(action)
                .build());
    }
}
