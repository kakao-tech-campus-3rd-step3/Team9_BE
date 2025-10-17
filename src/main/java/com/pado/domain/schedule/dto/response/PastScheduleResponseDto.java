package com.pado.domain.schedule.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;

@Schema(description = "회고 작성/수정 가능한 스터디 일정 조회 응답 DTO")
@AllArgsConstructor
public class PastScheduleResponseDto {

    @JsonProperty("schedule_id")
    @Schema(description = "스터디 일정 ID", example = "1")
    public Long scheduleId;

    @JsonProperty("schedule_title")
    @Schema(description = "스터디 일정 제목", example = "1주차 정기 회의")
    public String scheduleTitle;
}