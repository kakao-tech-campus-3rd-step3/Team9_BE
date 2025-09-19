package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.ScheduleTuneSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTuneSlotRepository extends JpaRepository<ScheduleTuneSlot, Long> {

    List<ScheduleTuneSlot> findByScheduleTuneIdOrderBySlotIndexAsc(Long scheduleTuneId);

    boolean existsByScheduleTuneId(Long scheduleTuneId);

    void deleteByScheduleTuneId(Long scheduleTuneId);
}
