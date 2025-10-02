package com.medvault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

// this code snippet maps which patients are assigned to a doctor so that the doctor can see data of their
// own patient only and get restricted from seeing the data of the patients assigned to other doctors

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "doctor_patient",
       uniqueConstraints = @UniqueConstraint(columnNames = {"doctor_id","patient_id"}))
public class DoctorPatient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "doctor_id")
    private User doctor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "patient_id")
    private User patient;

    @Column(nullable = false)
    @Builder.Default
    private Instant assignedAt = Instant.now();



}
