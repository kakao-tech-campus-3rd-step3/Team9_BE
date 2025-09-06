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

    public Material(String title, MaterialCategory materialCategory, String content, Long studyId, Long userId) {
        validateInput(title, materialCategory, content, studyId, userId);

        this.title = title;
        this.materialCategory = materialCategory;
        this.content = content;
        this.studyId = studyId;
        this.userId = userId;
    }

    public void updateMaterial(String title, MaterialCategory materialCategory, String content) {
        validateUpdateInput(title, materialCategory, content);

        this.title = title;
        this.materialCategory = materialCategory;
        this.content = content;
    }

    public boolean isOwnedBy(Long userId) {
        return this.userId.equals(userId);
    }

    private void validateInput(String title, MaterialCategory materialCategory, String content, Long studyId, Long userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_TITLE);
        }
        if (materialCategory == null) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_CATEGORY);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_CONTENT);
        }
        if (studyId == null) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_IDS);
        }
        if (userId == null) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_WRITER);
        }
    }

    private void validateUpdateInput(String title, MaterialCategory materialCategory, String content) {
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_TITLE);
        }
        if (materialCategory == null) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_CATEGORY);
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_MATERIAL_CONTENT);
        }
    }
}