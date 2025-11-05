package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
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
    @Setter
    @Column(nullable = false, length = 100)
    private String title;
    @Setter
    @Column(nullable = false, length = 200)
    private String description;

    @Setter
    @Lob
    private String detailDescription;
    @Setter
    @Column(length = 100)
    private String studyTime;

    @Setter
    @Column(nullable = false)
    private Integer maxMembers;
    @Setter
    @Column(length = 500)
    private String fileKey;

    @Setter
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

    public void update(String title, String description, String detailDescription, Region region,
        String studyTime, Integer maxMembers, String fileKey, List<Category> interests,
        List<String> conditions) {
        this.title = title;
        this.description = description;
        this.detailDescription = detailDescription;
        this.region = region;
        this.studyTime = studyTime;
        this.maxMembers = maxMembers;
        this.fileKey = fileKey;
        updateInterests(interests);
        updateConditions(conditions);
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