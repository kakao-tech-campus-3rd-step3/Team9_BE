package com.pado.domain.schedule.entity;

import com.pado.domain.basetime.AuditingEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule_tune")
@AttributeOverrides({
    @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = false, updatable = false)),
    @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = false))
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleTune extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "available_start_time", nullable = false)
    private LocalTime availableStartTime;

    @Column(name = "available_end_time", nullable = false)
    private LocalTime availableEndTime;

    @Column(name = "slot_minutes", nullable = false)
    private Integer slotMinutes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ScheduleTuneStatus status;

    @OneToMany(mappedBy = "scheduleTune", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleTuneParticipant> participants = new ArrayList<>();

    @OneToMany(mappedBy = "scheduleTune", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ScheduleTuneSlot> slots = new ArrayList<>();

    @Builder
    public ScheduleTune(Long studyId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime availableStartTime,
        LocalTime availableEndTime,
        Integer slotMinutes,
        ScheduleTuneStatus status) {
        this.studyId = studyId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.availableStartTime = availableStartTime;
        this.availableEndTime = availableEndTime;
        this.slotMinutes = (slotMinutes == null ? 30 : slotMinutes);
        this.status = (status == null ? ScheduleTuneStatus.PENDING : status);
    }

    public void complete() {
        this.status = ScheduleTuneStatus.COMPLETED;
    }
}
