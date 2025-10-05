package com.pado.domain.chat.dto.response;

import com.pado.domain.chat.entity.UpdateType;
import io.swagger.v3.oas.annotations.media.Schema;

public record UpdatedChatRoomResponseDto(
        @Schema(description = "업데이트 정보 타입", example = "imoji, delated")
        UpdateType type,

        @Schema(description = "메세지 id", example = "1")
        Long messageId,

        @Schema(description = "좋아요 수(타입이 deleted면 null)", example = "1")
        Long likeCount,

        @Schema(description = "싫어요 수(타입이 deleted면 null)", example = "1")
        Long dislikeCount
) {
}
