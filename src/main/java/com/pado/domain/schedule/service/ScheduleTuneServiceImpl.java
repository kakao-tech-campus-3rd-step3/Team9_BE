package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.entity.ScheduleTune;
import com.pado.domain.schedule.entity.ScheduleTuneParticipant;
import com.pado.domain.schedule.entity.ScheduleTuneSlot;
import com.pado.domain.schedule.entity.ScheduleTuneStatus;
import com.pado.domain.schedule.repository.ScheduleTuneParticipantRepository;
import com.pado.domain.schedule.repository.ScheduleTuneRepository;
import com.pado.domain.schedule.repository.ScheduleTuneSlotRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.service.StudyMemberService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.userdetails.CustomUserDetails;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleTuneServiceImpl implements ScheduleTuneService {

    private final ScheduleTuneRepository scheduleTuneRepository;
    private final ScheduleTuneParticipantRepository scheduleTuneParticipantRepository;
    private final ScheduleTuneSlotRepository scheduleTuneSlotRepository;

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyMemberService studyMemberService;

    @Override
    public Long createScheduleTune(Long studyId, ScheduleTuneCreateRequestDto request) {
        User currentUser = getCurrentUser();
        Study study = findStudy(studyId);

        if (!studyMemberService.isStudyLeader(currentUser, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        validateDateTimeWindow(request.startDate(), request.endDate(),
            request.availableStartTime(), request.availableEndTime());

        ScheduleTune tune = ScheduleTune.builder()
            .studyId(studyId)
            .title(request.title())
            .description(request.content())
            .startDate(request.startDate())
            .endDate(request.endDate())
            .availableStartTime(request.availableStartTime())
            .availableEndTime(request.availableEndTime())
            .slotMinutes(30)
            .status(ScheduleTuneStatus.PENDING)
            .build();

        ScheduleTune saved = scheduleTuneRepository.save(tune);

        List<StudyMember> members = studyMemberRepository.findByStudyId(studyId);
        List<ScheduleTuneParticipant> participants = new ArrayList<>(members.size());
        long bit = 1L;
        for (StudyMember m : members) {
            participants.add(ScheduleTuneParticipant.builder()
                .scheduleTune(saved)
                .studyMemberId(m.getId())
                .candidateNumber(bit)
                .votedAt(null)
                .build());
            bit <<= 1;
        }
        scheduleTuneParticipantRepository.saveAll(participants);

        int memberCount = participants.size();
        int bytes = Math.max(1, (memberCount + 7) / 8);
        List<ScheduleTuneSlot> slots = buildSlots(saved,
            request.startDate(), request.endDate(),
            request.availableStartTime(), request.availableEndTime(),
            saved.getSlotMinutes(), bytes);
        scheduleTuneSlotRepository.saveAll(slots);

        return saved.getId();
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ScheduleTuneResponseDto> findAllScheduleTunes(Long studyId) {
        User currentUser = getCurrentUser();
        if (!studyMemberService.isStudyMember(currentUser, studyId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        List<ScheduleTune> list = scheduleTuneRepository
            .findByStudyIdAndStatusOrderByIdDesc(studyId, ScheduleTuneStatus.PENDING);

        List<ScheduleTuneResponseDto> result = new ArrayList<>(list.size());
        for (ScheduleTune t : list) {
            LocalDateTime start = LocalDateTime.of(
                t.getStartDate(), t.getAvailableStartTime());
            LocalDateTime end = LocalDateTime.of(
                t.getEndDate(), t.getAvailableEndTime());
            result.add(new ScheduleTuneResponseDto(t.getTitle(), start, end));
        }
        return result;
    }

    @Override
    @Transactional(Transactional.TxType.SUPPORTS)
    public ScheduleTuneDetailResponseDto findScheduleTuneDetail(Long studyId, Long tuneId) {
        User currentUser = getCurrentUser();
        if (!studyMemberService.isStudyMember(currentUser, studyId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        ScheduleTune tune = scheduleTuneRepository.findByIdAndStudyId(tuneId, studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PENDING_SCHEDULE_NOT_FOUND));

        List<ScheduleTuneParticipant> parts = scheduleTuneParticipantRepository.findByScheduleTuneId(
            tune.getId());

        List<ScheduleTuneParticipantDto> partDtos = new ArrayList<>(parts.size());
        for (ScheduleTuneParticipant p : parts) {
            partDtos.add(new ScheduleTuneParticipantDto(p.getId(), null, p.getCandidateNumber()));
        }

        LocalDateTime availableStart = LocalDateTime.of(tune.getStartDate(),
            tune.getAvailableStartTime());
        LocalDateTime availableEnd = LocalDateTime.of(tune.getEndDate(),
            tune.getAvailableEndTime());

        return new ScheduleTuneDetailResponseDto(
            tune.getTitle(),
            tune.getDescription(),
            List.of(),
            availableStart,
            availableEnd,
            partDtos
        );
    }

    @Override
    public ScheduleTuneParticipantResponseDto participate(Long studyId, Long tuneId,
        ScheduleTuneParticipantRequestDto request) {
        throw new BusinessException(ErrorCode.INVALID_INPUT, "participate API는 다음 단계에서 구현됩니다.");
    }

    @Override
    public ScheduleCompleteResponseDto complete(Long tuneId, ScheduleCreateRequestDto request) {
        throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE, "complete API는 다음 단계에서 구현됩니다.");
    }
    

    private List<ScheduleTuneSlot> buildSlots(
        ScheduleTune tune,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime availStart,
        LocalTime availEnd,
        int slotMinutes,
        int occupancyBytes
    ) {
        List<ScheduleTuneSlot> slots = new ArrayList<>();
        LocalDate cur = startDate;
        int idx = 0;
        while (!cur.isAfter(endDate)) {
            LocalDateTime s = LocalDateTime.of(cur, availStart);
            LocalDateTime e = LocalDateTime.of(cur, availEnd);
            LocalDateTime ptr = s;
            while (!ptr.plusMinutes(slotMinutes).isAfter(e)) {
                LocalDateTime slotStart = ptr;
                LocalDateTime slotEnd = ptr.plusMinutes(slotMinutes);
                slots.add(ScheduleTuneSlot.builder()
                    .scheduleTune(tune)
                    .slotIndex(idx++)
                    .startTime(slotStart)
                    .endTime(slotEnd)
                    .occupancyBits(new byte[occupancyBytes])
                    .build());
                ptr = slotEnd;
            }
            cur = cur.plusDays(1);
        }
        return slots;
    }

    private void validateDateTimeWindow(LocalDate startDate, LocalDate endDate, LocalTime s,
        LocalTime e) {
        if (Objects.isNull(startDate) || Objects.isNull(endDate) || Objects.isNull(s)
            || Objects.isNull(e)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "날짜/시간 범위가 올바르지 않습니다.");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "end_date는 start_date 이후여야 합니다.");
        }
        if (!e.isAfter(s)) {
            throw new BusinessException(ErrorCode.INVALID_START_TIME,
                "available_end_time은 available_start_time 이후여야 합니다.");
        }
    }

    private Study findStudy(Long studyId) {
        return studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails cud) {
            return cud.getUser();
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
    }
}
