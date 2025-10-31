package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "스터디 신청자 상세 정보 DTO")
public record StudyApplicantDetailDto(
    @Schema(description = "스터디 신청 ID", example = "123")
    Long applicationId,

    @Schema(description = "신청자 닉네임", example = "신청자1닉네임")
    String nickname,

    @Schema(description = "신청 메시지", example = "열심히 참여하겠습니다!")
    String applicationMessage,

    @Schema(description = "신청자 세부 정보")
    UserDetailDto userDetail,

    @Schema(description = "신청 일시", example = "2025-10-21T01:00:00")
    LocalDateTime appliedAt
) {

}