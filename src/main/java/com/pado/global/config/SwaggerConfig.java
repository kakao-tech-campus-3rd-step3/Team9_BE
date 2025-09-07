package com.pado.global.config;

import com.pado.global.exception.common.ErrorCode;
import com.pado.global.exception.dto.ErrorResponseDto;
import com.pado.global.swagger.annotation.common.NoApi409Conflict;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
            addInternalServerErrorResponse(operation);

            if (isAuthRequired(operation)) {
                addUnauthorizedResponse(operation);
            }

            if (hasRequestBody(handlerMethod)) {
                addBadRequestResponse(operation, "/api/some-endpoint");
            }

            if (isPotentiallyCreatingOrUpdating(handlerMethod)
                    && !handlerMethod.hasMethodAnnotation(NoApi409Conflict.class)
                    && !operation.getResponses().containsKey("409")) {
                addConflictResponse(operation);
            }

            return operation;
        };
    }

    private void addInternalServerErrorResponse(Operation operation) {
        ErrorResponseDto errorDto = ErrorResponseDto.of(
                ErrorCode.INTERNAL_ERROR,
                ErrorCode.INTERNAL_ERROR.message,
                Collections.emptyList(),
                "/api/endpoint"
        );
        ApiResponse apiResponse = createSingleExampleApiResponse("서버 내부 오류", errorDto);
        addApiResponse(operation, "500", apiResponse);
    }

    private void addUnauthorizedResponse(Operation operation) {
        ErrorResponseDto errorDto = ErrorResponseDto.of(
                ErrorCode.UNAUTHENTICATED_USER,
                ErrorCode.UNAUTHENTICATED_USER.message,
                Collections.emptyList(),
                "/api/secured-endpoint"
        );
        ApiResponse apiResponse = createSingleExampleApiResponse("인증 실패", errorDto);
        addApiResponse(operation, "401", apiResponse);
    }

    private void addBadRequestResponse(Operation operation, String examplePath) {
        ErrorResponseDto validationErrorDto = ErrorResponseDto.of(
                ErrorCode.INVALID_INPUT,
                ErrorCode.INVALID_INPUT.message,
                List.of("field_name: 올바른 형식이 아닙니다."),
                examplePath
        );

        ErrorResponseDto jsonParseErrorDto = ErrorResponseDto.of(
                ErrorCode.JSON_PARSE_ERROR,
                ErrorCode.JSON_PARSE_ERROR.message,
                Collections.emptyList(),
                examplePath
        );

        Map<String, Example> examples = Map.of(
                "Validation Error", new Example().value(validationErrorDto).summary("유효성 검사 실패"),
                "JSON Parse Error", new Example().value(jsonParseErrorDto).summary("JSON 파싱 실패")
        );

        ApiResponse apiResponse = createMultipleExampleApiResponse("잘못된 요청", examples);
        addApiResponse(operation, "400", apiResponse);
    }

    private void addConflictResponse(Operation operation) {
        ErrorResponseDto errorDto = ErrorResponseDto.of(ErrorCode.DUPLICATE_KEY, ErrorCode.DUPLICATE_KEY.message, Collections.emptyList(), "/api/some-resource");
        ApiResponse apiResponse = createSingleExampleApiResponse("중복 데이터", errorDto);
        addApiResponse(operation, "409", apiResponse);
    }

    private ApiResponse createSingleExampleApiResponse(String description, Object example) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new Schema<>().$ref("ErrorResponseDto"));
        mediaType.addExamples(description, new Example().value(example).summary(description));

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }

    private ApiResponse createMultipleExampleApiResponse(String description, Map<String, Example> examples) {
        MediaType mediaType = new MediaType();
        mediaType.setSchema(new Schema<>().$ref("ErrorResponseDto"));
        examples.forEach(mediaType::addExamples);

        return new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", mediaType));
    }

    private void addApiResponse(Operation operation, String code, ApiResponse apiResponse) {
        operation.getResponses().addApiResponse(code, apiResponse);
    }

    private boolean hasRequestBody(HandlerMethod handlerMethod) {
        return Arrays.stream(handlerMethod.getMethodParameters())
                .anyMatch(p -> p.hasParameterAnnotation(RequestBody.class));
    }

    private boolean isAuthRequired(Operation operation) {
        return operation.getSecurity() != null && !operation.getSecurity().isEmpty();
    }

    private boolean isPotentiallyCreatingOrUpdating(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(PostMapping.class)
                || handlerMethod.hasMethodAnnotation(PutMapping.class)
                || handlerMethod.hasMethodAnnotation(PatchMapping.class);
    }
}
