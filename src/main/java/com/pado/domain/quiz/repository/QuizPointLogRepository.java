package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizPointLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizPointLogRepository extends JpaRepository<QuizPointLog, Long>, QuizPointLogRepositoryCustom {

}