package com.pado.domain.quiz.entity;

import com.pado.domain.quiz.dto.response.AnswerResultDto;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Builder;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Collections;

@Entity
@Getter
@DiscriminatorValue("SHORT_ANSWER")
public class ShortAnswerQuestion extends QuizQuestion {

    @Column(nullable = false)
    private String answer;

    protected ShortAnswerQuestion() {
    }

    @Builder
    public ShortAnswerQuestion(Quiz quiz, String questionText, String explanation, String answer) {
        super(quiz, questionText, explanation);
        this.answer = answer;
    }

    @Override
    public int calculateScore(String userAnswer) {
        if (!StringUtils.hasText(userAnswer)) {
            return 0;
        }
        boolean correct = normalize(getAnswer()).equals(normalize(userAnswer));
        return correct ? 1 : 0;
    }

    @Override
    public AnswerResultDto toAnswerResultDto(AnswerSubmission userAnswer) {
        String userAnswerText = userAnswer != null
                ? userAnswer.getSubmittedAnswer()
                : "";

        boolean isCorrect = userAnswer != null
                && userAnswer.isCorrect();

        return new AnswerResultDto(
                this.getId(),
                QuestionType.SHORT_ANSWER,
                this.getQuestionText(),
                isCorrect,
                userAnswerText,
                this.getAnswer(),
                this.getExplanation(),
                Collections.emptyList()
        );
    }

    private String normalize(String input) {
        return input == null ? "" : input.trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }
}