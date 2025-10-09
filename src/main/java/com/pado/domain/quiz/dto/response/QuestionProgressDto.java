package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "진행 중인 문제 DTO (객관식과 주관식 모두 사용)")
public record QuestionProgressDto(
        @JsonProperty("question_id")
        @Schema(description = "문제 ID", example = "1")
        Long questionId,

        @JsonProperty("question_type")
        @Schema(description = "문제 유형", example = "MULTIPLE_CHOICE")
        String questionType,

        @JsonProperty("question_text")
        @Schema(description = "문제 내용", example = "자바에서 상속을 지원하는 키워드는?")
        String questionText,

        @Schema(description = "선택지 목록")
        List<ChoiceDto> choices,

        @JsonProperty("user_answer")
        @Schema(
                description = """
                          사용자가 선택한 답안.
                          - 객관식(MULTIPLE_CHOICE): 선택한 선지 인덱스(0부터 시작)를 문자열로 전달
                          - 주관식(SHORT_ANSWER): 사용자가 입력한 문자열 답안
                          """,
                example = "0"
        )
        String userAnswer
) {

}