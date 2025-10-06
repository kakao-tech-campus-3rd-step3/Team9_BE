package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(description = "내가 신청한 스터디 정보 응답 DTO")
@Builder
public record MyApplicationResponseDto(
    @Schema(description = "스터디 신청 ID", example = "1")
    Long applicationId,

    @Schema(description = "스터디 ID", example = "10")
    Long studyId,

    @Schema(description = "스터디 제목", example = "스프링 스터디")
    String studyTitle,

    @Schema(description = "신청 상태", example = "PENDING")
    String status,

    @Schema(description = "신청 시 보낸 메시지", example = "열심히 참여하겠습니다!")
    String message
) {

}