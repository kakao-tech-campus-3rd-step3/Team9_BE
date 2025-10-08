package com.pado.domain.schedule.service;

import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.PastScheduleResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleByDateResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.event.ScheduleCreatedEvent;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.service.StudyMemberService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.userdetails.CustomUserDetails;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberService studyMemberService;
    private final ApplicationEventPublisher eventPublisher;
    private final ReflectionRepository reflectionRepository;
    private final StudyMemberRepository studyMemberRepository;

    @Override
    public void createSchedule(Long studyId, ScheduleCreateRequestDto request) {
        User currentUser = getCurrentUser();
        Study study = findStudyById(studyId);

        if (!studyMemberService.isStudyLeader(currentUser, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        Schedule schedule = Schedule.builder()
            .studyId(studyId)
            .title(request.title())
            .description(request.content())
            .startTime(request.start_time())
            .endTime(request.end_time())
            .build();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        eventPublisher.publishEvent(
                new ScheduleCreatedEvent(
                        studyId,
                        savedSchedule.getId(),
                        savedSchedule.getTitle()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleByDateResponseDto> findMySchedulesByMonth(Long userId, int year,
        int month) {
        // 1. 요청받은 달의 1일 찾기
        LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
        // 2. 그 1일이 포함된 주의 일요일 찾기 (캘린더의 시작일)
        LocalDate startDate = firstDayOfMonth.with(
            TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        // 3. 캘린더의 종료일 찾기 (시작일 + 6주 - 1일)
        LocalDate endDate = startDate.plusWeeks(6).minusDays(1);
        List<Schedule> schedules = scheduleRepository.findAllByUserIdAndPeriod(
            userId,
            startDate.atStartOfDay(),
            endDate.plusDays(1).atStartOfDay()
        );
        // 4. DB에서 startDate와 endDate 사이의 모든 일정을 조회하여 반환
        return schedules.stream()
            .map(schedule -> new ScheduleByDateResponseDto(
                schedule.getId(),
                schedule.getStudyId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> findAllSchedulesByStudyId(Long studyId) {
        User currentUser = getCurrentUser();
        if (!studyMemberService.isStudyMember(currentUser, studyId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        List<Schedule> schedules = scheduleRepository.findAllByStudyId(studyId);
        return schedules.stream()
            .map(schedule -> new ScheduleResponseDto(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getStartTime(),
                schedule.getEndTime()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScheduleDetailResponseDto findScheduleDetailById(Long scheduleId) {
        Schedule schedule = findScheduleById(scheduleId);
        User currentUser = getCurrentUser();

        if (!studyMemberService.isStudyMember(currentUser, schedule.getStudyId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        return new ScheduleDetailResponseDto(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getDescription(),
            schedule.getStartTime(),
            schedule.getEndTime()
        );
    }

    @Override
    public void updateSchedule(Long scheduleId, ScheduleCreateRequestDto request) {
        User currentUser = getCurrentUser();
        Schedule schedule = findScheduleById(scheduleId);
        Study study = findStudyById(schedule.getStudyId());
        if (!studyMemberService.isStudyLeader(currentUser, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }
        schedule.update(request.title(), request.content(), request.start_time(),
            request.end_time());
    }

    @Override
    public void deleteSchedule(Long scheduleId) {
        User currentUser = getCurrentUser();
        Schedule schedule = findScheduleById(scheduleId);
        Study study = findStudyById(schedule.getStudyId());
        if (!studyMemberService.isStudyLeader(currentUser, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }
        scheduleRepository.delete(schedule);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PastScheduleResponseDto> findPastSchedulesForReflection(Long studyId, User user) {
        // 1. 스터디 멤버인지 확인하면서 StudyMember 정보 가져오기
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId,
                user.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

        // 2. '내가' 이미 회고를 작성한 스케줄 ID 목록 조회
        Set<Long> writtenScheduleIds =
            reflectionRepository.findExistingScheduleIdsByStudyMember(studyId, studyMember.getId())
                .stream().collect(Collectors.toSet());

        // 3. 현재 시간 이전의 모든 스케줄 조회 (시작 시간 기준)
        List<Schedule> pastSchedules =
            scheduleRepository.findByStudyIdAndStartTimeBefore(studyId, LocalDateTime.now());

        // 4. 회고가 작성되지 않은 스케줄만 필터링 후 DTO로 변환
        return pastSchedules.stream()
            .filter(schedule -> !writtenScheduleIds.contains(schedule.getId()))
            .map(schedule -> new PastScheduleResponseDto(schedule.getId(), schedule.getTitle()))
            .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof CustomUserDetails) {
            return ((CustomUserDetails) principal).getUser();
        }
        throw new BusinessException(ErrorCode.UNAUTHENTICATED_USER);
    }

    private Study findStudyById(Long studyId) {
        return studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
    }

    private Schedule findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));
    }
}
