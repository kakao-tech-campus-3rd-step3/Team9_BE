package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByStudyId(Long studyId);
    List<Schedule> findByStudyIdOrderByStartTimeAsc(Long studyId);
    @Query("SELECT s FROM Schedule s " +
            "JOIN StudyMember sm ON s.studyId = sm.study.id " +
            "WHERE sm.user.id = :userId " +
            "AND s.endTime > :periodStart " +
            "AND s.startTime < :periodEnd " +
            "ORDER BY s.startTime ASC")
    List<Schedule> findAllByUserIdAndPeriod(
            @Param("userId") Long userId,
            @Param("periodStart") LocalDateTime periodStart,
            @Param("periodEnd") LocalDateTime periodEnd);
}