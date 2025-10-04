package com.pado.domain.chat.entity;

import com.pado.domain.basetime.AuditingEntity;
import com.pado.domain.study.entity.StudyMember;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message_read")
public class LastReadMessage{
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_member_id", nullable = false)
    private StudyMember studyMember;
    
    @Column(name = "last_read_message_id", nullable = false)
    private Long lastReadMessageId;
    
    @Builder
    public LastReadMessage(StudyMember studyMember, Long lastReadMessageId) {
        this.studyMember = studyMember;
        this.lastReadMessageId = lastReadMessageId;
    }
    
    public void updateLastReadMessageId(Long messageId) {
        this.lastReadMessageId = messageId;
    }
}
