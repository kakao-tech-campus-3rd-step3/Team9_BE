package com.pado.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "answer_submission")
public class AnswerSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private QuizSubmission submission;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private QuizQuestion question;

    @Column(name = "submitted_answer", columnDefinition = "TEXT")
    private String submittedAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Builder
    public AnswerSubmission(QuizSubmission submission, QuizQuestion question, String submittedAnswer, boolean isCorrect) {
        this.submission = submission;
        this.question = question;
        this.submittedAnswer = submittedAnswer;
        this.isCorrect = isCorrect;
    }

    public void updateAnswer(String submittedAnswer) {
        this.submittedAnswer = submittedAnswer;
    }

    public void updateFinalAnswer(String userAnswer, boolean isCorrect) {
        this.submittedAnswer = userAnswer;
        this.isCorrect = isCorrect;
    }

    public void assignSubmission(QuizSubmission submission) {
        this.submission = submission;
    }
}