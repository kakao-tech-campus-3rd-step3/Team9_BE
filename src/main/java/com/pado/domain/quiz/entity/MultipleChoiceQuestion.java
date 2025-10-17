package com.pado.domain.quiz.entity;

import com.pado.domain.quiz.dto.response.AnswerResultDto;
import com.pado.domain.quiz.dto.response.ChoiceResultDto;
import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestion extends QuizQuestion {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 100)
    private List<QuizChoice> choices = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correct_choice_id")
    private QuizChoice correctChoice;

    @Builder
    public MultipleChoiceQuestion(Quiz quiz, String questionText, String explanation, List<QuizChoice> choices, QuizChoice correctChoice) {
        super(quiz, questionText, explanation);
        this.choices = choices;
        this.correctChoice = correctChoice;
    }

    public void setChoices(List<QuizChoice> choices) {
        this.choices = choices;
        choices.forEach(choice -> choice.setQuestion(this));
    }

    @Override
    public int calculateScore(String userAnswer) {
        if (!StringUtils.hasText(userAnswer)) {
            return 0;
        }

        Long correctChoiceId = getCorrectChoice() != null
                            ? getCorrectChoice().getId()
                            : null;

        if (correctChoiceId == null) {
            return 0;
        }

        try {
            long userAnswerChoiceId = Long.parseLong(userAnswer);
            return correctChoiceId.equals(userAnswerChoiceId) ? 1 : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public AnswerResultDto toAnswerResultDto(AnswerSubmission userAnswer) {
        String userAnswerText = userAnswer != null
                            ? userAnswer.getSubmittedAnswer()
                            : "";

        boolean isCorrect = userAnswer != null
                && userAnswer.isCorrect();

        Long correctChoiceId = getCorrectChoice() != null
                            ? getCorrectChoice().getId()
                            : null;

        final Long finalUserAnswerChoiceId = parseUserAnswerToLong(userAnswerText);

        List<ChoiceResultDto> choices = this.getChoices().stream()
                .map(choice -> new ChoiceResultDto(
                        choice.getId(),
                        choice.getChoiceText(),
                        choice.getId().equals(correctChoiceId),
                        choice.getId().equals(finalUserAnswerChoiceId)
                ))
                .toList();

        return new AnswerResultDto(
                this.getId(),
                QuestionType.MULTIPLE_CHOICE,
                this.getQuestionText(),
                isCorrect,
                userAnswerText,
                correctChoiceId != null ? correctChoiceId.toString() : "",
                this.getExplanation(),
                choices
        );
    }

    private Long parseUserAnswerToLong(String userAnswerText) {
        if (!StringUtils.hasText(userAnswerText)) {
            return null;
        }
        try {
            return Long.parseLong(userAnswerText);
        } catch (NumberFormatException e) {
            log.warn("Invalid user answer format for MCQ DTO mapping: {}", userAnswerText);
            return null;
        }
    }

}