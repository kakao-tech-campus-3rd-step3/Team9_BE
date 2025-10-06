package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.PastScheduleResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleByDateResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import com.pado.domain.user.entity.User;

import java.util.List;

public interface ScheduleService {

    void createSchedule(Long studyId, ScheduleCreateRequestDto request);

    List<ScheduleByDateResponseDto> findMySchedulesByMonth(Long userId, int year, int month);

    List<ScheduleResponseDto> findAllSchedulesByStudyId(Long studyId);

    ScheduleDetailResponseDto findScheduleDetailById(Long scheduleId);

    void updateSchedule(Long scheduleId, ScheduleCreateRequestDto request);

    void deleteSchedule(Long scheduleId);

    List<PastScheduleResponseDto> findPastSchedulesForReflection(Long studyId, User user);
}