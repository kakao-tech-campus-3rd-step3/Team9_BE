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
    name = "schedule_tune_slot",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_schedule_tune_slot__tune_index",
        columnNames = {"schedule_tune_id", "slot_index"}
    )
)
@AttributeOverrides({
    @AttributeOverride(name = "createdAt", column = @Column(name = "created_at", nullable = false, updatable = false)),
    @AttributeOverride(name = "updatedAt", column = @Column(name = "updated_at", nullable = false))
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduleTuneSlot extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_tune_id", nullable = false)
    private ScheduleTune scheduleTune;

    @Column(name = "slot_index", nullable = false)
    private Integer slotIndex;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Lob
    @Column(name = "occupancy_bits", nullable = false)
    private byte[] occupancyBits;

    @Builder
    public ScheduleTuneSlot(ScheduleTune scheduleTune,
        Integer slotIndex,
        LocalDateTime startTime,
        LocalDateTime endTime,
        byte[] occupancyBits) {
        this.scheduleTune = scheduleTune;
        this.slotIndex = slotIndex;
        this.startTime = startTime;
        this.endTime = endTime;
        this.occupancyBits = occupancyBits;
    }
}
