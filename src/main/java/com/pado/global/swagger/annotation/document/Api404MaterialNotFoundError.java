package com.pado.global.swagger.annotation.document;

import com.pado.global.exception.dto.ErrorResponseDto;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(@ApiResponse(responseCode = "404", description = "자료를 찾을 수 없음",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "자료 미존재 예시",
                        value = "{\"error_code\": \"MATERIAL_NOT_FOUND\", \"message\": \"해당 자료를 찾을 수 없습니다.\"}"
                ))))
public @interface Api404MaterialNotFoundError {
}

