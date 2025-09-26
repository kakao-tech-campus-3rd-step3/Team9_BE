package com.pado.domain.study.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디 신청한 유저의 상태 변경 요청 DTO")
public record StudyApplicationStatusChangeRequestDto(
        @Schema(description = "신청한 유저의 상태", example = "Pending", allowableValues = {"Pending", "APPROVED", "REJECTED"})
        @NotNull(message = "상태는 필수 입력 항목입니다.")
        String status
) {
}
