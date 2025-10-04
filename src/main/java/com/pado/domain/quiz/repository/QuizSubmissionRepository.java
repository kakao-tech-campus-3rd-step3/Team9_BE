package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, QuizSubmissionRepositoryCustom {
    Optional<QuizSubmission> findByQuizIdAndUserId(Long quizId, Long userId);
}