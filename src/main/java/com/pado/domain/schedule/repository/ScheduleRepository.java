package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findAllByStudyId(Long studyId);
    List<Schedule> findByStudyIdOrderByStartTimeAsc(Long studyId);
}