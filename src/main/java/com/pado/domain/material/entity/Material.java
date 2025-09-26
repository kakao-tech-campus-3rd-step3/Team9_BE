package com.pado.domain.material.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;
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
    private Integer week;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id", nullable = false)
     private User user;

     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "study_id", nullable = false)
     private Study study;

    public Material(String title, MaterialCategory materialCategory, Integer week, String content, Study study, User user) {

        validateCategoryAndWeek(materialCategory, week);

        this.title = title;
        this.materialCategory = materialCategory;
        this.week = week;
        this.content = content;
        this.study = study;
        this.user = user;
    }

    public void updateMaterial(String title, MaterialCategory materialCategory, Integer week, String content) {

        validateCategoryAndWeek(materialCategory, week);

        this.title = title;
        this.materialCategory = materialCategory;
        this.week = week;
        this.content = content;
    }

    public boolean isOwnedBy(User user) {

        return this.user.getId().equals(user.getId());
    }

    public boolean isLearningMaterial() {
        return MaterialCategory.LEARNING.equals(this.materialCategory);
    }

    private void validateCategoryAndWeek(MaterialCategory materialCategory, Integer week) {
        if (MaterialCategory.LEARNING.equals(materialCategory)) {
            // 학습자료는 주차가 필수
            if (week == null) {
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