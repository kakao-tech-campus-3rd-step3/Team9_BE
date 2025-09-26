package com.pado.domain.study.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "사용자 세부 정보 DTO")
public record UserDetailDto(
        @Schema(description = "스터디 대표 이미지 파일 키 (S3에 저장된 객체 경로)", example = "study/12345/main.png")
        String file_key,

        @Schema(description = "성별", example = "Men", allowableValues = {"Men", "Women"})
        String gender,

        @Schema(description = "관심 분야", example = "[\"프로그래밍\", \"취업\"]")
        List<String> interests,

        @Schema(description = "지역", example = "서울")
        String location
) {}
