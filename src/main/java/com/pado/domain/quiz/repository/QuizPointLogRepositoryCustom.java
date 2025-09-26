package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizPointLog;
import java.util.List;

public interface QuizPointLogRepositoryCustom {
    List<QuizPointLog> findActiveLogsForQuiz(Long quizId);
}
