package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.LastReadMessage;
import com.pado.domain.chat.entity.QLastReadMessage;
import com.pado.domain.study.entity.QStudyMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class LastReadMessageRepositoryCustomImpl implements LastReadMessageRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public long countUnreadMembers(Long studyId, Long messageId) {
        QLastReadMessage lastReadMessage = QLastReadMessage.lastReadMessage;
        QStudyMember studyMember = QStudyMember.studyMember;

        Long count = queryFactory
                .select(lastReadMessage.count())
                .from(lastReadMessage)
                .join(lastReadMessage.studyMember, studyMember)
                .where(
                        studyMember.study.id.eq(studyId),
                        lastReadMessage.lastReadMessageId.lt(messageId)
                )
                .fetchOne();

        return count != null ? count : 0L;
    }


    @Override
    public Map<Long, Long> getUnreadCountsForMessages(Long studyId, List<Long> messageIds) {
        QLastReadMessage lastReadMessage = QLastReadMessage.lastReadMessage;
        QStudyMember studyMember = QStudyMember.studyMember;

        // 스터디의 모든 멤버의 LastReadMessage를 가져옴
        List<LastReadMessage> allReads = queryFactory
                .selectFrom(lastReadMessage)
                .join(lastReadMessage.studyMember, studyMember).fetchJoin()
                .where(studyMember.study.id.eq(studyId))
                .fetch();

        // Java에서 계산
        return messageIds.stream()
                .collect(Collectors.toMap(
                        messageId -> messageId,
                        messageId -> allReads.stream()
                                .filter(read -> read.getLastReadMessageId() < messageId)
                                .count()
                ));
    }
}
