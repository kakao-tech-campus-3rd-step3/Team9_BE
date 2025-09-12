package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_application")
public class StudyApplication extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StudyApplicationStatus status;

    @Column(length = 500)
    private String message;

    private StudyApplication(Study study, User user, String message) {
        this.study = study;
        this.user = user;
        this.message = message;
        this.status = StudyApplicationStatus.PENDING;
    }

    public static StudyApplication create(Study study, User user, String message) {
        return new StudyApplication(
                Objects.requireNonNull(study),
                Objects.requireNonNull(user),
                message
        );
    }
}