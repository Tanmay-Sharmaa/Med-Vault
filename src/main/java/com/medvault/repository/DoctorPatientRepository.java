package com.medvault.repository;

import com.medvault.model.DoctorPatient;
import com.medvault.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface DoctorPatientRepository extends JpaRepository<DoctorPatient,Long>{
    boolean existsByDoctorAndPatient(User doctor,User patient );
    List<DoctorPatient>findByDoctor(User doctor);
    Optional<DoctorPatient>findByDoctorAndPatient(User doctor, User patient);


}
