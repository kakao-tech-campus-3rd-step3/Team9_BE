package com.pado.domain.chat.service;

import com.pado.domain.chat.dto.request.ChatMessageRequestDto;
import com.pado.domain.chat.dto.request.ReactionRequestDto;
import com.pado.domain.chat.dto.response.ChatMessageListResponseDto;
import com.pado.domain.chat.dto.response.ChatMessageResponseDto;
import com.pado.domain.chat.dto.response.ChatReactionCountDto;
import com.pado.domain.chat.dto.response.UpdatedChatRoomResponseDto;
import com.pado.domain.chat.entity.*;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.chat.repository.ChatReactionRepository;
import com.pado.domain.chat.repository.LastReadMessageRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @InjectMocks
    private ChatServiceImpl chatService;

    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private LastReadMessageRepository lastReadRepository;
    @Mock
    private RedisChatModalManager modalManager;
    @Mock
    private SimpMessagingTemplate messagingTemplate;
    @Mock
    private ChatReactionRepository chatReactionRepository;

    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_STUDY_ID = 1L;
    private static final Long TEST_MESSAGE_ID = 1L;
    private static final Long TEST_STUDY_MEMBER_ID = 1L;

    private User user;
    private Study study;
    private StudyMember studyMember;
    private ChatMessage chatMessage;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@test.com")
                .nickname("test")
                .build();
        ReflectionTestUtils.setField(user, "id", TEST_USER_ID);

        study = Study.builder()
                .leader(user)
                .title("test")
                .build();
        ReflectionTestUtils.setField(study, "id", TEST_STUDY_ID);

        studyMember = StudyMember.builder()
                .study(study)
                .user(user)
                .build();
        ReflectionTestUtils.setField(studyMember, "id", TEST_STUDY_MEMBER_ID);

        chatMessage = ChatMessage.builder()
                .study(study)
                .sender(studyMember)
                .content("test message")
                .type(MessageType.CHAT)
                .build();
        ReflectionTestUtils.setField(chatMessage, "id", TEST_MESSAGE_ID);
    }

    @Nested
    class 메시지_전송_테스트 {

        private ChatMessageRequestDto requestDto;

        @BeforeEach
        void setUp() {
            lenient().when(studyRepository.findById(TEST_STUDY_ID)).thenReturn(Optional.of(study));
            lenient().when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID))
                    .thenReturn(Optional.of(studyMember));
        }

        @Test
        void 정상적인_메세지_전송() {
            // given
            requestDto = new ChatMessageRequestDto("안녕하세요!");
            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
            when(modalManager.getOpenModalUserIds(TEST_STUDY_ID)).thenReturn(new HashSet<>());
            when(lastReadRepository.countUnreadMembers(anyLong(), anyLong())).thenReturn(0L);
            when(chatReactionRepository.findReactionCountsByMessageIdIn(List.of(TEST_MESSAGE_ID)))
                    .thenReturn(List.of(new ChatReactionCountDto(TEST_MESSAGE_ID, 0L, 0L)));

            // when
            chatService.sendMessage(TEST_STUDY_ID, requestDto, user);

            // then
            ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
            verify(chatMessageRepository, times(1)).save(messageCaptor.capture());
            ChatMessage savedMessage = messageCaptor.getValue();
            assertThat(savedMessage.getContent()).isEqualTo("안녕하세요!");
            assertThat(savedMessage.getSender()).isEqualTo(studyMember);

            ArgumentCaptor<ChatMessageResponseDto> dtoCaptor = ArgumentCaptor.forClass(ChatMessageResponseDto.class);
            verify(messagingTemplate, times(1))
                    .convertAndSend(eq("/topic/studies/1/chats"), dtoCaptor.capture());
            ChatMessageResponseDto sentDto = dtoCaptor.getValue();
            assertThat(sentDto.content()).isEqualTo("test message");
            assertThat(sentDto.senderName()).isEqualTo(user.getNickname());
        }

        @Test
        void 존재하지_않는_스터디에_메세지_전송() {
            // given
            requestDto = new ChatMessageRequestDto("안녕하세요!");
            when(studyRepository.findById(TEST_STUDY_ID)).thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatService.sendMessage(TEST_STUDY_ID, requestDto, user));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.STUDY_NOT_FOUND);
        }

        @Test
        void 스터디멤버가_아닌_경우의_메세지_전송() {
            // given
            requestDto = new ChatMessageRequestDto("안녕하세요!");
            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID))
                    .thenReturn(Optional.empty());

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatService.sendMessage(TEST_STUDY_ID, requestDto, user));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        @Test
        void 빈_내용의_메세지_전송() {
            // given
            requestDto = new ChatMessageRequestDto("");

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatService.sendMessage(TEST_STUDY_ID, requestDto, user));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
        }

        @Test
        void 채팅방에_접속한_사용자의_마지막_메세지_아이디_수신() {
            // given
            requestDto = new ChatMessageRequestDto("안녕하세요!");

            User user2 = User.builder().email("user2@test.com").nickname("유저2").build();
            ReflectionTestUtils.setField(user2, "id", 2L);
            StudyMember member2 = StudyMember.builder().study(study).user(user2).build();
            ReflectionTestUtils.setField(member2, "id", 2L);

            Set<Long> onlineUserIds = new HashSet<>(Arrays.asList(TEST_USER_ID, 2L));

            LastReadMessage lastRead1 = LastReadMessage.builder().studyMember(studyMember).lastReadMessageId(0L).build();
            LastReadMessage lastRead2 = LastReadMessage.builder().studyMember(member2).lastReadMessageId(0L).build();

            when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(chatMessage);
            when(modalManager.getOpenModalUserIds(TEST_STUDY_ID)).thenReturn(onlineUserIds);
            when(studyMemberRepository.findByStudyIdAndUserIdIn(TEST_STUDY_ID, onlineUserIds))
                    .thenReturn(Arrays.asList(studyMember, member2));
            when(lastReadRepository.findByStudyMemberIn(anyList()))
                    .thenReturn(Arrays.asList(lastRead1, lastRead2));
            when(lastReadRepository.countUnreadMembers(TEST_STUDY_ID, TEST_MESSAGE_ID)).thenReturn(0L);
            when(chatReactionRepository.findReactionCountsByMessageIdIn(List.of(TEST_MESSAGE_ID)))
                    .thenReturn(List.of(new ChatReactionCountDto(TEST_MESSAGE_ID, 0L, 0L)));

            // when
            chatService.sendMessage(TEST_STUDY_ID, requestDto, user);

            // then
            assertThat(lastRead1.getLastReadMessageId()).isEqualTo(TEST_MESSAGE_ID);
            assertThat(lastRead2.getLastReadMessageId()).isEqualTo(TEST_MESSAGE_ID);
        }
    }

    @Nested
    @DisplayName("채팅 메시지 조회 테스트")
    class 채팅_메세지_조회_테스트 {

        @BeforeEach
        void setUp() {
            when(studyRepository.existsById(TEST_STUDY_ID)).thenReturn(true);
        }

        @Test
        void 정상적인_메세지_조회() {
            // given
            when(chatMessageRepository.findChatMessagesWithCursor(TEST_STUDY_ID, null, 20))
                    .thenReturn(List.of(chatMessage));
            when(lastReadRepository.getUnreadCountsForMessages(eq(TEST_STUDY_ID), anyList()))
                    .thenReturn(Map.of(TEST_MESSAGE_ID, 0L));
            when(chatReactionRepository.findReactionCountsByMessageIdIn(anyList()))
                    .thenReturn(Collections.emptyList());
            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID)).thenReturn(Optional.of(studyMember));


            // when
            ChatMessageListResponseDto result = chatService.getChatMessages(TEST_STUDY_ID, null, 20, user);

            // then
            assertThat(result.messages()).hasSize(1);
            assertThat(result.messages().get(0).content()).isEqualTo("test message");
            assertThat(result.hasNext()).isFalse();
        }

        @Test
        void 스터디멤버가_아닌경우의_메세지목록_조회() {
            // given
            User otherUser = User.builder()
                    .email("other@test.com")
                    .nickname("다른사람")
                    .build();
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            StudyMember otherMember = StudyMember.builder()
                    .user(otherUser)
                    .build();

            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, 2L))
                    .thenReturn(Optional.of(otherMember));

            User originalUser = User.builder()
                    .email("owner@test.com")
                    .nickname("본인")
                    .build();
            ReflectionTestUtils.setField(originalUser, "id", 1L);

            StudyMember senderMember = StudyMember.builder()
                    .user(originalUser)
                    .build();

            ChatMessage message = ChatMessage.builder()
                    .sender(senderMember)
                    .type(MessageType.CHAT)
                    .content("안녕")
                    .build();
            ReflectionTestUtils.setField(message, "id", TEST_MESSAGE_ID);

            when(chatMessageRepository.findById(TEST_MESSAGE_ID))
                    .thenReturn(Optional.of(message));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatService.deleteChatMessage(TEST_STUDY_ID, TEST_MESSAGE_ID, otherUser));

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_MESSAGE);
        }
    }

    @Nested
    class 리액션_테스트 {

        private ReactionRequestDto request;
        private ChatReaction existingReaction;

        @BeforeEach
        void setUp() {
            when(studyRepository.existsById(TEST_STUDY_ID)).thenReturn(true);
            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID))
                    .thenReturn(Optional.of(studyMember));
            when(chatMessageRepository.findById(TEST_MESSAGE_ID)).thenReturn(Optional.of(chatMessage));

            existingReaction = ChatReaction.builder()
                    .chatMessage(chatMessage)
                    .studyMember(studyMember)
                    .reactionType(ReactionType.LIKE)
                    .build();
            ReflectionTestUtils.setField(existingReaction, "id", 1L);
        }

        @Test
        @DisplayName("생성 성공: 좋아요 리액션을 생성한다")
        void 정상적인_리액선_생성() {
            // given
            request = new ReactionRequestDto("LIKE");
            when(chatReactionRepository.findReactionCountsByMessageIdIn(List.of(TEST_MESSAGE_ID)))
                    .thenReturn(List.of(new ChatReactionCountDto(TEST_MESSAGE_ID, 1L, 0L)));

            // when
            chatService.createChatReaction(TEST_STUDY_ID, TEST_MESSAGE_ID, request, user);

            // then
            ArgumentCaptor<ChatReaction> reactionCaptor = ArgumentCaptor.forClass(ChatReaction.class);
            verify(chatReactionRepository).save(reactionCaptor.capture());
            assertThat(reactionCaptor.getValue().getReactionType()).isEqualTo(ReactionType.LIKE);
            verify(messagingTemplate).convertAndSend(eq("/topic/studies/1/updates"), any(UpdatedChatRoomResponseDto.class));
        }

        @Test
        void 정상적인_리액션_변경() {
            // given
            request = new ReactionRequestDto("DISLIKE");
            when(chatReactionRepository.findByChatMessageAndStudyMember(chatMessage, studyMember))
                    .thenReturn(Optional.of(existingReaction));
            when(chatReactionRepository.findReactionCountsByMessageIdIn(List.of(TEST_MESSAGE_ID)))
                    .thenReturn(List.of(new ChatReactionCountDto(TEST_MESSAGE_ID, 0L, 1L)));

            // when
            chatService.updateChatReaction(TEST_STUDY_ID, TEST_MESSAGE_ID, request, user);

            // then
            assertThat(existingReaction.getReactionType()).isEqualTo(ReactionType.DISLIKE);
            verify(messagingTemplate).convertAndSend(eq("/topic/studies/1/updates"), any(UpdatedChatRoomResponseDto.class));
        }

        @Test
        void 정상적인_리액션_삭제() {
            // given
            when(chatReactionRepository.findByChatMessageAndStudyMember(chatMessage, studyMember))
                    .thenReturn(Optional.of(existingReaction));
            when(chatReactionRepository.findReactionCountsByMessageIdIn(List.of(TEST_MESSAGE_ID)))
                    .thenReturn(List.of(new ChatReactionCountDto(TEST_MESSAGE_ID, 0L, 0L)));

            // when
            chatService.deleteChatReaction(TEST_STUDY_ID, TEST_MESSAGE_ID, user);

            // then
            verify(chatReactionRepository).delete(existingReaction);
            verify(messagingTemplate).convertAndSend(eq("/topic/studies/1/updates"), any(UpdatedChatRoomResponseDto.class));
        }
    }

    @Nested
    class 메세지_삭제_테스트 {

        @BeforeEach
        void setUp() {
            when(studyRepository.existsById(TEST_STUDY_ID)).thenReturn(true);
            when(chatMessageRepository.findById(TEST_MESSAGE_ID)).thenReturn(Optional.of(chatMessage));
        }

        @Test
        void 정상적인_메세지_삭제() {
            // given
            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID)).thenReturn(Optional.of(studyMember));

            // when
            chatService.deleteChatMessage(TEST_STUDY_ID, TEST_MESSAGE_ID, user);

            // then
            verify(chatMessageRepository).delete(chatMessage);
            verify(messagingTemplate).convertAndSend(eq("/topic/studies/1/updates"), any(Object.class));
        }

        @Test
        void 다른사람의_메세지를_삭제() {
            // given
            User otherUser = User.builder().email("other@test.com").nickname("다른사람").build();

            ReflectionTestUtils.setField(otherUser, "id", 2L);

            StudyMember otherMember = StudyMember.builder()
                    .user(otherUser)
                    .build();

            when(studyMemberRepository.findByStudyIdAndUserId(TEST_STUDY_ID, 2L)).thenReturn(Optional.of(otherMember));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                    () -> chatService.deleteChatMessage(TEST_STUDY_ID, TEST_MESSAGE_ID, otherUser));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_DELETE_MESSAGE);
        }
    }

    @Nested
    @DisplayName("채팅 모달 관리 테스트")
    class ChatModalTests {

        @BeforeEach
        void setUp() {
            when(studyRepository.existsById(TEST_STUDY_ID)).thenReturn(true);
            when(studyMemberRepository.existsByStudyIdAndUserId(TEST_STUDY_ID, TEST_USER_ID)).thenReturn(true);
        }
    }
}