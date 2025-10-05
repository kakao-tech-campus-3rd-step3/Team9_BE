package com.pado.domain.reflection.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Schema(description = "회고 간략 정보 응답 DTO")
@Builder
public record ReflectionSimpleResponseDto(
    @Schema(description = "회고 ID", example = "101")
    Long reflectionId,

    @Schema(description = "회고 제목", example = "1주차 스터디를 돌아보며")
    String title,

    @Schema(description = "작성자 닉네임", example = "파도타기")
    String authorName,

    @Schema(description = "연관된 스터디 일정 제목 (없을 경우 null)", example = "1주차 정기 회의", nullable = true)
    String scheduleTitle,

    @JsonProperty("updated_at")
    @Schema(name = "updated_at", description = "마지막 수정일", example = "2025-09-25T13:00:00")
    LocalDateTime updatedAt
) {

}