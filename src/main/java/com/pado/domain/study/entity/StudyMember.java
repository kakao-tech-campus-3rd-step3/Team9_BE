package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_member")
public class StudyMember extends AuditingEntity {

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
    private StudyMemberRole role;

    @Column(length = 500)
    private String resolution;

    @Builder
    public StudyMember(Study study, User user, StudyMemberRole role, String resolution) {
        this.study = study;
        this.user = user;
        this.role = role;
        this.resolution = resolution;
    }
}