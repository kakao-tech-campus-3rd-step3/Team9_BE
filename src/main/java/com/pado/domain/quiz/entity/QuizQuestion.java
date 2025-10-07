package com.pado.domain.quiz.entity;

import com.pado.domain.quiz.dto.response.AnswerResultDto;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "question_type", discriminatorType = DiscriminatorType.STRING)
public abstract class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnswerSubmission> answerSubmissions = new ArrayList<>();

    public abstract int calculateScore(String userAnswer);

    public abstract AnswerResultDto toAnswerResultDto(AnswerSubmission userAnswer);

    protected QuizQuestion(Quiz quiz, String questionText, String explanation) {
        this.quiz = quiz;
        this.questionText = questionText;
        this.explanation = explanation;
    }

    protected void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

}