package com.pado.domain.quiz.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record QuizCreateRequestDto(
        @NotBlank(message = "퀴즈 제목은 필수입니다.")
        @Size(max = 255, message = "퀴즈 제목은 255자를 초과할 수 없습니다.")
        String title,

        @NotEmpty(message = "퀴즈를 생성할 파일을 최소 1개 이상 선택해야 합니다.")
        List<Long> fileIds
) {

}