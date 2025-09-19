package com.pado.domain.schedule.repository;

import com.pado.domain.schedule.entity.ScheduleTuneParticipant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleTuneParticipantRepository extends
    JpaRepository<ScheduleTuneParticipant, Long> {

    List<ScheduleTuneParticipant> findByScheduleTuneId(Long scheduleTuneId);

    Optional<ScheduleTuneParticipant> findByScheduleTuneIdAndStudyMemberId(Long scheduleTuneId,
        Long studyMemberId);

    boolean existsByScheduleTuneIdAndStudyMemberId(Long scheduleTuneId, Long studyMemberId);

    long countByScheduleTuneId(Long scheduleTuneId);
}
