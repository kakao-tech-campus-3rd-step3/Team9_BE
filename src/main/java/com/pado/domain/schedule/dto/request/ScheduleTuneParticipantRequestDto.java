package com.pado.domain.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Schema(description = "조율 참여(슬롯 선택) DTO")
public record ScheduleTuneParticipantRequestDto(
    @Schema(description = "선택 슬롯 비트마스크 배열(0/1 비트로 구성된 정수 배열)", example = "[3,5,2]")
    @JsonProperty("candidate_dates")
    @NotNull(message = "candidate_dates는 필수입니다.")
    List<Long> candidateDates
) {

}
