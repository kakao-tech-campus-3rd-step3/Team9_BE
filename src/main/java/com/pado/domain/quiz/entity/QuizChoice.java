package com.pado.domain.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class QuizChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String choiceText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private MultipleChoiceQuestion question;

    @Builder
    public QuizChoice(MultipleChoiceQuestion question, String choiceText) {
        this.question = question;
        this.choiceText = choiceText;
    }

}