package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.study.entity.Study;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {
    long countByIdGreaterThanAndStudyId(Long messageId, Long studyId);
    Optional<ChatMessage> findTopByStudyIdOrderByIdDesc(Long studyId);
    List<ChatMessage> findAllByIdGreaterThanAndStudyIdOrderByIdAsc(Long id, Long studyId);
}
