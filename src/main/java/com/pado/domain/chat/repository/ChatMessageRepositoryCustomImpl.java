package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.QChatMessage;
import com.pado.domain.study.entity.QStudyMember;
import com.pado.domain.user.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryCustomImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findChatMessagesWithCursor(Long studyId, Long cursor, int size) {
        QChatMessage chatMessage = QChatMessage.chatMessage;
        QStudyMember studyMember = QStudyMember.studyMember;
        QUser user = QUser.user;

        return queryFactory
                .selectFrom(chatMessage)
                .join(chatMessage.sender, studyMember).fetchJoin()
                .join(studyMember.user, user).fetchJoin()
                .where(
                        chatMessage.study.id.eq(studyId),
                        cursorCondition(cursor)
                )
                .orderBy(chatMessage.id.desc())
                .limit(size + 1)
                .fetch();
    }

    private BooleanExpression cursorCondition(Long cursor) {
        QChatMessage chatMessage = QChatMessage.chatMessage;
        
        if (cursor == null) {
            return null;
        }
        return chatMessage.id.lt(cursor);
    }
}
