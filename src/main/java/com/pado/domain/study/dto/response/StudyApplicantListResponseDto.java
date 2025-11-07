package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "스터디 신청자 목록 조회 응답 DTO")
public record StudyApplicantListResponseDto(
    @Schema(description = "스터디 신청자 정보 리스트")
    List<StudyApplicantDetailDto> applicants
) {

}