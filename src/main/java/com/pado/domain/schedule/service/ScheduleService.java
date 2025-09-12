package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;

public interface ScheduleService {

    void createSchedule(Long studyId, ScheduleCreateRequestDto request);

}