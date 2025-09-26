package com.pado.domain.reflection.dto;

import java.time.LocalDateTime;

public record ReflectionResponseDto(
    Long id,
    Long studyId,
    Long studyMemberId,
    Long scheduleId,
    Integer satisfactionScore,
    Integer understandingScore,
    Integer participationScore,
    String learnedContent,
    String improvement,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}