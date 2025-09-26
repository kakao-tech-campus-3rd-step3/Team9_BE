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

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Builder
    public QuizSubmission(Quiz quiz, User user, SubmissionStatus status, LocalDateTime submittedAt) {
        this.quiz = quiz;
        this.user = user;
        this.status = status;
        this.score = 0;
        this.totalQuestions = quiz.getQuestions().size();
        this.submittedAt = submittedAt != null
                                        ? submittedAt
                                        : LocalDateTime.now();
    }

    public void complete(int finalScore) {
        this.score = finalScore;
        this.status = SubmissionStatus.COMPLETED;
        this.submittedAt = LocalDateTime.now();
    }

    public void addAnswerSubmission(AnswerSubmission answer) {
        this.answers.add(answer);
        answer.setSubmission(this);
    }

    public void validateIsNotCompleted() {
        if (this.status == SubmissionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUIZ_ALREADY_COMPLETED);
        }
    }
}