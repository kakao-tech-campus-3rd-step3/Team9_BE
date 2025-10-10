package com.pado.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.ArrayList;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceQuestion extends QuizQuestion {

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizChoice> choices = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "correct_choice_id")
    private QuizChoice correctChoice;

    @Builder
    public MultipleChoiceQuestion(Quiz quiz, String questionText, List<QuizChoice> choices, QuizChoice correctChoice) {
        super(quiz, questionText);
        this.choices = choices;
        this.correctChoice = correctChoice;
    }

}