package com.pado.domain.chat.dto.response;

public record ChatReactionCountDto(
   Long messageId,
   Long likeCount,
   Long dislikeCount
) {}
