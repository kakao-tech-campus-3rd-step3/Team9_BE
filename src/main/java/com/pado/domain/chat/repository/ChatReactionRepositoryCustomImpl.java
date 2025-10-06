package com.pado.domain.chat.repository;

import com.pado.domain.chat.dto.response.ChatReactionCountDto;
import com.pado.domain.chat.entity.ReactionType;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.pado.domain.chat.entity.QChatReaction.chatReaction;

@Repository
@RequiredArgsConstructor
public class ChatReactionRepositoryCustomImpl implements ChatReactionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatReactionCountDto> findReactionCountsByMessageIdIn(List<Long> messageIds) {
        if (messageIds == null || messageIds.isEmpty()) {
            return List.of();
        }

        return queryFactory
                .select(Projections.constructor(
                        ChatReactionCountDto.class,
                        chatReaction.chatMessage.id,
                        new CaseBuilder()
                                .when(chatReaction.reactionType.eq(ReactionType.LIKE))
                                .then(1L)
                                .otherwise(0L)
                                .sum(),
                        new CaseBuilder()
                                .when(chatReaction.reactionType.eq(ReactionType.DISLIKE))
                                .then(1L)
                                .otherwise(0L)
                                .sum()
                ))
                .from(chatReaction)
                .where(chatReaction.chatMessage.id.in(messageIds))
                .groupBy(chatReaction.chatMessage.id)
                .fetch();
    }
}
