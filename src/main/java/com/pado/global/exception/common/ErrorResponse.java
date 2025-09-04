package com.pado.global.exception.common;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> errors,
        LocalDateTime timestamp,
        String path
) {
    public static ErrorResponse of (ErrorCode code, String message, List<String> errors, String path) {
        return new ErrorResponse(
                code.code,
                message != null ? message : code.message,
                errors,
                LocalDateTime.now(),
                path
        );
    }
}
