package com.pado.infrastruture.ai.dto;

import java.util.List;
public record GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
    public static GeminiRequest create(String text) {
        return new GeminiRequest(
                List.of(new Content(List.of(new Part(text)))),
                new GenerationConfig("application/json")
        );
    }
}