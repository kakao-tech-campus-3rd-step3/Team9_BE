package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import java.util.List;

public interface ScheduleService {

    void createSchedule(Long studyId, ScheduleCreateRequestDto request);

    List<ScheduleResponseDto> findAllSchedulesByStudyId(Long studyId);

    ScheduleDetailResponseDto findScheduleDetailById(Long scheduleId);

    void updateSchedule(Long studyId, Long scheduleId, ScheduleCreateRequestDto request);

    void deleteSchedule(Long studyId, Long scheduleId);
}