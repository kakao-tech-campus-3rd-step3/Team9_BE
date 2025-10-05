package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    public void addInterests(List<Category> categories) {
        this.interests.clear();
        if (categories != null) {
            List<StudyCategory> newInterests = categories.stream()
                .map(category -> StudyCategory
                    .builder()
                    .study(this)
                    .category(category)
                    .build()
                )
                .toList();
            this.interests.addAll(newInterests);
        }
    }

    public void addConditions(List<String> conditionContents) {
        this.conditions.clear();
        if (conditionContents != null) {
            List<StudyCondition> newConditions = conditionContents.stream()
                .map(content -> StudyCondition.builder()
                    .study(this)
                    .content(content)
                    .build())
                .toList();
            this.conditions.addAll(newConditions);
        }
    }

}