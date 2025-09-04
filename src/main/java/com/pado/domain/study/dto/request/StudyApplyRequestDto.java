package com.pado.domain.study.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디 참여 신청 요청 DTO")
public record StudyApplyRequestDto(
        @Schema(description = "스터디장에게 전하고 싶은 메시지 (선택 사항)", example = "열심히 공부해서 스터디에 기여하겠습니다!")
        String message
) {}