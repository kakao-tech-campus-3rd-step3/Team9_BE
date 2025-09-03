package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "일정 조율 참여 응답 DTO")
public record ScheduleTuneParticipantResponseDto(
        @Schema(description = "응답 메시지", example = "일정 조율 참여 정보가 성공적으로 업데이트되었습니다.")
        String message
) {}
