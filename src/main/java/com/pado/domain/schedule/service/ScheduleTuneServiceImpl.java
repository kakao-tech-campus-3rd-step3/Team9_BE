package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.entity.ScheduleTune;
import com.pado.domain.schedule.entity.ScheduleTuneParticipant;
import com.pado.domain.schedule.entity.ScheduleTuneSlot;
import com.pado.domain.schedule.entity.ScheduleTuneStatus;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.schedule.repository.ScheduleTuneParticipantRepository;
import com.pado.domain.schedule.repository.ScheduleTuneRepository;
import com.pado.domain.schedule.repository.ScheduleTuneSlotRepository;
import com.pado.domain.schedule.util.BitMaskUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final ScheduleRepository scheduleRepository;

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
        List<ScheduleTuneSlot> slots = buildSlots(
            saved,
            request.startDate(), request.endDate(),
            request.availableStartTime(), request.availableEndTime(),
            saved.getSlotMinutes(),
            bytes
        );
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
        List<ScheduleTune> list =
            scheduleTuneRepository.findByStudyIdAndStatusOrderByIdDesc(studyId,
                ScheduleTuneStatus.PENDING);

        List<ScheduleTuneResponseDto> result = new ArrayList<>(list.size());
        for (ScheduleTune t : list) {
            LocalDateTime start = LocalDateTime.of(t.getStartDate(), t.getAvailableStartTime());
            LocalDateTime end = LocalDateTime.of(t.getEndDate(), t.getAvailableEndTime());
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

        Study study = findStudy(studyId);
        Map<Long, String> nameByStudyMemberId = new HashMap<>();
        for (StudyMember sm : studyMemberRepository.findByStudyWithUser(study)) {
            nameByStudyMemberId.put(sm.getId(), sm.getUser().getNickname());
        }

        List<ScheduleTuneParticipant> parts = scheduleTuneParticipantRepository.findByScheduleTuneId(
            tune.getId());
        List<ScheduleTuneParticipantDto> partDtos = new ArrayList<>(parts.size());
        for (ScheduleTuneParticipant p : parts) {
            partDtos.add(new ScheduleTuneParticipantDto(
                p.getId(),
                nameByStudyMemberId.get(p.getStudyMemberId()),
                p.getCandidateNumber()
            ));
        }

        List<ScheduleTuneSlot> slots =
            scheduleTuneSlotRepository.findByScheduleTuneIdOrderBySlotIndexAsc(tune.getId());
        List<Long> candidateDates = new ArrayList<>(slots.size());
        for (ScheduleTuneSlot s : slots) {
            candidateDates.add(BitMaskUtils.toUnsignedLong(s.getOccupancyBits()));
        }

        LocalDateTime availableStart = LocalDateTime.of(tune.getStartDate(),
            tune.getAvailableStartTime());
        LocalDateTime availableEnd = LocalDateTime.of(tune.getEndDate(),
            tune.getAvailableEndTime());

        return new ScheduleTuneDetailResponseDto(
            tune.getTitle(),
            tune.getDescription(),
            candidateDates,
            availableStart,
            availableEnd,
            partDtos
        );
    }

    @Override
    public ScheduleTuneParticipantResponseDto participate(Long studyId, Long tuneId,
        ScheduleTuneParticipantRequestDto request) {
        User currentUser = getCurrentUser();
        if (!studyMemberService.isStudyMember(currentUser, studyId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
        ScheduleTune tune = scheduleTuneRepository.findByIdAndStudyId(tuneId, studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PENDING_SCHEDULE_NOT_FOUND));
        if (tune.getStatus() != ScheduleTuneStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE, "이미 완료된 조율입니다.");
        }

        StudyMember member = studyMemberRepository.findByStudyIdAndUserId(studyId,
                currentUser.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        ScheduleTuneParticipant participant = scheduleTuneParticipantRepository
            .findByScheduleTuneIdAndStudyMemberId(tune.getId(), member.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INPUT, "조율 참여 대상이 아닙니다."));

        int bitIndex = BitMaskUtils.bitIndexFromCandidateNumber(participant.getCandidateNumber());

        List<ScheduleTuneSlot> slots =
            scheduleTuneSlotRepository.findByScheduleTuneIdOrderBySlotIndexAscForUpdate(
                tune.getId());

        if (request.candidateDates().size() != slots.size()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "candidate_dates 길이가 슬롯 수와 다릅니다.");
        }

        for (int i = 0; i < slots.size(); i++) {
            ScheduleTuneSlot slot = slots.get(i);
            boolean selected = request.candidateDates().get(i) != 0L;
            byte[] bits = slot.getOccupancyBits();
            BitMaskUtils.setBit(bits, bitIndex, selected);
        }

        participant.markVotedNow();
        scheduleTuneParticipantRepository.save(participant);
        scheduleTuneSlotRepository.saveAll(slots);

        return new ScheduleTuneParticipantResponseDto("updated");
    }

    @Override
    public ScheduleCompleteResponseDto complete(Long tuneId, ScheduleCreateRequestDto request) {
        User currentUser = getCurrentUser();
        ScheduleTune tune = scheduleTuneRepository.findById(tuneId)
            .orElseThrow(() -> new BusinessException(ErrorCode.PENDING_SCHEDULE_NOT_FOUND));
        Study study = findStudy(tune.getStudyId());
        if (!studyMemberService.isStudyLeader(currentUser, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }
        if (tune.getStatus() != ScheduleTuneStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE, "이미 완료된 조율입니다.");
        }

        if (!request.end_time().isAfter(request.start_time())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "end_time은 start_time 이후여야 합니다.");
        }
        if (request.start_time().toLocalDate().isBefore(tune.getStartDate())
            || request.end_time().toLocalDate().isAfter(tune.getEndDate())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택 시간이 조율 날짜 범위를 벗어납니다.");
        }
        LocalTime s = request.start_time().toLocalTime();
        LocalTime e = request.end_time().toLocalTime();
        if (s.isBefore(tune.getAvailableStartTime()) || e.isAfter(tune.getAvailableEndTime())) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택 시간이 가용 시간대를 벗어납니다.");
        }

        boolean matched = false;
        List<ScheduleTuneSlot> slots =
            scheduleTuneSlotRepository.findByScheduleTuneIdOrderBySlotIndexAsc(tune.getId());
        for (ScheduleTuneSlot slot : slots) {
            if (slot.getStartTime().equals(request.start_time())
                && slot.getEndTime().equals(request.end_time())) {
                matched = true;
                break;
            }
        }
        if (!matched) {
            throw new BusinessException(ErrorCode.INVALID_INPUT, "선택 시간이 생성된 슬롯과 일치하지 않습니다.");
        }

        Schedule schedule = Schedule.builder()
            .studyId(tune.getStudyId())
            .title(request.title())
            .description(request.content())
            .startTime(request.start_time())
            .endTime(request.end_time())
            .build();
        scheduleRepository.save(schedule);

        tune.complete();
        scheduleTuneRepository.save(tune);

        return new ScheduleCompleteResponseDto(true);
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
