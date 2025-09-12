package com.pado.domain.study.entity;

import com.pado.domain.shared.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "study_category", indexes = {
        @Index(name = "idx_study_category_study_id", columnList = "study_id"),
        @Index(name = "idx_study_category_category", columnList = "category")
})
public class StudyCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Builder
    public StudyCategory(Study study, Category category) {
        this.study = study;
        this.category = category;
    }
}