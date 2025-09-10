package com.pado.domain.study.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "스터디원 역할 변경 요청 DTO")
public record StudyMemberRoleChangeRequestDto(
        @Schema(description = "변경할 스터디원의 역할", example = "Member", allowableValues = {"Leader", "Member", "Pending"})
        @NotNull(message = "역할은 필수 입력 항목입니다.")
        String role
) {}
