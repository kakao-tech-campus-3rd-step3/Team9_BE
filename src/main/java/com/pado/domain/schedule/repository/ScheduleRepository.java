package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findAllByStudy(Study study);
}