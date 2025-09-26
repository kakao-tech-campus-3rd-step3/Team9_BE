package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.entity.Quiz;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface QuizRepositoryCustom {
    List<QuizInfoProjection> findByStudyIdWithCursor(Long studyId, Long cursor, int pageSize);
    Map<Long, Long> findQuestionCountsByQuizIds(List<Long> quizIds);
    Optional<Quiz> findDetailById(Long quizId);
}
