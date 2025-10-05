package com.pado.domain.study.repository;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyApplication;
import com.pado.domain.study.entity.StudyApplicationStatus;
import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {

    boolean existsByStudyAndUserAndStatus(Study study, User user, StudyApplicationStatus status);

    @Query("SELECT sa FROM StudyApplication sa JOIN FETCH sa.user WHERE sa.study = :study")
    List<StudyApplication> findByStudyWithUser(@Param("study") Study study);
}