package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.Reflection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReflectionRepository extends JpaRepository<Reflection, Long>,
    ReflectionRepositoryCustom {

    List<Reflection> findByStudyId(Long studyId);
    
    @Query("SELECT r.schedule.id FROM Reflection r WHERE r.study.id = :studyId AND r.studyMember.id = :studyMemberId AND r.schedule.id IS NOT NULL")
    List<Long> findExistingScheduleIdsByStudyMember(@Param("studyId") Long studyId,
        @Param("studyMemberId") Long studyMemberId);
}