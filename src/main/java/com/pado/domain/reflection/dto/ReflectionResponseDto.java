package com.pado.domain.reflection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "회고 정보 응답 DTO")
public record ReflectionResponseDto(
    @Schema(description = "회고 ID", example = "1")
    Long id,
    @Schema(description = "스터디 ID", example = "1")
    Long studyId,
    @Schema(description = "스터디 멤버 ID", example = "1")
    Long studyMemberId,
    @Schema(description = "연관된 스터디 일정 ID", example = "10")
    Long scheduleId,
    @Schema(description = "만족도 점수", example = "5")
    Integer satisfactionScore,
    @Schema(description = "이해도 점수", example = "4")
    Integer understandingScore,
    @Schema(description = "참여도 점수", example = "5")
    Integer participationScore,
    @Schema(description = "배운 내용", example = "JPA 영속성 컨텍스트에 대해 깊이 이해했습니다.")
    String learnedContent,
    @Schema(description = "개선할 점", example = "다음 스터디 전까지 관련 예제 코드를 직접 작성해봐야겠습니다.")
    String improvement,
    @Schema(description = "생성 시각", example = "2025-09-26T14:00:00")
    LocalDateTime createdAt,
    @Schema(description = "수정 시각", example = "2025-09-26T14:10:00")
    LocalDateTime updatedAt
) {

}