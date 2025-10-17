package com.pado.domain.chat.dto.response;

public record UpdatedMessageUnreadCountDto (
        Long messageId,
        Long unreadCount
) {}
