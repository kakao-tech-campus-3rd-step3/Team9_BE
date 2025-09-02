package com.pado.global.swagger.annotation.schedule;

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
@ApiResponses(@ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "시작 시간 유효성 오류 예시",
                        value = "{\"error_code\": \"INVALID_START_TIME\", \"field\": \"start_time\", \"message\": \"시작 시간은 현재 시각보다 미래여야 합니다.\"}"
                ))))
public @interface Api400InvalidStartTimeError {
}
