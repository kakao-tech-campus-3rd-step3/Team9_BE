package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "스터디 랭킹 조회 응답 DTO")
public record StudyRankingResponseDto(
        @Schema(description = "랭킹 순으로 정렬된 사용자 닉네임 목록", example = "[\"랭커1\", \"랭커2\", \"랭커3\"]")
        List<String> user_nicknames
) {}