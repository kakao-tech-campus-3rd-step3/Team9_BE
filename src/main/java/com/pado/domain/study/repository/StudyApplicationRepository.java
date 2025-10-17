package com.pado.domain.study.repository;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyApplication;
import com.pado.domain.study.entity.StudyApplicationStatus;
import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    boolean existsByStudyAndUserAndStatus(Study study, User user, StudyApplicationStatus status);
}