package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "조율 일정 참여자 정보 DTO")
public record ScheduleTuneParticipantDto(
        @Schema(description = "참여자 ID", example = "1")
        Long id,

        @Schema(description = "참여자 닉네임", example = "스터디원1")
        String name,

        @Schema(description = "선택한 후보 시간을 나타내는 비트마스크 값. 각 비트는 시간 슬롯을 의미합니다.",
                example = "21")
        @NotNull(message = "후보 날짜 정보는 필수 입력 항목입니다.")
        Long candidate_number
) {}