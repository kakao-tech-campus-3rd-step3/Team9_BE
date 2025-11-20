package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.Quiz;
import com.pado.domain.quiz.entity.QuizSubmission;
import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, QuizSubmissionRepositoryCustom {
    Optional<QuizSubmission> findByQuizAndUser(Quiz quiz, User user);
}