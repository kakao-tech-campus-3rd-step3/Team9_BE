package com.pado.domain.quiz.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.material.entity.File;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz")
public class Quiz extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(nullable = false)
    private String title;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private QuizStatus status;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "quiz_file_source",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private Set<File> sourceFiles = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSubmission> submissions = new ArrayList<>();

    @Builder
    public Quiz(Study study, User createdBy, String title, Integer timeLimitSeconds, QuizStatus status) {
        this.study = study;
        this.createdBy = createdBy;
        this.title = title;
        this.timeLimitSeconds = timeLimitSeconds;
        this.status = status;
    }

    public void updateStatus(QuizStatus status) {
        this.status = status;
    }

    public void setSourceFiles(Set<File> sourceFiles) {
        this.sourceFiles = sourceFiles;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public void addQuestions(Collection<QuizQuestion> questions) {
        questions.forEach(question -> {
            this.questions.add(question);
            question.setQuiz(this);
        });
    }

    public void validateIsActive() {
        if (this.status != QuizStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_ACTIVE, "Quiz is not active.");
        }
    }
}