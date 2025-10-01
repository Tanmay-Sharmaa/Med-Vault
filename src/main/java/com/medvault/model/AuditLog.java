package com.medvault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * this code snippet keeps a history of uploaded and downloaded as per user and as per record
 */
@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class AuditLog {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(optional = false)
    private User actor;


    @ManyToOne(optional = false)
    private MedicalRecord record;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Action action;

    @Column(nullable = false)
    public Instant at = Instant.now();

    public enum Action{ UPLOAD, DOWNLOAD}

}
