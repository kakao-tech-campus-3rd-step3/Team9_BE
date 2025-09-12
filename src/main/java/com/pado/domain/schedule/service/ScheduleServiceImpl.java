package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;

    @Override
    public void createSchedule(Long studyId, ScheduleCreateRequestDto request) {
        // TODO: 향후 Study 도메인이 구현되면, studyId로 실제 스터디가 존재하는지 확인하는 로직 추가가 필요함.

        Schedule schedule = Schedule.builder()
            .studyId(studyId)
            .title(request.title())
            .description(request.content())
            .startTime(request.start_time())
            .endTime(request.end_time())
            .build();

        scheduleRepository.save(schedule);
    }

    @Override
    public List<ScheduleResponseDto> findAllSchedulesByStudyId(Long studyId) {
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
    public ScheduleDetailResponseDto findScheduleDetailById(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
            .orElseThrow(() -> new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

        return new ScheduleDetailResponseDto(
            schedule.getId(),
            schedule.getTitle(),
            schedule.getDescription(),
            schedule.getStartTime(),
            schedule.getEndTime()
        );
    }
}