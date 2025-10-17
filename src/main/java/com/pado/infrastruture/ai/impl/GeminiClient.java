package com.pado.infrastruture.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.pado.infrastruture.ai.GeminiProperties;
import com.pado.infrastruture.ai.dto.AiQuestionDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.concurrent.Executor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<AiQuestionDto> generateSingleQuestion(String context, String hint, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            Objects.requireNonNull(context, "Context cannot be null");

            final var prompt = buildSingleQuestionPrompt(context, hint);
            final var content = Content.fromParts(Part.fromText(prompt));
            final var config = buildSingleQuestionGenerationConfig();

            log.info("Sending single-question request. Context Length: {}, Hint: '{}'", context.length(), hint);
            try {
                GenerateContentResponse response = client.models.generateContent(modelName, content, config);
                if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
                    handleNonStopFinishReason(response);
                }

                log.info("Gemini quiz generation completed successfully");
                return objectMapper.readValue(response.text(), AiQuestionDto.class);

            } catch (IOException e) {
                log.error("Failed to parse Gemini JSON response.", e);
                throw new RuntimeException("Failed to parse LLM response", e);
            } catch (Exception e) {
                log.error("Failed to generate a single question. Context length: {}", context.length(), e);
                throw new RuntimeException("Failed to generate content", e);
            }
        }, executor);
    }

    private String buildSingleQuestionPrompt(String context, String hint) {
        String hintSection = (hint != null && !hint.isBlank())
                ? "\n### 집중 영역 ###\n" + hint + "\n"
                : "";

        return """
                너는 한국의 전문 퀴즈 출제자이며, 학생이 이해하기 쉽고 정확한 학습용 퀴즈만 제작해야 한다.
                학습용 퀴즈를 자연스러운 한국어로 만들며, 문제와 해설은 학생이 이해하기 쉽게 작성한다.
                주어진 텍스트 전체를 참고하되, '집중 영역'이 주어지면 해당 부분을 중심으로 단 하나의 학습용 퀴즈를 생성해라.

            ### 지침 ###
            1. 응답은 반드시 JSON 형식이어야 하며, 다른 부가 설명은 절대 포함하지 마. 각 문제 객체의 키 이름과 구조는 변경하지 말고, 반드시 지정된 JSON 형식으로 반환해야 한다.
            2. `explanation` 필드는 문제와 정답에 대한 상세한 해설이며, 존댓말로 작성한다.
            3. 문제 유형은 MULTIPLE_CHOICE 또는 SHORT_ANSWER 중 하나를 랜덤으로 선택하라.
            4. 객관식 문제(`MULTIPLE_CHOICE`)의 정답은 반드시 하나여야 해. 객관식 문제의 선택지는 2~4개로 제한한다.
            5. 주관식 문제(`SHORT_ANSWER`)의 정답(`sampleAnswer`)은 반드시 하나의 단어여야 해. 답변이 영어일 경우에는 문제(`questionText`)에 명시해.
            
            %s
            
            ### 기반 텍스트 ###
            ---
            %s
            ---
            """.formatted(hintSection, context);
    }

    private GenerateContentConfig buildSingleQuestionGenerationConfig() {
        final var commonRequired = ImmutableList.of("questionType", "questionText", "explanation");
        final var multipleChoiceSchema = buildMultipleChoiceSchema(commonRequired);
        final var shortAnswerSchema = buildShortAnswerSchema(commonRequired);

        final var finalSchema = ImmutableMap.of("oneOf", List.of(multipleChoiceSchema, shortAnswerSchema));

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