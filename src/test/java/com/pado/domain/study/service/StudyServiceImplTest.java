package com.pado.domain.study.service;

import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.chat.repository.LastReadMessageRepository;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.study.dto.response.StudySimpleResponseDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.Gender;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyServiceImplTest {

    @InjectMocks
    private StudyServiceImpl studyService;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private LastReadMessageRepository lastReadMessageRepository;

    private static final int MAX_PAGE_SIZE = 50;

    // updateStudy 테스트를 위한 공용 변수
    private User leaderUser;
    private User nonLeaderUser;
    private Study existingStudy;
    private Long studyId = 1L;

    // 공용 변수 초기화
    @BeforeEach
    void setUp() {
        leaderUser = User.builder().nickname("Leader").email("leader@test.com").gender(Gender.MALE)
            .region(Region.SEOUL).build();
        ReflectionTestUtils.setField(leaderUser, "id", 1L);

        nonLeaderUser = User.builder().nickname("NonLeader").email("non@test.com")
            .gender(Gender.FEMALE).region(Region.BUSAN).build();
        ReflectionTestUtils.setField(nonLeaderUser, "id", 2L);

        existingStudy = Study.builder()
            .id(studyId)
            .leader(leaderUser)
            .title("Original Title")
            .description("Original Desc")
            .maxMembers(10)
            .region(Region.SEOUL)
            .build();

        // 테스트를 위해 초기 컬렉션 상태 설정
        // Study 엔티티의 updateInterests/updateConditions를 직접 사용
        existingStudy.updateInterests(List.of(Category.PROGRAMMING, Category.EMPLOYMENT));
        existingStudy.updateConditions(List.of("Condition 1", "Condition 2"));
    }

    @Test
    void 스터디_생성_성공() {
        // given
        User user = User.builder().build();
        StudyCreateRequestDto dto = new StudyCreateRequestDto(
            "새로운 스터디",
            "한 줄 설명",
            "상세 설명",
            List.of(Category.PROGRAMMING, Category.LANGUAGE),
            Region.ONLINE,
            "월요일 오후 2시-4시",
            10,
            List.of("열심히 하실 분만"),
            "image_url"
        );

        Study dummySavedStudy = Study.builder()
            .id(1L) // ID 설정
            .leader(user)
            .title(dto.title())
            .description(dto.description())
            .detailDescription(dto.detail_description())
            .studyTime(dto.study_time())
            .region(dto.region())
            .maxMembers(dto.max_members())
            .fileKey(dto.file_key())
            .build();

        when(studyRepository.save(any(Study.class))).thenReturn(dummySavedStudy);

        // when
        studyService.createStudy(user, dto);

        // then
        ArgumentCaptor<Study> captor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(captor.capture());
        Study savedStudy = captor.getValue();

        ArgumentCaptor<StudyMember> memberCaptor = ArgumentCaptor.forClass(StudyMember.class);
        verify(studyMemberRepository).save(memberCaptor.capture());
        StudyMember savedMember = memberCaptor.getValue();

        assertAll(
            () -> assertThat(savedMember.getStudy()).isEqualTo(savedStudy),
            () -> assertThat(savedMember.getUser()).isEqualTo(user),
            () -> assertThat(savedMember.getRole()).isEqualTo(StudyMemberRole.LEADER)
        );

        assertAll(
            () -> assertThat(savedStudy.getTitle()).isEqualTo(dto.title()),
            () -> assertThat(savedStudy.getDescription()).isEqualTo(dto.description()),
            () -> assertThat(savedStudy.getDetailDescription()).isEqualTo(dto.detail_description()),
            () -> assertThat(savedStudy.getRegion()).isEqualTo(dto.region()),
            () -> assertThat(savedStudy.getStudyTime()).isEqualTo(dto.study_time()),
            () -> assertThat(savedStudy.getMaxMembers()).isEqualTo(dto.max_members()),
            () -> assertThat(savedStudy.getFileKey()).isEqualTo(dto.file_key()),
            () -> assertThat(savedStudy.getLeader()).isEqualTo(user),

            () -> assertThat(savedStudy.getInterests()).hasSize(2),
            () -> assertThat(savedStudy.getInterests().get(0).getCategory()).isEqualTo(
                Category.PROGRAMMING),
            () -> assertThat(savedStudy.getInterests().get(1).getCategory()).isEqualTo(
                Category.LANGUAGE),

            () -> assertThat(savedStudy.getConditions())
                .extracting(StudyCondition::getContent)
                .containsExactly("열심히 하실 분만")
        );
    }

    @Test
    void 스터디_목록_조회_성공() {
        // given
        User user = User.builder().build();
        Study study1 = Study.builder().id(1L).title("스터디1").description("설명1").build();
        Study study2 = Study.builder().id(2L).title("스터디2").description("설명2").build();
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size);
        Slice<Study> mockSlice = new SliceImpl<>(List.of(study1, study2), pageable, false);

        when(studyRepository.findStudiesByFilter(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(mockSlice);

        // when
        StudyListResponseDto response = studyService.findStudies(user, "키워드", null, null, page,
            size);

        // then
        assertThat(response.studies()).hasSize(2);
        assertThat(response.studies().get(0))
            .extracting(StudySimpleResponseDto::title, StudySimpleResponseDto::description)
            .containsExactly("스터디1", "설명1");
        assertThat(response.studies().get(1))
            .extracting(StudySimpleResponseDto::title, StudySimpleResponseDto::description)
            .containsExactly("스터디2", "설명2");
        assertThat(response.page()).isEqualTo(0);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.has_next()).isFalse();
    }

    @Test
    void 페이지_사이즈_최대값_초과시_최대페이지사이즈로_조정() {
        int requestedPage = 0;
        int requestedSize = MAX_PAGE_SIZE + 20;

        when(studyRepository.findStudiesByFilter(any(), any(), any(), any(), any(Pageable.class)))
            .thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, MAX_PAGE_SIZE), false));

        studyService.findStudies(null, null, null, null, requestedPage, requestedSize);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(studyRepository).findStudiesByFilter(any(), any(), any(), any(),
            pageableCaptor.capture());
        Pageable capturedPageable = pageableCaptor.getValue();

        assertThat(capturedPageable.getPageSize()).isEqualTo(MAX_PAGE_SIZE);
        assertThat(capturedPageable.getPageNumber()).isEqualTo(requestedPage);
    }

    @Test
    void 상세정보_조회_성공() {
        // given
        User testUser = User.builder()
            .email("test@test.com")
            .passwordHash("1234")
            .nickname("tester")
            .gender(Gender.MALE)
            .region(Region.SEOUL)
            .build();

        Study mockStudy = Study.builder()
            .id(100L)
            .fileKey("https://test.com/image.jpg")
            .title("테스트 스터디")
            .description("테스트 설명")
            .detailDescription("상세 설명")
            .leader(testUser)
            .region(Region.SEOUL)
            .studyTime("주말")
            .maxMembers(5)
            .build();

        mockStudy.updateInterests(List.of(Category.PROGRAMMING, Category.EMPLOYMENT));
        mockStudy.updateConditions(List.of("코어타임 참여 필수", "성실한 분"));

        when(studyRepository.findByIdWithLeader(mockStudy.getId())).thenReturn(
            Optional.of(mockStudy));

        // studyMemberRepository.countByStudy(mockStudy) 호출에 대한 Mocking 추가
        when(studyMemberRepository.countByStudy(mockStudy)).thenReturn(1L);

        // when
        StudyDetailResponseDto result = studyService.getStudyDetail(mockStudy.getId());

        // then
        assertAll(
            () -> assertThat(result.file_key()).isEqualTo(mockStudy.getFileKey()),
            () -> assertThat(result.title()).isEqualTo(mockStudy.getTitle()),
            () -> assertThat(result.description()).isEqualTo(mockStudy.getDescription()),
            () -> assertThat(result.detail_description()).isEqualTo(
                mockStudy.getDetailDescription()),
            () -> assertThat(result.region()).isEqualTo(mockStudy.getRegion()),
            () -> assertThat(result.study_time()).isEqualTo(mockStudy.getStudyTime()),
            () -> assertThat(result.max_members()).isEqualTo(mockStudy.getMaxMembers()),
            () -> assertThat(result.current_members()).isEqualTo(1), // Mocking된 값(1) 확인

            () -> assertThat(result.interests()).containsExactlyInAnyOrder(Category.PROGRAMMING,
                Category.EMPLOYMENT),
            () -> assertThat(result.conditions()).containsExactlyInAnyOrder("코어타임 참여 필수", "성실한 분")
        );

        verify(studyRepository, times(1)).findByIdWithLeader(mockStudy.getId());
        verify(studyMemberRepository, times(1)).countByStudy(mockStudy); // 호출 검증
    }

    @Test
    void 상세정보_조회_존재하지않는_스터디ID_예외() {
        Long invalidId = 999L;
        when(studyRepository.findByIdWithLeader(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyService.getStudyDetail(invalidId))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
    }

    // --- [새로 추가된 테스트 블록] ---
    @Nested
    @DisplayName("updateStudy (스터디 수정) 테스트")
    class UpdateStudyTests {

        @Test
        @DisplayName("성공: 컬렉션 변경 없이 제목만 수정 (500 에러 재현 케이스)")
        void updateStudy_WithSameCollections_ShouldSucceed() {
            // given
            when(studyRepository.findByIdWithLeader(studyId)).thenReturn(
                Optional.of(existingStudy));

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "New Title",
                "New Desc",
                "New Detail",
                List.of(Category.PROGRAMMING, Category.EMPLOYMENT), // ★ 동일한 관심사
                Region.ONLINE,
                "New Time",
                5,
                List.of("Condition 1", "Condition 2"), // ★ 동일한 조건
                "new_key"
            );

            // when
            studyService.updateStudy(leaderUser, studyId, updateRequest);

            // then
            assertThat(existingStudy.getTitle()).isEqualTo("New Title");
            assertThat(existingStudy.getRegion()).isEqualTo(Region.ONLINE);
            assertThat(existingStudy.getMaxMembers()).isEqualTo(5);

            assertThat(existingStudy.getInterests())
                .extracting(StudyCategory::getCategory)
                .containsExactlyInAnyOrder(Category.PROGRAMMING, Category.EMPLOYMENT);
            assertThat(existingStudy.getConditions())
                .extracting(StudyCondition::getContent)
                .containsExactlyInAnyOrder("Condition 1", "Condition 2");

            verify(studyRepository, times(1)).findByIdWithLeader(studyId);
        }

        @Test
        @DisplayName("성공: 관심사 1개 추가 (2개 -> 3개)")
        void updateStudy_AddInterest() {
            // given
            when(studyRepository.findByIdWithLeader(studyId)).thenReturn(
                Optional.of(existingStudy));

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "Title", "Desc", "Detail",
                List.of(Category.PROGRAMMING, Category.EMPLOYMENT, Category.LANGUAGE),
                // ★ 신규(LANGUAGE) 추가
                Region.SEOUL, "Time", 5, List.of("Condition 1"), "key"
            );

            // when
            studyService.updateStudy(leaderUser, studyId, updateRequest);

            // then
            assertThat(existingStudy.getInterests())
                .extracting(StudyCategory::getCategory)
                .containsExactlyInAnyOrder(Category.PROGRAMMING, Category.EMPLOYMENT,
                    Category.LANGUAGE);
            assertThat(existingStudy.getConditions())
                .extracting(StudyCondition::getContent)
                .containsExactlyInAnyOrder("Condition 1");
        }

        @Test
        @DisplayName("성공: 관심사 1개 삭제 (2개 -> 1개)")
        void updateStudy_RemoveInterest() {
            // given
            when(studyRepository.findByIdWithLeader(studyId)).thenReturn(
                Optional.of(existingStudy));

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "Title", "Desc", "Detail",
                List.of(Category.PROGRAMMING), // ★ EMPLOYMENT 삭제됨
                Region.SEOUL, "Time", 5, List.of("Condition 1", "Condition 2"), "key"
            );

            // when
            studyService.updateStudy(leaderUser, studyId, updateRequest);

            // then
            assertThat(existingStudy.getInterests())
                .extracting(StudyCategory::getCategory)
                .containsExactlyInAnyOrder(Category.PROGRAMMING);
            assertThat(existingStudy.getConditions())
                .extracting(StudyCondition::getContent)
                .containsExactlyInAnyOrder("Condition 1", "Condition 2");
        }

        @Test
        @DisplayName("성공: 관심사 모두 삭제 (2개 -> 0개)")
        void updateStudy_RemoveAllInterests() {
            // given
            when(studyRepository.findByIdWithLeader(studyId)).thenReturn(
                Optional.of(existingStudy));

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "Title", "Desc", "Detail",
                List.of(), // ★ 빈 리스트
                Region.SEOUL, "Time", 5, List.of(), "key"
            );

            // when
            studyService.updateStudy(leaderUser, studyId, updateRequest);

            // then
            assertThat(existingStudy.getInterests()).isEmpty();
            assertThat(existingStudy.getConditions()).isEmpty();
        }

        @Test
        @DisplayName("실패: 리더가 아닌 사용자가 수정 시도 시 FORBIDDEN 예외")
        void updateStudy_Fail_NotLeader() {
            // given
            when(studyRepository.findByIdWithLeader(studyId)).thenReturn(
                Optional.of(existingStudy));

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "New Title", "Desc", "Detail",
                List.of(), Region.SEOUL, "Time", 5, List.of(), "key"
            );

            // when & then
            assertThatThrownBy(
                () -> studyService.updateStudy(nonLeaderUser, studyId, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 스터디 수정 시도 시 NOT_FOUND 예외")
        void updateStudy_Fail_StudyNotFound() {
            // given
            Long nonExistentStudyId = 999L;
            when(studyRepository.findByIdWithLeader(nonExistentStudyId)).thenReturn(
                Optional.empty());

            StudyCreateRequestDto updateRequest = new StudyCreateRequestDto(
                "New Title", "Desc", "Detail",
                List.of(), Region.SEOUL, "Time", 5, List.of(), "key"
            );

            // when & then
            assertThatThrownBy(
                () -> studyService.updateStudy(leaderUser, nonExistentStudyId, updateRequest))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
        }
    }
}