package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
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
}