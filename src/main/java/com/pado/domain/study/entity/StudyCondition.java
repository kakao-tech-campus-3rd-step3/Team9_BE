package com.pado.domain.study.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_condition", uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_study_condition",
                columnNames = {"study_id", "content"}
        )
})
public class StudyCondition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Builder
    public StudyCondition(Study study, String content) {
        this.study = study;
        this.content = content;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
