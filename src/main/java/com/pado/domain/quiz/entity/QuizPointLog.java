package com.pado.domain.quiz.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.study.entity.StudyMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_point_log")
public class QuizPointLog extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_member_id", nullable = false)
    private StudyMember studyMember;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_submission_id", nullable = false, unique = true)
    private QuizSubmission quizSubmission;

    @Column(name = "points_awarded", nullable = false)
    private Integer pointsAwarded;

    @Column(nullable = false)
    private boolean revoked = false;

    @Builder
    public QuizPointLog(StudyMember studyMember, QuizSubmission quizSubmission, Integer pointsAwarded) {
        this.studyMember = studyMember;
        this.quizSubmission = quizSubmission;
        this.pointsAwarded = pointsAwarded;
        this.revoked = false;
    }

    public void revoke() {
        this.revoked = true;
    }

}