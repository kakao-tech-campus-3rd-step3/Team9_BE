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
@ApiResponses({
        @ApiResponse(responseCode = "404", description = "일정을 찾을 수 없음", content = @Content(
                schema = @Schema(implementation = ErrorResponseDto.class),
                examples = @ExampleObject(
                        name = "존재하지 않는 일정",
                        value = "{\"error_code\": \"SCHEDULE_NOT_FOUND\", \"message\": \"해당 일정을 찾을 수 없습니다.\"}"
                )
        ))
})
public @interface Api404ScheduleNotFoundError {
}
