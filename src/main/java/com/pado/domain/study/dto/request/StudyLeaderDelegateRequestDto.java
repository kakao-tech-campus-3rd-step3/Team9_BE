package com.pado.domain.study.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디 리더 위임 요청 DTO")
public record StudyLeaderDelegateRequestDto(
    @Schema(description = "새로운 리더로 지정할 스터디 멤버의 ID", example = "2")
    @NotNull(message = "새로운 리더의 ID는 필수입니다.")
    Long newLeaderMemberId
) {

}