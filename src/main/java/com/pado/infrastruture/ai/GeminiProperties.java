package com.pado.infrastruture.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "gemini.api")
@Getter
@Setter
@Validated
public class GeminiProperties {
    @NotBlank
    private String apiKey;
    @NotBlank
    private String modelName = "gemini-2.5-flash";
}