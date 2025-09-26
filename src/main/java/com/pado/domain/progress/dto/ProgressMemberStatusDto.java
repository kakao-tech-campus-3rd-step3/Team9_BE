package com.pado.domain.progress.dto;

import com.pado.domain.study.entity.StudyMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "개인별 현황판 스터디원 닉네임, 역할 DTO")
public record ProgressMemberStatusDto(
        @Schema(description = "맴버 닉네임", example = "파도")
        String nickname,
        @Schema(description = "스터디에서의 역할", example = "Leader", allowableValues = {"Leader", "Member", "Pending"})
        StudyMemberRole role,
        @Schema(description = "출석 횟수", example = "5")
        int attendance_count,
        @Schema(description = "퀴즈 횟수", example = "8")
        int quiz_count,
        @Schema(description = "회고 횟수", example = "3")
        int reflection_count
){
}
