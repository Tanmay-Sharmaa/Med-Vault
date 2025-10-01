package com.medvault.repository;

import com.medvault.model.MedicalRecord;
import com.medvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Long> {
    List<MedicalRecord> findByPatientOrderByUploadedAtDesc(User patient);
}
