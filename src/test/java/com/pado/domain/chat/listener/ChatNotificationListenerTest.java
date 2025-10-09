package com.pado.domain.chat.listener;

import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.MessageType;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.material.event.NoticeCreatedEvent;
import com.pado.domain.schedule.event.ScheduleCreatedEvent;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatNotificationListenerTest {

    @InjectMocks
    private ChatNotificationListener chatNotificationListener;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private StudyRepository studyRepository;

    private Study study;
    private User leader;

    @BeforeEach
    void setUp() {
        leader = User.builder()
                .email("leader@test.com")
                .nickname("leader")
                .build();
        ReflectionTestUtils.setField(leader, "id", 1L);

        study = Study.builder()
                .leader(leader)
                .title("test study")
                .build();
        ReflectionTestUtils.setField(study, "id", 1L);
    }

    @Nested
    class 공지사항_생성_이벤트_처리_테스트 {

        @Test
        void 성공적인_공지_알림_메세지_전송() {
            // given
            NoticeCreatedEvent event = new NoticeCreatedEvent(1L, 10L, "중요 공지사항");
            
            ChatMessage savedMessage = ChatMessage.builder()
                    .study(study)
                    .sender(null)
                    .content("새로운 공지사항이 등록되었습니다: 중요 공지사항")
                    .type(MessageType.NOTICE)
                    .link("https://pado-6hybij4m8-sehighs-projects.vercel.app/study/document/10")
                    .build();
            ReflectionTestUtils.setField(savedMessage, "id", 100L);

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

            // when
            chatNotificationListener.handleNoticeCreatedEvent(event);

            // then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(chatMessageRepository).save(messageCaptor.capture());
            
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getType()).isEqualTo(MessageType.NOTICE);
            assertThat(capturedMessage.getContent()).contains("중요 공지사항");
            assertThat(capturedMessage.getSender()).isNull();

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/studies/1/chats"),
                    any(ChatMessageResponseDto.class)
            );
        }

        @Test
        void 스터디가_존재하지_않는_경우의_공지생성_이벤트_처리() {
            // given
            NoticeCreatedEvent event = new NoticeCreatedEvent(999L, 10L, "공지");
            when(studyRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatNotificationListener.handleNoticeCreatedEvent(event));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STUDY_NOT_FOUND);
        }
    }

    @Nested
    class 일정_생성_이벤트_처리_테스트 {

        @Test
        void 성공적인_일정생성_알림_메세지_전송() {
            // given
            ScheduleCreatedEvent event = new ScheduleCreatedEvent(1L, 20L, "정기 모임");
            
            ChatMessage savedMessage = ChatMessage.builder()
                    .study(study)
                    .sender(null)
                    .content("새로운 일정이 등록되었습니다: 정기 모임")
                    .type(MessageType.SCHEDULE)
                    .link("https://pado-6hybij4m8-sehighs-projects.vercel.app/study/schedule/1")
                    .build();
            ReflectionTestUtils.setField(savedMessage, "id", 200L);

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(savedMessage);

            // when
            chatNotificationListener.handleScheduleCreatedEvent(event);

            // then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(chatMessageRepository).save(messageCaptor.capture());
            
            ChatMessage capturedMessage = messageCaptor.getValue();
            assertThat(capturedMessage.getType()).isEqualTo(MessageType.SCHEDULE);
            assertThat(capturedMessage.getContent()).contains("정기 모임");
            assertThat(capturedMessage.getSender()).isNull();

            verify(messagingTemplate).convertAndSend(
                    eq("/topic/studies/1/chats"),
                    any(ChatMessageResponseDto.class)
            );
        }

        @Test
        void 스터디가_존재하지_않는_경우의_알림생성_이벤트_처리() {
            // given
            ScheduleCreatedEvent event = new ScheduleCreatedEvent(999L, 20L, "일정");
            when(studyRepository.findById(999L)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatNotificationListener.handleScheduleCreatedEvent(event));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STUDY_NOT_FOUND);
        }
    }
}
