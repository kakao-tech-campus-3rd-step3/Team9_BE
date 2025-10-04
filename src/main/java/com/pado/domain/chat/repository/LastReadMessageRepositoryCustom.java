package com.pado.domain.chat.repository;

import java.util.List;
import java.util.Map;

public interface LastReadMessageRepositoryCustom {

    long countUnreadMembers(Long studyId, Long messageId);
    Map<Long, Long> getUnreadCountsForMessages(Long studyId, List<Long> messageIds);
}