package com.pado.domain.chat.entity;

import com.pado.domain.basetime.CreatedAtEntity;
import com.pado.domain.study.entity.StudyMember;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_reaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ChatReaction extends CreatedAtEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    @JoinColumn(name = "chat_message_id", nullable = false)
    ChatMessage chatMessage;

    @ManyToOne
    @JoinColumn(name = "study_member_id", nullable = false)
    StudyMember studyMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type", nullable = false)
    ReactionType reactionType;

    @Builder
    public ChatReaction(ChatMessage chatMessage, StudyMember studyMember, ReactionType reactionType) {
        this.chatMessage = chatMessage;
        this.studyMember = studyMember;
        this.reactionType = reactionType;
    }

    public void changeReaction(ReactionType reactionType) {
        this.reactionType = reactionType;
    }
}
