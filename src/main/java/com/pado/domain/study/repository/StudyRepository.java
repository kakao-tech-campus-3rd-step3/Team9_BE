package com.pado.domain.study.repository;

import com.pado.domain.study.entity.Study;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudyRepository extends JpaRepository<Study, Long>, StudyRepositoryCustom {
    @Query("SELECT s FROM Study s JOIN FETCH s.leader WHERE s.id = :studyId")
    Optional<Study> findByIdWithLeader(@Param("studyId") Long studyId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Study s WHERE s.id = :studyId")
    Optional<Study> findByIdWithPessimisticLock(@Param("studyId") Long studyId);
}