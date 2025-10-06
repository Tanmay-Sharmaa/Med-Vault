package com.medvault.repository;

import com.medvault.model.AuditLog;
import com.medvault.model.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByRecord_Patient_EmailOrderByAtDesc(String email);

}


