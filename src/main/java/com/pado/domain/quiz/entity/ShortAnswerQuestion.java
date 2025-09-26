package com.pado.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@DiscriminatorValue("SHORT_ANSWER")
public class ShortAnswerQuestion extends QuizQuestion {

    @Column(nullable = false)
    private String answer;

    protected ShortAnswerQuestion() {
    }

    @Builder
    public ShortAnswerQuestion(Quiz quiz, String questionText, String answer) {
        super(quiz, questionText);
        this.answer = answer;
    }
}