package com.pado.domain.auth.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_verifications_email", columnList = "email")
})
public class EmailVerification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 6)
    private String code;


    @Builder
    public EmailVerification(String email, String code) {
        this.email = email;
        this.code = code;
    }
}

