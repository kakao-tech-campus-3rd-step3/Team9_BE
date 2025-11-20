package com.pado.domain.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "조율 참여(슬롯 선택) DTO")
public record ScheduleTuneParticipantRequestDto(
    @Schema(description = "전체 슬롯에 대한 참여 가능 여부 배열 (0: 불가능, 1: 가능). 배열 길이는 해당 조율의 전체 슬롯 수와 일치해야 합니다.", example = "[1, 1, 0, 0, 1, 0]")
    @JsonProperty("candidate_dates")
    @NotNull(message = "candidate_dates는 필수입니다.")
    List<Long> candidateDates
) {

}