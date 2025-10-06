package com.pado.domain.chat.repository;

import com.pado.domain.chat.dto.response.ChatReactionCountDto;
import com.pado.domain.chat.entity.ChatMessage;

import java.util.List;

public interface ChatReactionRepositoryCustom {
    List<ChatReactionCountDto> findReactionCountsByMessageIdIn(List<Long> messageIds);

}
