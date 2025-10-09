package com.pado.infrastruture.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.pado.infrastruture.ai.GeminiProperties;
import com.pado.infrastruture.ai.dto.AiQuizResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
public class GeminiClient {

    private final Client client;
    private final String modelName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiClient(GeminiProperties geminiProperties) {
        HttpOptions httpOptions = HttpOptions.builder()
                .timeout(180)
                .build();

        this.client = Client.builder()
                .apiKey(geminiProperties.getApiKey())
                .httpOptions(httpOptions)
                .build();

        this.modelName = geminiProperties.getModelName();
    }

    public AiQuizResponseDto generateQuiz(String context) {
        Objects.requireNonNull(context, "Context cannot be null");

        final var prompt = buildPrompt(context);
        final var content = Content.fromParts(Part.fromText(prompt));
        final var config = buildGenerationConfig();

        log.info("Sending request to Gemini SDK. Model: {}, Context Length: {} chars", modelName, context.length());

        try {
            GenerateContentResponse response = client.models.generateContent(modelName, content, config);

            if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
                handleNonStopFinishReason(response);
            }

            log.info("Gemini quiz generation completed successfully");
            return objectMapper.readValue(response.text(), AiQuizResponseDto.class);

        } catch (IOException e) {
            log.error("Failed to parse Gemini JSON response.", e);
            throw new RuntimeException("Failed to parse LLM response", e);
        } catch (Exception e) {
            log.error("Failed to generate content from Gemini.", e);
            throw new RuntimeException("Failed to generate content", e);
        }
    }

    private String buildPrompt(String context) {
        return """
            너는 한국의 전문 퀴즈 출제자이며, 학생이 이해하기 쉽고 정확한 학습용 퀴즈만 제작해야 한다.
            학습용 퀴즈를 한국어로 만들며, 문제와 해설은 학생이 이해하기 쉽게 작성한다.

            ### 지침 ###
            1. 응답은 반드시 JSON 형식이어야 하며, 다른 부가 설명은 절대 포함하지 마. 각 문제 객체의 키 이름과 구조는 변경하지 말고, 반드시 지정된 JSON 형식으로 반환해야 한다.
            2. 전체 문항 수는 5~10개로 제한한다.
            3. 각 문제 객체에는 `explanation` 필드가 반드시 포함되어야 하며, 해설은 존댓말로 작성한다.
            4. `explanation` 필드는 문제와 정답에 대한 상세한 해설이며, 비워두거나 생략하면 안 돼.
            5. 모든 내용은 반드시 한국어로 작성해.
            6. 객관식 문제(`MULTIPLE_CHOICE`)의 정답은 반드시 하나여야 해. 객관식 문제의 선택지는 2~4개로 제한한다.
            7. 주관식 문제(`SHORT_ANSWER`)의 정답(`sampleAnswer`)은 반드시 하나의 단어여야 해. 답변이 영어일 경우에는 문제(`questionText`)에 명시해.
            8. 최상위 객체에는 `recommendedTimeLimitSeconds` 필드를 포함해야 하며, 전체 문제를 푸는 데 적절한 시간을 초 단위로 제시한다.

            ### 기반 텍스트 ###
            ---
            %s
            ---
            """.formatted(context);
    }

    private GenerateContentConfig buildGenerationConfig() {
        final var commonRequired = ImmutableList.of("questionType", "questionText", "explanation");

        final var multipleChoiceSchema = buildMultipleChoiceSchema(commonRequired);
        final var shortAnswerSchema = buildShortAnswerSchema(commonRequired);

        final var finalSchema = ImmutableMap.of(
                "type", "object",
                "properties", ImmutableMap.of(
                        "recommendedTimeLimitSeconds", ImmutableMap.of("type", "integer", "description", "퀴즈 전체 권장 시간(초)"),
                        "questions", ImmutableMap.of(
                                "type", "array",
                                "items", ImmutableMap.of("oneOf", List.of(multipleChoiceSchema, shortAnswerSchema))
                        )
                ),
                "required", List.of("questions")
        );

        return GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseJsonSchema(finalSchema)
                .build();
    }

    private Map<String, Object> buildMultipleChoiceSchema(List<String> commonRequired) {
        return ImmutableMap.of(
                "type", "object",
                "properties", ImmutableMap.of(
                        "questionType", ImmutableMap.of("const", "MULTIPLE_CHOICE"),
                        "questionText", ImmutableMap.of("type", "string"),
                        "explanation", ImmutableMap.of("type", "string", "description", "문제에 대한 상세한 해설"),
                        "options", ImmutableMap.of("type", "array", "items", ImmutableMap.of("type", "string"), "minItems", 2, "maxItems", 4),
                        "correctAnswerIndex", ImmutableMap.of("type", "integer")
                ),
                "required", ImmutableList.<String>builder()
                        .addAll(commonRequired)
                        .add("options", "correctAnswerIndex")
                        .build()
        );
    }

    private Map<String, Object> buildShortAnswerSchema(List<String> commonRequired) {
        return ImmutableMap.of(
                "type", "object",
                "properties", ImmutableMap.of(
                        "questionType", ImmutableMap.of("const", "SHORT_ANSWER"),
                        "questionText", ImmutableMap.of("type", "string"),
                        "explanation", ImmutableMap.of("type", "string", "description", "문제에 대한 상세한 해설"),
                        "sampleAnswer", ImmutableMap.of("type", "string", "maxLength", 20)
                ),
                "required", ImmutableList.<String>builder()
                        .addAll(commonRequired)
                        .add("sampleAnswer")
                        .build()
        );
    }

    private void handleNonStopFinishReason(GenerateContentResponse response) {
        String reason = "Unknown finish reason: " + response.finishReason();
        if (response.finishReason().knownEnum() != null) {
            reason = response.finishReason().knownEnum().name();
        }
        log.error("Gemini generation finished for a non-STOP reason: {}", reason);
        throw new RuntimeException("AI content generation failed: " + reason);
    }
}