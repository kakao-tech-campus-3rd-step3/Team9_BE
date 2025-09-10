package com.pado.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "일정 조율 참여 요청 DTO")
public record ScheduleTuneParticipantRequestDto(
        @Schema(description = "선택한 후보 시간을 나타내는 비트마스크 값. 각 비트는 시간 슬롯을 의미합니다.",
                example = "21")
        @NotNull(message = "후보 날짜 정보는 필수 입력 항목입니다.")
        Long candidate_dates
) {}
