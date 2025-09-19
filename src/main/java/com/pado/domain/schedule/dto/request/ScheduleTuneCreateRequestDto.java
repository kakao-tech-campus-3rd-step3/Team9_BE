package com.pado.domain.schedule.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

@Schema(description = "조율 요청 생성 DTO")
public record ScheduleTuneCreateRequestDto(
    @Schema(description = "제목", example = "주간 정기 회의")
    @NotBlank(message = "제목은 비어 있을 수 없습니다.")
    String title,

    @Schema(description = "내용", example = "의제: 진행상황 점검")
    @NotBlank(message = "내용은 비어 있을 수 없습니다.")
    String content,

    @Schema(description = "시작 일자", example = "2025-09-20")
    @JsonProperty("start_date")
    @NotNull(message = "start_date는 필수입니다.")
    LocalDate startDate,

    @Schema(description = "종료 일자", example = "2025-09-25")
    @JsonProperty("end_date")
    @NotNull(message = "end_date는 필수입니다.")
    LocalDate endDate,

    @Schema(description = "가능 시작 시간", example = "10:00")
    @JsonProperty("available_start_time")
    @NotNull(message = "available_start_time은 필수입니다.")
    LocalTime availableStartTime,

    @Schema(description = "가능 종료 시간", example = "18:00")
    @JsonProperty("available_end_time")
    @NotNull(message = "available_end_time은 필수입니다.")
    LocalTime availableEndTime
) {

}
