package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, QuizSubmissionRepositoryCustom {
}