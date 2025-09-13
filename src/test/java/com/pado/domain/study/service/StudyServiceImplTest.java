package com.pado.domain.study.service;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.study.dto.response.StudySimpleResponseDto;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyCondition;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
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

    private static final int MAX_PAGE_SIZE = 50;

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
                () -> assertThat(savedStudy.getInterests().get(0).getCategory()).isEqualTo(Category.PROGRAMMING),
                () -> assertThat(savedStudy.getInterests().get(1).getCategory()).isEqualTo(Category.LANGUAGE),

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
        StudyListResponseDto response = studyService.findStudies(user, "키워드", null, null, page, size);

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
        verify(studyRepository).findStudiesByFilter(any(), any(), any(), any(), pageableCaptor.capture());
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

        mockStudy.addInterests(List.of(Category.PROGRAMMING, Category.EMPLOYMENT));
        mockStudy.addConditions(List.of("코어타임 참여 필수", "성실한 분"));

        when(studyRepository.findByIdWithLeader(mockStudy.getId())).thenReturn(Optional.of(mockStudy));

        // when
        StudyDetailResponseDto result = studyService.getStudyDetail(mockStudy.getId());

        // then
        assertAll(
                () -> assertThat(result.file_key()).isEqualTo(mockStudy.getFileKey()),
                () -> assertThat(result.title()).isEqualTo(mockStudy.getTitle()),
                () -> assertThat(result.description()).isEqualTo(mockStudy.getDescription()),
                () -> assertThat(result.detail_description()).isEqualTo(mockStudy.getDetailDescription()),
                () -> assertThat(result.region()).isEqualTo(mockStudy.getRegion()),
                () -> assertThat(result.study_time()).isEqualTo(mockStudy.getStudyTime()),
                () -> assertThat(result.max_members()).isEqualTo(mockStudy.getMaxMembers()),

                () -> assertThat(result.interests()).containsExactlyInAnyOrder(Category.PROGRAMMING, Category.EMPLOYMENT),
                () -> assertThat(result.conditions()).containsExactlyInAnyOrder("코어타임 참여 필수", "성실한 분")
        );

        verify(studyRepository, times(1)).findByIdWithLeader(mockStudy.getId());
    }

    @Test
    void 상세정보_조회_존재하지않는_스터디ID_예외() {
        Long invalidId = 999L;
        when(studyRepository.findByIdWithLeader(invalidId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studyService.getStudyDetail(invalidId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
    }

}