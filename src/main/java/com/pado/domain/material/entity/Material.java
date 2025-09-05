package com.pado.domain.material.entity;

import com.pado.domain.baseTime.AuditingEntity;
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

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "material_category")
    private MaterialCategory materialCategory;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id")
//    private User user;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "study_id")
//    private Study study;

    public Material(
            String title,
            MaterialCategory materialCategory,
            String content) {
        this.title = title;
        this.materialCategory = materialCategory;
        this.content = content;
    }
}
