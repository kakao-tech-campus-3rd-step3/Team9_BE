package com.pado.global.exception.dto;

import com.pado.global.exception.common.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponseDto(
        String code,
        String message,
        List<String> errors,
        LocalDateTime timestamp,
        String path
) {
    public static ErrorResponseDto of (ErrorCode code, String message, List<String> errors, String path) {
        return new ErrorResponseDto(
                code.code,
                message != null ? message : code.message,
                errors,
                LocalDateTime.now(),
                path
        );
    }
}
