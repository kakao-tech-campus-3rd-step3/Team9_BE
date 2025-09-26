package com.pado.domain.schedule.entity;

import com.pado.domain.basetime.AuditingEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "schedule_tune_participant",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_schedule_tune_participant__tune_member",
        columnNames = {"schedule_tune_id", "study_member_id"}
    )
)
@AttributeOverrides({
    @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = false, updatable = false)),
    @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = false))
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleTuneParticipant extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_tune_id", nullable = false)
    private ScheduleTune scheduleTune;

    @Column(name = "study_member_id", nullable = false)
    private Long studyMemberId;

    @Column(name = "candidate_number", nullable = false)
    private Long candidateNumber;

    @Column(name = "voted_at")
    private LocalDateTime votedAt;

    @Builder
    public ScheduleTuneParticipant(ScheduleTune scheduleTune,
        Long studyMemberId,
        Long candidateNumber,
        LocalDateTime votedAt) {
        this.scheduleTune = scheduleTune;
        this.studyMemberId = studyMemberId;
        this.candidateNumber = candidateNumber;
        this.votedAt = votedAt;
    }

    public void markVotedNow() {
        this.votedAt = LocalDateTime.now();
    }
}
