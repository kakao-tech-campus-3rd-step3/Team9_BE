package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.ScheduleTune;
import com.pado.domain.schedule.entity.ScheduleTuneStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTuneRepository extends JpaRepository<ScheduleTune, Long> {

    List<ScheduleTune> findByStudyIdAndStatusOrderByIdDesc(Long studyId, ScheduleTuneStatus status);

    Optional<ScheduleTune> findByIdAndStudyId(Long id, Long studyId);

    boolean existsByIdAndStatus(Long id, ScheduleTuneStatus status);
}
