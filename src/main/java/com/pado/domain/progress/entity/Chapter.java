package com.pado.domain.progress.entity;

import com.pado.domain.study.entity.Study;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(nullable = false, length = 500)
    @NotBlank
    private String content;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean completed;

    @Builder
    private Chapter(Study study, String content, boolean completed) {
        this.study = study;
        this.content = content;
        this.completed = completed;
    }

    public static Chapter createChapter(Study study, String content, boolean completed){
        return Chapter.builder()
                .study(study)
                .content(content)
                .completed(completed)
                .build();
    }
}
