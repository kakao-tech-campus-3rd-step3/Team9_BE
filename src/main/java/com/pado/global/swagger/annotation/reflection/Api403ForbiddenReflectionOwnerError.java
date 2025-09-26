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
@ApiResponse(responseCode = "403", description = "접근 권한 없음 (회고 작성자가 아닌 경우)",
    content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
            name = "권한 없음 예시",
            value = """
                {
                  "code": "FORBIDDEN_REFLECTION_OWNER_ONLY",
                  "message": "본인만 수정/삭제할 수 있습니다.",
                  "errors": [],
                  "timestamp": "2025-09-07T09:28:57.508Z",
                  "path": "/api/reflections/1"
                }
                """
        )
    )
)
public @interface Api403ForbiddenReflectionOwnerError {

}