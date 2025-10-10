package com.pado.global.swagger.annotation.reflection;

import com.pado.global.exception.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponse(responseCode = "404", description = "회고를 찾을 수 없음",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
            name = "존재하지 않는 회고 예시",
            value = """
                {
                  "code": "REFLECTION_NOT_FOUND",
                  "message": "회고를 찾을 수 없습니다.",
                  "errors": [],
                  "timestamp": "2025-09-07T09:28:57.508Z",
                  "path": "/api/reflections/999"
                }
                """
        )
    )
)
public @interface Api404ReflectionNotFoundError {

}