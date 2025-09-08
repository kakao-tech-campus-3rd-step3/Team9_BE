package com.pado.domain.material.entity;

import com.pado.domain.baseTime.AuditingEntity;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "material")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_category", nullable = false)
    private MaterialCategory materialCategory;

    @Column(name = "week", length = 10)
    private String week;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // TODO: 추후 User, Study 엔티티 연관관계 활성화
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "user_id", nullable = false)
    // private User user;

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "study_id", nullable = false)
    // private Study study;

    // 임시로 studyId와 userId를 직접 저장 (추후 연관관계로 변경)
    @Column(name = "study_id", nullable = false)
    private Long studyId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public Material(String title, MaterialCategory materialCategory, String week, String content, Long studyId, Long userId) {

        validateCategoryAndWeek(materialCategory, week);

        this.title = title;
        this.materialCategory = materialCategory;
        this.week = week;
        this.content = content;
        this.studyId = studyId;
        this.userId = userId;
    }

    public void updateMaterial(String title, MaterialCategory materialCategory, String week, String content) {

        validateCategoryAndWeek(materialCategory, week);

        this.title = title;
        this.materialCategory = materialCategory;
        this.week = week;
        this.content = content;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    public boolean isLearningMaterial() {
        return MaterialCategory.LEARNING.equals(this.materialCategory);
    }

    private void validateCategoryAndWeek(MaterialCategory materialCategory, String week) {
        if (MaterialCategory.LEARNING.equals(materialCategory)) {
            // 학습자료는 주차가 필수
            if (week == null || week.trim().isEmpty()) {
                throw new BusinessException(ErrorCode.INVALID_MATERIAL_WEEK_REQUIRED);
            }
        }
        else {
            // 학습자료가 아니면 주차가 있으면 안됨
            if (week != null) {
                throw new BusinessException(ErrorCode.INVALID_MATERIAL_WEEK_NOT_ALLOWED);
            }
        }
    }
}