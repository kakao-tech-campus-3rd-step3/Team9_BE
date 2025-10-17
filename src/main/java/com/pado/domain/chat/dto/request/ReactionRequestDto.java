package com.pado.domain.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record ReactionRequestDto(

        @Schema(description = "좋아요 또는 싫어요",
                example = "[LIKE, DISLIKE]"
        )
        @NotBlank(message = "이모지는 필수입니다.")
        String reaction
) {
}
