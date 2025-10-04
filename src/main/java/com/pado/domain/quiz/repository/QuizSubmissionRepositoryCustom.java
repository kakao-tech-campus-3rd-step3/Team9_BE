package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.entity.QuizSubmission;
import com.pado.domain.quiz.repository.dto.UserQuizCountDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QuizSubmissionRepositoryCustom {
    List<SubmissionStatusDto> findSubmissionStatuses(List<Long> quizIds, Long userId);
    Optional<QuizSubmission> findWithDetailsById(Long submissionId);
    List<UserQuizCountDto> countByStudyGroupByUser(Long studyId);
    default Map<Long, Long> countMapByStudy(Long studyId) {
        return countByStudyGroupByUser(studyId).stream()
                .collect(Collectors.toMap(UserQuizCountDto::userId, UserQuizCountDto::cnt));
    }
}