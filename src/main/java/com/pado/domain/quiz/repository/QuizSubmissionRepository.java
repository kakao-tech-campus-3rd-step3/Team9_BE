package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, QuizSubmissionRepositoryCustom {
    Optional<QuizSubmission> findByQuizIdAndUserId(Long quizId, Long userId);
}