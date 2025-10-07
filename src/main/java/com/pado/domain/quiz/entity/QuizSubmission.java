package com.pado.domain.quiz.entity;

import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "quiz_submission")
public class QuizSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerSubmission> answers = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    @Version
    private Long version;

    @Column(name = "started_at", nullable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToOne(mappedBy = "quizSubmission", cascade = CascadeType.ALL, orphanRemoval = true)
    private QuizPointLog pointLog;

    @Builder
    public QuizSubmission(User user, LocalDateTime startedAt) {
        this.quiz = null;
        this.user = user;
        this.status = SubmissionStatus.IN_PROGRESS;
        this.score = 0;
        this.totalQuestions = 0;
        this.startedAt = (startedAt != null)
                ? startedAt
                : LocalDateTime.now();
    }

    protected void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.totalQuestions = quiz.getQuestions().size();
    }

    public void complete(int finalScore) {
        if (this.status == SubmissionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }

        this.score = finalScore;
        this.status = SubmissionStatus.COMPLETED;
        this.submittedAt = LocalDateTime.now();
    }

    public void addAnswerSubmission(AnswerSubmission answer) {
        if (answer.getSubmission() == null) {
            answer.assignSubmission(this);
        }
        if (!answers.contains(answer)) {
            this.answers.add(answer);
        }
    }

    public AnswerSubmission findOrCreateAnswer(QuizQuestion question) {
        return this.getAnswers().stream()
                .filter(answer -> answer.getQuestion().getId().equals(question.getId()))
                .findFirst()
                .orElseGet(() -> {
                    AnswerSubmission newAnswer = AnswerSubmission.builder()
                            .submission(this)
                            .question(question)
                            .build();
                    this.addAnswerSubmission(newAnswer);
                    return newAnswer;
                });
    }

    public void validateIsNotCompleted() {
        if (this.status == SubmissionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }
    }
}