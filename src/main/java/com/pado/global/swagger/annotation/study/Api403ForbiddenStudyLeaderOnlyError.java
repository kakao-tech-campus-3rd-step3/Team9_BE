package com.pado.global.swagger.annotation.study;

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
@ApiResponse(responseCode = "400", description = "입력값 유효성 검증 실패",
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "최대 인원 검증 실패 예시",
                        value = """
                                {
                                  "code": "INVALID_MAX_MEMBERS",
                                  "message": "최대 멤버 수가 올바르지 않습니다.",
                                  "errors": [
                                    "max_members: 최대 인원은 2명 이상이어야 합니다."
                                  ],
                                  "timestamp": "2025-09-07T08:15:30.123Z",
                                  "path": "/api/study/create"
                                }
                                """
                )
        )
)
public @interface Api403ForbiddenStudyLeaderOnlyError {
}