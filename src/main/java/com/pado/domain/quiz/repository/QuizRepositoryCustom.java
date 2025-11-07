package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.DashboardQuizProjection;
import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.entity.Quiz;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuizRepositoryCustom {
    List<QuizInfoProjection> findByStudyIdWithCursor(Long studyId, Long cursor, int pageSize);
    Map<Long, Long> findQuestionCountsByQuizIds(List<Long> quizIds);
    Optional<Quiz> findWithSourceFilesById(Long id);
    Optional<Quiz> findForStartQuizById(Long quizId);
    List<DashboardQuizProjection> findRecentDashboardQuizzes(Long studyId, Long userId, int size);
}