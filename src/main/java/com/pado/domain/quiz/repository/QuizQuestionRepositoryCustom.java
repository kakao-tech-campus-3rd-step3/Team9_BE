package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizQuestion;
import java.util.List;

public interface QuizQuestionRepositoryCustom {
    List<QuizQuestion> findByQuizId(Long quizId);
}
