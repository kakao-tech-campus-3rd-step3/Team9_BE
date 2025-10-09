package com.pado.domain.quiz.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.material.entity.File;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
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
    @Builder.Default
    private Set<File> sourceFiles = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @BatchSize(size = 100)
    private List<QuizQuestion> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<QuizSubmission> submissions = new ArrayList<>();

    public void updateStatus(QuizStatus status) {
        this.status = status;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }

    public QuizSubmission start(User user) {
        return this.submissions.stream()
                .filter(submission -> submission.getUser().getId().equals(user.getId()))
                .findFirst()
                .map(submission -> {
                    submission.validateIsNotCompleted();
                    return submission;
                })
                .orElseGet(() -> {
                    QuizSubmission newSubmission = QuizSubmission.builder()
                            .user(user)
                            .build();
                    this.addSubmission(newSubmission);
                    return newSubmission;
                });
    }

    private void addSubmission(QuizSubmission submission) {
        this.submissions.add(submission);
        submission.setQuiz(this);
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