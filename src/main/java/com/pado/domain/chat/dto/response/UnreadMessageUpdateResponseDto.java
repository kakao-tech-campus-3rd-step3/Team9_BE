package com.pado.domain.chat.dto.response;

import java.util.List;

public record UnreadMessageUpdateResponseDto(
    List<UpdatedMessageUnreadCountDto> updatedMessages
) {}
