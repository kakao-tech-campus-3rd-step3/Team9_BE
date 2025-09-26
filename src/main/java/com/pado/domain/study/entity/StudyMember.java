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
    private String message;

    @Column(name = "rank_point", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer rankPoint;

    @Builder
    public StudyMember(Study study, User user, StudyMemberRole role, String message, Integer rankPoint) {
        this.study = study;
        this.user = user;
        this.role = role;
        this.message = message;
        this.rankPoint = (rankPoint != null) ? rankPoint : 0;
    }

    public void addRankPoints(int pointsToAdd) {
        if (pointsToAdd > 0) {
            this.rankPoint += pointsToAdd;
        }
    }

    public void subtractRankPoints(int pointsToSubtract) {
        if (pointsToSubtract > 0) {
            this.rankPoint = Math.max(0, this.rankPoint - pointsToSubtract);
        }
    }
}