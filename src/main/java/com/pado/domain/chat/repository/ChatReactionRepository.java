package com.pado.domain.chat.repository;

import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.ChatReaction;
import com.pado.domain.chat.entity.ReactionType;
import com.pado.domain.study.entity.StudyMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatReactionRepository extends JpaRepository<ChatReaction, Long> {

    Optional<ChatReaction> findByChatMessageAndStudyMember(ChatMessage chatMessage, StudyMember studyMember);

    long countByChatMessageAndReactionType(ChatMessage chatMessage, ReactionType reactionType);
}
