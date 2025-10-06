package com.medvault.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // who this file belongs to(patient)
    @ManyToOne(optional = false)
    private User patient;


    //Who uploaded it patient or doctor
    @ManyToOne(optional = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String storedFilename;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private long sizeByte;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RecordType type;

    @Column(nullable = false)
    @Builder.Default
    private Instant uploadedAt = Instant.now();

    public enum RecordType { PRESCRIPTION, LAB_REPORT, HISTORY, OTHER }

    public String getReadableSize(){
        double kb = sizeByte / 1024.0;
        if (kb<1024){
            return String.format("%.2f KB", kb);
        }
        else {
            double mb = kb / 1024.0;
            return String.format("%.2f MB", mb);
        }
    }

}
