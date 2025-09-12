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
@ApiResponses(@ApiResponse(responseCode = "404", description = "스터디 또는 스터디원을 찾을 수 없음",
        content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = {
                        @ExampleObject(name = "스터디원 미존재 예시",
                                value = """
                                        {
                                          "code": "MEMBER_NOT_FOUND",
                                          "message": "멤버를 찾을 수 없습니다.",
                                          "errors": [],
                                          "timestamp": "22025-09-07T09:28:57.5088742",
                                          "path": "/api/your-endpoint"
                                        }
                                        """
                        ),
                        @ExampleObject(name = "스터디 미존재 예시",
                                value = """
                                        {
                                          "code": "STUDY_NOT_FOUND",
                                          "message": "스터디를 찾을 수 없습니다.",
                                          "errors": null,
                                          "timestamp": "2025-09-07T08:15:30.123Z",
                                          "path": "/api/your-endpoint"
                                        }
                                        """
                        )
                })
))
public @interface Api404StudyOrMemberNotFoundError {
}
