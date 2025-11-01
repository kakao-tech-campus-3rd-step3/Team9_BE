package com.pado.domain.progress.service;

import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.progress.dto.ProgressChapterRequestDto;
import com.pado.domain.progress.dto.ProgressMemberStatusDto;
import com.pado.domain.progress.dto.ProgressRoadMapResponseDto;
import com.pado.domain.progress.dto.ProgressStatusResponseDto;
import com.pado.domain.progress.entity.Chapter;
import com.pado.domain.progress.repository.ChapterRepository;
import com.pado.domain.quiz.repository.QuizSubmissionRepository;
import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.entity.StudyStatus;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    @Mock ChapterRepository chapterRepository;
    @Mock StudyRepository studyRepository;
    @Mock StudyMemberRepository studyMemberRepository;
    @Mock AttendanceRepository attendanceRepository;
    @Mock ScheduleRepository scheduleRepository;
    @Mock QuizSubmissionRepository quizSubmissionRepository;
    @Mock ReflectionRepository reflectionRepository;

    @InjectMocks ProgressServiceImpl service;

    private Long studyId;
    private Long chapterId;
    private User userLeader;
    private User userMember;
    private Study study;

    @BeforeEach
    void setUp() {
        studyId = 1L;
        chapterId = 10L;

        userLeader = User.builder()
                .email("www.example1.com")
                .gender(Gender.FEMALE)
                .nickname("nickname1")
                .passwordHash("1111")
                .build();
        ReflectionTestUtils.setField(userLeader, "id", 100L);

        userMember = User.builder()
                .email("www.example2.com")
                .gender(Gender.FEMALE)
                .nickname("nickname2")
                .passwordHash("1111")
                .build();
        ReflectionTestUtils.setField(userMember, "id", 200L);

        study = Study.builder()
                .id(studyId)
                .title("제목")
                .leader(userLeader)
                .description("내용")
                .maxMembers(10)
                .region(Region.BUSAN)
                .status(StudyStatus.IN_PROGRESS)
                .build();
    }

    // ---------- getRoadMap ----------
    @Test
    void getRoadMap_멤버권한일때_로드맵DTO반환() {
        // given
        ProgressChapterRequestDto req = new ProgressChapterRequestDto("1주차 OT");
        Chapter ch1 = Chapter.createChapter(study, "1주차 OT", false);
        Chapter ch2 = Chapter.createChapter(study, "2주차 실습", false);

        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(true);
        given(chapterRepository.findByStudyId(studyId)).willReturn(List.of(ch1, ch2));

        // when
        ProgressRoadMapResponseDto dto = service.getRoadMap(studyId, userMember);

        // then
        assertNotNull(dto);
        assertEquals(2, dto.chapters().size());
        assertEquals("1주차 OT", dto.chapters().get(0).content());
        then(chapterRepository).should().findByStudyId(studyId);
    }

    @Test
    void getRoadMap_스터디없음_예외_STUDY_NOT_FOUND() {
        // given
        given(studyRepository.existsById(studyId)).willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getRoadMap(studyId, userMember));

        // then
        assertEquals(ErrorCode.STUDY_NOT_FOUND, ex.getErrorCode());
        then(chapterRepository).should(never()).findByStudyId(any());
    }

    @Test
    void getRoadMap_권한없음_예외_FORBIDDEN_STUDY_MEMBER_ONLY() {
        // given
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getRoadMap(studyId, userMember));

        // then
        assertEquals(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY, ex.getErrorCode());
        then(chapterRepository).should(never()).findByStudyId(any());
    }

    // ---------- createChapter ----------

    @Test
    void createChapter_리더권한일때_챕터저장됨() {
        // given
        ProgressChapterRequestDto req = new ProgressChapterRequestDto("새 차시");
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userLeader.getId()), anyCollection()))
                .willReturn(true);
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));

        // when
        service.createChapter(studyId, req, userLeader);

        // then
        ArgumentCaptor<Chapter> captor = ArgumentCaptor.forClass(Chapter.class);
        verify(chapterRepository).save(captor.capture());
        assertEquals("새 차시", captor.getValue().getContent());
        assertSame(study, captor.getValue().getStudy());
    }

    @Test
    void createChapter_권한없음_예외_FORBIDDEN_STUDY_LEADER_ONLY() {
        // given
        ProgressChapterRequestDto req = new ProgressChapterRequestDto("새 차시");
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createChapter(studyId, req, userMember));

        // then
        assertEquals(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY, ex.getErrorCode());
        then(chapterRepository).should(never()).save(any());
    }

    @Test
    void createChapter_스터디없음_예외_STUDY_NOT_FOUND() {
        // given
        ProgressChapterRequestDto req = new ProgressChapterRequestDto("새 차시");
        given(studyRepository.existsById(studyId)).willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createChapter(studyId, req, userLeader));

        // then
        assertEquals(ErrorCode.STUDY_NOT_FOUND, ex.getErrorCode());
        then(chapterRepository).should(never()).save(any());
    }

    // ---------- updateChapter ----------

    @Test
    void updateChapter_리더권한일때_내용수정되고_업데이트반환() {
        // given
        ProgressChapterRequestDto req = new ProgressChapterRequestDto("수정된 내용");
        Chapter chapter = Chapter.createChapter(study, "원래 내용", false);

        given(chapterRepository.findById(chapterId)).willReturn(Optional.of(chapter));
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userLeader.getId()), anyCollection()))
                .willReturn(true);
        given(chapterRepository.updateContent(chapterId, "수정된 내용")).willReturn(1);

        // when
        assertDoesNotThrow(() -> service.updateChapter(chapterId, req, userLeader));

        // then
        then(chapterRepository).should().updateContent(chapterId, "수정된 내용");
    }

    @Test
    void updateChapter_챕터없음_예외_CHAPTER_NOT_FOUND() {
        // given
        given(chapterRepository.findById(chapterId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateChapter(chapterId, new ProgressChapterRequestDto("x"), userLeader));

        // then
        assertEquals(ErrorCode.CHAPTER_NOT_FOUND, ex.getErrorCode());
        then(chapterRepository).should(never()).updateContent(anyLong(), anyString());
    }

    @Test
    void updateChapter_권한없음_예외_FORBIDDEN_STUDY_LEADER_ONLY() {
        // given
        Chapter chapter = Chapter.createChapter(study, "원래 내용", false);
        given(chapterRepository.findById(chapterId)).willReturn(Optional.of(chapter));
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.updateChapter(chapterId, new ProgressChapterRequestDto("수정"), userMember));

        // then
        assertEquals(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY, ex.getErrorCode());
        then(chapterRepository).should(never()).updateContent(anyLong(), anyString());
    }

    // ---------- deleteChapter ----------

    @Test
    void deleteChapter_리더권한일때_삭제성공() {
        // given
        Chapter chapter = Chapter.createChapter(study, "내용", false);
        given(chapterRepository.findById(chapterId)).willReturn(Optional.of(chapter));
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userLeader.getId()), anyCollection()))
                .willReturn(true);

        // when
        service.deleteChapter(chapterId, userLeader);

        // then
        then(chapterRepository).should().deleteById(chapterId);
    }

    @Test
    void deleteChapter_챕터없음_예외_CHAPTER_NOT_FOUND() {
        // given
        given(chapterRepository.findById(chapterId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.deleteChapter(chapterId, userLeader));

        // then
        assertEquals(ErrorCode.CHAPTER_NOT_FOUND, ex.getErrorCode());
        then(chapterRepository).should(never()).deleteById(anyLong());
    }

    // ---------- completeChapter ----------

    @Test
    void completeChapter_리더권한일때_완료처리성공() {
        // given
        Chapter chapter = Chapter.createChapter(study, "내용", false);
        given(chapterRepository.findById(chapterId)).willReturn(Optional.of(chapter));
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userLeader.getId()), anyCollection()))
                .willReturn(true);
        given(chapterRepository.complete(chapterId)).willReturn(1);

        // when / then
        assertDoesNotThrow(() -> service.completeChapter(chapterId, userLeader));
        then(chapterRepository).should().complete(chapterId);
    }

    @Test
    void completeChapter_챕터없음_예외_CHAPTER_NOT_FOUND() {
        // given
        given(chapterRepository.findById(chapterId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.completeChapter(chapterId, userLeader));

        // then
        assertEquals(ErrorCode.CHAPTER_NOT_FOUND, ex.getErrorCode());
        then(chapterRepository).should(never()).complete(anyLong());
    }

    // ---------- getStudyStatus ----------

    @Test
    void getStudyStatus_멤버권한일때_개인진척도리스트반환_모든지표검증() {
        // given
        User u1 = mock(User.class); when(u1.getId()).thenReturn(1L); when(u1.getNickname()).thenReturn("철수");
        User u2 = mock(User.class); when(u2.getId()).thenReturn(2L); when(u2.getNickname()).thenReturn("영희");

        StudyMember sm1 = mock(StudyMember.class);
        when(sm1.getUser()).thenReturn(u1);
        when(sm1.getRole()).thenReturn(StudyMemberRole.MEMBER);

        StudyMember sm2 = mock(StudyMember.class);
        when(sm2.getUser()).thenReturn(u2);
        when(sm2.getRole()).thenReturn(StudyMemberRole.LEADER);

        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(true);
        given(studyMemberRepository.findByStudyIdFetchUser(studyId)).willReturn(List.of(sm1, sm2));

        // 핵심: 모든 맵 스텁
        given(attendanceRepository.countMapByStudy(studyId)).willReturn(Map.of(1L, 3L));  // 철수=3, 영희=0
        given(quizSubmissionRepository.countMapByStudy(studyId)).willReturn(Map.of(1L, 2L, 2L, 1L)); // 철수=2, 영희=1
        given(reflectionRepository.countMapByStudy(studyId)).willReturn(Collections.emptyMap()); // 둘 다 0


        // when
        ProgressStatusResponseDto dto = service.getStudyStatus(studyId, userMember);

        // then
        assertNotNull(dto);
        assertEquals(2, dto.progressMemberStatusDto().size());

        // 순서 의존 제거: 닉네임으로 매핑
        Map<String, ProgressMemberStatusDto> byName = dto.progressMemberStatusDto().stream()
                .collect(Collectors.toMap(ProgressMemberStatusDto::nickname, d -> d));

        assertEquals(StudyMemberRole.MEMBER, byName.get("철수").role());
        assertEquals(StudyMemberRole.LEADER, byName.get("영희").role());

        assertEquals(3, byName.get("철수").attendance_count());
        assertEquals(0, byName.get("영희").attendance_count());

        assertEquals(2, byName.get("철수").quiz_count());
        assertEquals(1, byName.get("영희").quiz_count());

        assertEquals(0, byName.get("철수").reflection_count());
        assertEquals(0, byName.get("영희").reflection_count());
    }

    @Test
    void getMyStudyStatus_멤버권한일때_본인만반환_모든지표검증() {
        // --- checkException() 통과에 필요한 "정확히 2개"의 스텁 ---
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                eq(studyId), eq(userMember.getId()),
                eq(List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))
        )).willReturn(true);

        // --- 실제 집계 대상 데이터: 스터디 멤버 목록은 리더/멤버 모두 반환되지만, 서비스에서 본인만 필터 ---
        StudyMember smLeader = mock(StudyMember.class);
        when(smLeader.getUser()).thenReturn(userLeader);

        StudyMember smMember = mock(StudyMember.class);
        when(smMember.getUser()).thenReturn(userMember);
        when(smMember.getRole()).thenReturn(StudyMemberRole.MEMBER);

        given(studyMemberRepository.findByStudyIdFetchUser(studyId))
                .willReturn(List.of(smLeader, smMember));

        // --- 카운트 맵: 본인(200L) 데이터만 있어도 충분. 없으면 0으로 처리됨 ---
        given(attendanceRepository.countMapByStudy(studyId))
                .willReturn(Map.of(200L, 3L));
        given(quizSubmissionRepository.countMapByStudy(studyId))
                .willReturn(Map.of(200L, 2L));
        given(reflectionRepository.countMapByStudy(studyId))
                .willReturn(Collections.emptyMap());

        // when
        ProgressStatusResponseDto dto = service.getMyStudyStatus(studyId, userMember);

        // then
        assertNotNull(dto);
        assertEquals(1, dto.progressMemberStatusDto().size(), "요청자 본인만 포함되어야 함");

        ProgressMemberStatusDto only = dto.progressMemberStatusDto().get(0);
        assertEquals(userMember.getNickname(), only.nickname());
        assertEquals(StudyMemberRole.MEMBER, only.role());
        assertEquals(3, only.attendance_count());
        assertEquals(2, only.quiz_count());
        assertEquals(0, only.reflection_count());

        // 핵심 상호작용만 확인 (불필요한 verify/doNothing 등은 추가하지 않음)
        verify(studyRepository).existsById(studyId);
        verify(studyMemberRepository).existsByStudyIdAndUserIdAndRoleIn(
                eq(studyId), eq(userMember.getId()),
                eq(List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))
        );
        verify(studyMemberRepository).findByStudyIdFetchUser(studyId);
        verify(attendanceRepository).countMapByStudy(studyId);
        verify(quizSubmissionRepository).countMapByStudy(studyId);
        verify(reflectionRepository).countMapByStudy(studyId);
        verifyNoMoreInteractions(studyRepository, studyMemberRepository,
                attendanceRepository, quizSubmissionRepository, reflectionRepository);
    }

    @Test
    void getStudyStatus_권한없음_예외_FORBIDDEN_STUDY_MEMBER_ONLY() {
        // given
        given(studyRepository.existsById(studyId)).willReturn(true);
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(eq(studyId), eq(userMember.getId()), anyCollection()))
                .willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getStudyStatus(studyId, userMember));

        // then
        assertEquals(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY, ex.getErrorCode());
    }

    @Test
    void getStudyStatus_스터디없음_예외_STUDY_NOT_FOUND() {
        // given
        given(studyRepository.existsById(studyId)).willReturn(false);

        // when
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.getStudyStatus(studyId, userMember));

        // then
        assertEquals(ErrorCode.STUDY_NOT_FOUND, ex.getErrorCode());
    }
}

