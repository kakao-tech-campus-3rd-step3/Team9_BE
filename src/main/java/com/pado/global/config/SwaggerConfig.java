package com.pado.global.config;

import com.pado.global.exception.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@OpenAPIDefinition(
        info = @Info(title = "PADO API Documentation",
                description = "스터디 통합 플랫폼 PADO의 API 명세서입니다.",
                version = "v1.0.0"))
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER).name("Authorization");
        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        Server devServer = new Server()
                .url("https://gogumalatte.site")
                .description("Development Server (Internal Use Only)");

        Server localServer = new Server()
                .url("http://localhost:8080")
                .description("Local Server");

        return new OpenAPI()
                .servers(List.of(devServer, localServer))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme))
                .addSecurityItem(securityRequirement);
    }

    @Bean
    public OperationCustomizer globalResponseCustomizer() {
        return (operation, handlerMethod) -> {
            String serverErrorExample = "{\"error_code\": \"INTERNAL_SERVER_ERROR\", \"message\": \"서버 내부 오류가 발생했습니다.\"}";
            operation.getResponses().addApiResponse("500",
                    createApiResponse("서버 내부 오류", "서버 내부 오류 예시", serverErrorExample));

            SecurityRequirements securityRequirements = handlerMethod.getMethodAnnotation(SecurityRequirements.class);
            if (securityRequirements == null) {
                securityRequirements = handlerMethod.getBeanType().getAnnotation(SecurityRequirements.class);
            }

            boolean isAuthRequired = (securityRequirements == null);

            if (isAuthRequired) {
                String unauthorizedExample = "{\"error_code\": \"UNAUTHENTICATED_USER\", \"message\": \"인증되지 않은 사용자입니다.\"}";
                operation.getResponses().addApiResponse("401",
                        createApiResponse("인증 실패(유효하지 않은 토큰)", "인증 실패 예시", unauthorizedExample));
            }

            return operation;
        };
    }

    private ApiResponse createApiResponse(String description, String exampleName, String example) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new Schema<>().$ref(ErrorResponseDto.class.getSimpleName()));
        mediaType.addExamples(exampleName, new Example().value(example));

        return new ApiResponse().description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }
}

