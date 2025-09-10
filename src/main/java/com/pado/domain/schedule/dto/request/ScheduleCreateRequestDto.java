package com.pado.domain.schedule.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Schema(description = "일정 생성 요청 DTO")
public record ScheduleCreateRequestDto(
        @Schema(description = "일정 제목", example = "3차 스터디 모임")
        @NotBlank(message = "일정 제목은 필수 입력 항목입니다.")
        String title,

        @Schema(description = "일정 내용", example = "스프링 시큐리티에 대한 발표와 코드 리뷰를 진행합니다.")
        @NotBlank(message = "일정 내용은 필수 입력 항목입니다.")
        String content,

        @Schema(description = "일정 시작 시간", example = "2025-09-10T14:00:00")
        @NotNull(message = "시작 시간은 필수 입력 항목입니다.")
        @Future(message = "시작 시간은 현재 시각보다 미래여야 합니다.")
        LocalDateTime start_time,

        @Schema(description = "일정 종료 시간", example = "2025-09-10T16:00:00")
        @NotNull(message = "종료 시간은 필수 입력 항목입니다.")
        @Future(message = "종료 시간은 현재 시각보다 미래여야 합니다.")
        LocalDateTime end_time
) {}
