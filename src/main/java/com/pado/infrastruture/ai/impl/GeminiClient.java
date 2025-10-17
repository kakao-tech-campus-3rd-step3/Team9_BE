package com.pado.infrastruture.ai.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.genai.Client;
import com.google.genai.types.*;
import com.pado.domain.quiz.entity.QuestionType;
import com.pado.infrastruture.ai.GeminiProperties;
import com.pado.infrastruture.ai.dto.AiQuizResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class GeminiClient {

    private final Client client;
    private final String modelName;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public GeminiClient(GeminiProperties geminiProperties) {
        this.client = new Client.Builder().apiKey(geminiProperties.getApiKey()).build();
        this.modelName = geminiProperties.getModelName();
    }

    public AiQuizResponseDto generateQuiz(String context) {
        Objects.requireNonNull(context, "Context cannot be null");

        String prompt = createPrompt(context);
        Content content = Content.fromParts(Part.fromText(prompt));
        GenerateContentConfig config = createGenerationConfig();

        log.info("Sending request to Gemini SDK. Model: {}, Context Length: {} chars", modelName, context.length());

        try {
            GenerateContentResponse response = client.models.generateContent(modelName, content, config);

            if (response.finishReason().knownEnum() != FinishReason.Known.STOP) {
                handleNonStopFinishReason(response);
            }

            return objectMapper.readValue(response.text(), AiQuizResponseDto.class);

        } catch (IOException e) {
            log.error("Failed to parse Gemini JSON response.", e);
            throw new RuntimeException("Failed to parse LLM response", e);
        } catch (Exception e) {
            log.error("Failed to generate content from Gemini.", e);
            throw new RuntimeException("Failed to generate content", e);
        }
    }

    private String createPrompt(String context) {
        return String.format("Generate a quiz in Korean based on the following context:\n\n---\n%s\n---", context);
    }

    private GenerateContentConfig createGenerationConfig() {
        ImmutableMap<String, Object> schema =
                ImmutableMap.of(
                        "type", "object",
                        "properties", ImmutableMap.of(
                                "recommendedTimeLimitSeconds", ImmutableMap.of("type", "integer"),
                                "questions", ImmutableMap.of(
                                        "type", "array",
                                        "items", ImmutableMap.of(
                                                "type", "object",
                                                "properties", ImmutableMap.of(
                                                        "questionType", ImmutableMap.of(
                                                                "type", "string",
                                                                "enum", Arrays.stream(QuestionType.values()).map(Enum::name).collect(Collectors.toList())
                                                        ),
                                                        "questionText", ImmutableMap.of("type", "string"),
                                                        "options", ImmutableMap.of(
                                                                "type", "array",
                                                                "items", ImmutableMap.of("type", "string")
                                                        ),
                                                        "correctAnswerIndex", ImmutableMap.of("type", "integer"),
                                                        "sampleAnswer", ImmutableMap.of("type", "string")
                                                ),
                                                "required", ImmutableList.of("questionType", "questionText")
                                        )
                                )
                        )
                );

        return GenerateContentConfig.builder()
                .responseMimeType("application/json")
                .responseJsonSchema(schema)
                .build();
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