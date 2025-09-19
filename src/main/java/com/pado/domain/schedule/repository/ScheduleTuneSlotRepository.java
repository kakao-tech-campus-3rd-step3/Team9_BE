package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.ScheduleTuneSlot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

@Repository
public interface ScheduleTuneSlotRepository extends JpaRepository<ScheduleTuneSlot, Long> {

    List<ScheduleTuneSlot> findByScheduleTuneIdOrderBySlotIndexAsc(Long scheduleTuneId);

    boolean existsByScheduleTuneId(Long scheduleTuneId);

    void deleteByScheduleTuneId(Long scheduleTuneId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000"))
    List<ScheduleTuneSlot> findByScheduleTuneIdOrderBySlotIndexAscForUpdate(Long scheduleTuneId);
}
