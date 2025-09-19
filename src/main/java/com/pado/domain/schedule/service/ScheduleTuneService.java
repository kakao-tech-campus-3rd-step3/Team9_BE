package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import java.util.List;

public interface ScheduleTuneService {

    Long createScheduleTune(Long studyId, ScheduleTuneCreateRequestDto request);

    List<ScheduleTuneResponseDto> findAllScheduleTunes(Long studyId);

    ScheduleTuneDetailResponseDto findScheduleTuneDetail(Long studyId, Long tuneId);

    ScheduleTuneParticipantResponseDto participate(Long studyId, Long tuneId,
        ScheduleTuneParticipantRequestDto request);

    ScheduleCompleteResponseDto complete(Long tuneId, ScheduleCreateRequestDto request);
}
