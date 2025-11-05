package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyUpdateRequestDto;
import com.pado.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "study", indexes = {
    @Index(name = "idx_study_status_created_at", columnList = "status, createdAt"),
    @Index(name = "idx_study_region", columnList = "region")
})
public class Study extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 200)
    private String description;

    @Lob
    private String detailDescription;

    @Column(length = 100)
    private String studyTime;

    @Column(nullable = false)
    private Integer maxMembers;

    @Column(length = 500)
    private String fileKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StudyStatus status = StudyStatus.RECRUITING;

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyCondition> conditions = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyCategory> interests = new ArrayList<>();

    @OneToMany(mappedBy = "study", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<StudyMember> studyMembers = new ArrayList<>();


    public void update(StudyUpdateRequestDto requestDto) {
        if (requestDto.title() != null) {
            this.title = requestDto.title();
        }
        if (requestDto.description() != null) {
            this.description = requestDto.description();
        }
        if (requestDto.detail_description() != null) {
            this.detailDescription = requestDto.detail_description();
        }
        if (requestDto.region() != null) {
            this.region = requestDto.region();
        }
        if (requestDto.study_time() != null) {
            this.studyTime = requestDto.study_time();
        }
        if (requestDto.max_members() != null) {
            this.maxMembers = requestDto.max_members();
        }
        if (requestDto.file_key() != null) {
            this.fileKey = requestDto.file_key();
        }

        if (requestDto.interests() != null) {
            updateInterests(requestDto.interests());
        }
        if (requestDto.conditions() != null) {
            updateConditions(requestDto.conditions());
        }
    }

    public void updateInterests(List<Category> newCategories) {
        if (newCategories == null) {
            return;
        }

        Set<Category> newCategorySet = new HashSet<>(newCategories);
        Set<Category> currentCategorySet = this.interests.stream()
            .map(StudyCategory::getCategory)
            .collect(Collectors.toSet());

        this.interests.removeIf(
            studyCategory -> !newCategorySet.contains(studyCategory.getCategory()));

        for (Category newCategory : newCategories) {
            if (!currentCategorySet.contains(newCategory)) {
                StudyCategory studyCategory = StudyCategory.builder()
                    .study(this)
                    .category(newCategory)
                    .build();
                this.interests.add(studyCategory);
            }
        }
    }

    public void updateConditions(List<String> newConditionContents) {
        if (newConditionContents == null) {
            return;
        }

        Set<String> newContentSet = new HashSet<>(newConditionContents);
        Set<String> currentContentSet = this.conditions.stream()
            .map(StudyCondition::getContent)
            .collect(Collectors.toSet());

        this.conditions.removeIf(
            studyCondition -> !newContentSet.contains(studyCondition.getContent()));

        for (String newContent : newConditionContents) {
            if (!currentContentSet.contains(newContent)) {
                StudyCondition studyCondition = StudyCondition.builder()
                    .study(this)
                    .content(newContent)
                    .build();
                this.conditions.add(studyCondition);
            }
        }
    }
}