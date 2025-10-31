package com.pado.domain.chat.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message", indexes = {
        @Index(name = "idx_study_id", columnList = "study_id, id"),
        @Index(name = "idx_study_created_at", columnList = "study_id, created_at")
})
public class ChatMessage extends AuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private StudyMember sender;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(length = 500)
    private String link;

    @Builder
    public ChatMessage(Study study, StudyMember sender, String content, MessageType type, String link) {
        this.study = study;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.link = link;
    }
}
