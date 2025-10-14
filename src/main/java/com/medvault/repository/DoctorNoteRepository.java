package com.medvault.repository;

import com.medvault.model.DoctorNote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DoctorNoteRepository extends JpaRepository<DoctorNote, Long> {
    List<DoctorNote> findByPatientId(Long patientId);
    List<DoctorNote> findByDoctorIdAndPatientIdOrderByCreatedAtDesc(Long doctorId, Long patientId);
}
