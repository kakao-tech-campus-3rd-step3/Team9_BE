package com.pado.domain.study.repository;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {
    long countByStudy(Study study);
    boolean existsByStudyAndUser(Study study, User user);
}