package com.pado.global.swagger.annotation.user;

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
@ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "존재하지 않는 사용자 예시",
                        value = """
                                {
                                  "code": "USER_NOT_FOUND",
                                  "message": "사용자를 찾을 수 없습니다.",
                                  "errors": [],
                                  "timestamp": "2025-09-07T09:28:57.5088742",
                                  "path": "/api/users/1"
                                }
                                """
                )
        )
)
public @interface Api404UserNotFoundError {
}