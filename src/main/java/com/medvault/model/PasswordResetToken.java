package com.medvault.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id; // unique id for each token entry in the database

    @Column(nullable = false)
    private String token; // Random unique token string that goes in th reset link

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // which user this token belongs to

    private LocalDateTime expiryDate; // when this token will expire

    // a method to check if the token is expired or not
    public boolean isExpired(){
        return expiryDate.isBefore(LocalDateTime.now());
    }
}
