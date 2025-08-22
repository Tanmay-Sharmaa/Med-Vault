package com.medvault.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // assign a unique id to each role

    @Column(nullable=false, unique=true)
    private String name; // ADMIN, DOCTOR, PATIENT
}
