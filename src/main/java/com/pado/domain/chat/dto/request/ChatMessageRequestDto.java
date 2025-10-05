package com.pado.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequestDto(

        @Schema(description = "메세지 내용",
                example = "안녕"
        )
        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 1000, message = "메시지는 1000자를 초과할 수 없습니다.")
        String content
) {
}
