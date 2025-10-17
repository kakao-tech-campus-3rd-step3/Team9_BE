package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.QuizSubmission;

import java.util.List;
import java.util.Optional;

public interface QuizSubmissionRepositoryCustom {
    List<SubmissionStatusDto> findSubmissionStatuses(List<Long> quizIds, Long userId);
    Optional<QuizSubmission> findWithDetailsById(Long submissionId);
}