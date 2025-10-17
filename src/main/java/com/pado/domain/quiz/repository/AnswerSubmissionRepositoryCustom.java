package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.AnswerSubmission;
import java.util.List;

public interface AnswerSubmissionRepositoryCustom {
    List<AnswerSubmission> findBySubmissionId(Long submissionId);
}