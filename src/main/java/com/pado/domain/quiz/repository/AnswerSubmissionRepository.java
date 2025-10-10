package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.AnswerSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerSubmissionRepository extends JpaRepository<AnswerSubmission, Long>, AnswerSubmissionRepositoryCustom {

}