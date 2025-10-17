package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.Quiz;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long>, QuizRepositoryCustom  {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Quiz> findWithLockById(Long quizId);
}