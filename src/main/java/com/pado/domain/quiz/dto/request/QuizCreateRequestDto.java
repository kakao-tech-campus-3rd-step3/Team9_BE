package com.pado.domain.quiz.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "퀴즈 생성 요청 DTO")
public record QuizCreateRequestDto(

        @Schema(description = "퀴즈 제목", example = "자바 기초 1주차 퀴즈")
        @NotBlank(message = "퀴즈 제목은 필수입니다.")
        @Size(max = 255, message = "퀴즈 제목은 255자를 초과할 수 없습니다.")
        String title,

        @JsonProperty("file_ids")
        @Schema(description = "퀴즈 생성에 사용할 파일 ID 목록", example = "[1, 2, 3]")
        @NotEmpty(message = "퀴즈를 생성할 파일을 최소 1개 이상 선택해야 합니다.")
        List<Long> fileIds
) {

}