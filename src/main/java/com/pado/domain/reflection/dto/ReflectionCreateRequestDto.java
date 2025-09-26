package com.pado.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "회고 생성 및 수정 요청 DTO")
public record ReflectionCreateRequestDto(
    @Schema(description = "연관된 스터디 일정 ID (선택)", example = "10")
    Long scheduleId,

    @Schema(description = "스터디 만족도 점수 (1~5)", example = "5")
    @NotNull @Min(1) @Max(5)
    Integer satisfactionScore,

    @Schema(description = "스터디 이해도 점수 (1~5)", example = "4")
    @NotNull @Min(1) @Max(5)
    Integer understandingScore,

    @Schema(description = "스터디 참여도 점수 (1~5)", example = "5")
    @NotNull @Min(1) @Max(5)
    Integer participationScore,

    @Schema(description = "배운 내용", example = "JPA 영속성 컨텍스트에 대해 깊이 이해했습니다.")
    @NotBlank
    String learnedContent,

    @Schema(description = "개선할 점", example = "다음 스터디 전까지 관련 예제 코드를 직접 작성해봐야겠습니다.")
    @NotBlank
    String improvement
) {

}