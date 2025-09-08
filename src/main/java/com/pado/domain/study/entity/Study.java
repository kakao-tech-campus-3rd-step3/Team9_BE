package com.pado.domain.study.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.category.entity.Category;
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
@Table(name = "study")
public class Study extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Builder.Default
    private StudyStatus status = StudyStatus.RECRUITING;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "study_condition", joinColumns = @JoinColumn(name = "study_id"))
    @Column(name = "content", length = 500)
    @Builder.Default
    private List<String> conditions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "study_category", joinColumns = @JoinColumn(name = "study_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    @Builder.Default
    private List<Category> interests = new ArrayList<>();
}