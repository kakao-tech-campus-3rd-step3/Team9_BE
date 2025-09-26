package com.pado.domain.reflection.entity;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.basetime.AuditingEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Table(name = "reflection")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Reflection extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_member_id", nullable = false)
    private StudyMember studyMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(nullable = false)
    private Integer satisfactionScore;

    @Column(nullable = false)
    private Integer understandingScore;

    @Column(nullable = false)
    private Integer participationScore;

    @Column(nullable = false, length = 1000)
    private String learnedContent;

    @Column(nullable = false, length = 1000)
    private String improvement;
}
