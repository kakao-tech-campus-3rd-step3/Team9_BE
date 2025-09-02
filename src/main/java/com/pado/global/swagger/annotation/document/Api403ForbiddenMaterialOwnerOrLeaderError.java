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
@ApiResponses(@ApiResponse(responseCode = "403", description = "접근 권한 없음", content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = ErrorResponseDto.class),
        examples = @ExampleObject(
                name = "권한 없음 예시",
                value = "{\"error_code\": \"FORBIDDEN_OWNER_OR_LEADER_ONLY\", \"message\": \"자료 작성자 또는 스터디 리더만 접근할 수 있습니다.\"}"
        ))))
public @interface Api403ForbiddenMaterialOwnerOrLeaderError {
}

