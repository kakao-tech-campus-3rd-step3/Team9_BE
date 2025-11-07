package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "스터디원 상세 정보 DTO")
public record StudyMemberDetailDto(
    @Schema(description = "스터디 멤버 ID", example = "101")
    Long memberId,

    @Schema(description = "회원 닉네임", example = "파도타기")
    String nickname,

    @Schema(description = "스터디에서의 역할", example = "Leader", allowableValues = {"Leader", "Member",
        "Pending"})
    String role,

    @Schema(description = "참여 신청 메시지 (대기 중인 경우에만 존재)", example = "열심히 하겠습니다.")
    String message,

    @Schema(description = "사용자 세부 정보")
    UserDetailDto user_detail
) {

}