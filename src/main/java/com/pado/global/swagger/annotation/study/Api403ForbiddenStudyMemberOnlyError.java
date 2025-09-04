package com.pado.global.swagger.annotation.study;

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
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "접근 권한 없음 (스터디 멤버가 아닌 경우)", content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "권한 없음 예시",
                        value = "{\"error_code\": \"FORBIDDEN_STUDY_MEMBER_ONLY\", \"message\": \"스터디 멤버만 접근할 수 있습니다.\"}"
                )
        ))
})
public @interface Api403ForbiddenStudyMemberOnlyError {
}
