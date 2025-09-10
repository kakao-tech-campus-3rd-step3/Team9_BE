package com.pado.domain.schedule.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "조율 중인 일정 세부 정보 응답 DTO")
public record ScheduleTuneDetailResponseDto(
        @Schema(description = "일정 제목", example = "3차 스터디 모임 날짜 조율")
        String title,

        @Schema(description = "일정 설명", example = "모두가 가능한 날짜에 스터디 모임을 잡기 위해 날짜 조율을 시작합니다.")
        String description,

        @Schema(description = "후보 날짜의 가능한 시간들을 나타내는 비트마스크 배열", example = "[3, 5, 2]")
        List<Long> candidate_dates,

        @Schema(description = "참여 가능 시작 시간", example = "2025-09-10T10:00:00")
        LocalDateTime available_start_time,

        @Schema(description = "참여 가능 종료 시간", example = "2025-09-10T18:00:00")
        LocalDateTime available_end_time,

        @Schema(description = "참여자 목록 및 선택한 후보 날짜")
        List<ScheduleTuneParticipantDto> participants
) {}