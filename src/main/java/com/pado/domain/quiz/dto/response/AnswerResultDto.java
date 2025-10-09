package com.pado.domain.quiz.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pado.domain.quiz.entity.QuestionType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "문제별 답안 결과 DTO")
public record AnswerResultDto(
        @JsonProperty("question_id")
        @Schema(description = "문제 ID", example = "1")
        Long questionId,

        @JsonProperty("question_type")
        @Schema(description = "문제 유형: SHORT_ANSWER(주관식) 또는 MULTIPLE_CHOICE(객관식)")
        QuestionType questionType,

        @JsonProperty("question_text")
        @Schema(description = "문제 내용", example = "자바에서 상속을 지원하는 키워드는?")
        String questionText,

        @JsonProperty("is_correct")
        @Schema(description = "정답 여부", example = "true")
        boolean isCorrect,

        @JsonProperty("user_answer")
        @Schema(
                description = """
                          사용자가 선택한 답안.
                          - 객관식(MULTIPLE_CHOICE): 선택한 선지 ID를 문자열로 전달
                          - 주관식(SHORT_ANSWER): 사용자가 입력한 문자열 답안
                          """,
                example = "8"
        )
        String userAnswer,

        @JsonProperty("correct_answer")
        @Schema(description = "정답", example = "extends")
        String correctAnswer,

        @Schema(description = "해설", example = "자바에서는 extends 키워드를 사용하여 상속합니다.")
        String explanation,

        @Schema(description = "선택지 결과 목록")
        List<ChoiceResultDto> choices
) {

}