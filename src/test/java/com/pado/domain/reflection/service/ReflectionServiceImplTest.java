package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.request.ReflectionCreateRequestDto;
import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReflectionServiceImplTest {

    @InjectMocks
    private ReflectionServiceImpl reflectionService;

    @Mock
    private ReflectionRepository reflectionRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;
    @Mock
    private ScheduleRepository scheduleRepository;

    private User author;
    private User anotherMember;
    private User nonMember;
    private Study study;
    private StudyMember authorMember;
    private Reflection reflection;

    @BeforeEach
    void setUp() {
        author = User.builder().nickname("author").build();
        anotherMember = User.builder().nickname("anotherMember").build();
        nonMember = User.builder().nickname("nonMember").build();
        ReflectionTestUtils.setField(author, "id", 1L);
        ReflectionTestUtils.setField(anotherMember, "id", 2L);
        ReflectionTestUtils.setField(nonMember, "id", 3L);

        study = Study.builder().title("Test Study").build();
        ReflectionTestUtils.setField(study, "id", 10L);

        authorMember = StudyMember.builder().user(author).study(study).build();
        ReflectionTestUtils.setField(authorMember, "id", 100L);

        reflection = Reflection.builder()
            .study(study)
            .studyMember(authorMember)
            .title("테스트 회고")
            .learnedContent("배운 점")
            .improvement("개선할 점")
            .satisfactionScore(5)
            .understandingScore(5)
            .participationScore(5)
            .build();
        ReflectionTestUtils.setField(reflection, "id", 1000L);
    }

    @Test
    @DisplayName("스터디 멤버가 회고를 성공적으로 생성한다.")
    void createReflection_Success() {
        // given
        ReflectionCreateRequestDto request = new ReflectionCreateRequestDto("새로운 회고", null, 5, 5, 5,
            "배운 점", "개선할 점");
        given(
            studyMemberRepository.findByStudyIdAndUserId(study.getId(), author.getId())).willReturn(
            Optional.of(authorMember));
        given(studyRepository.findById(study.getId())).willReturn(Optional.of(study));
        given(reflectionRepository.save(any(Reflection.class))).willReturn(reflection);

        // when
        reflectionService.createReflection(study.getId(), author, request);
        // then
        verify(reflectionRepository).save(any(Reflection.class));
    }

    @Test
    @DisplayName("스터디 멤버가 아닐 경우 회고 생성에 실패한다.")
    void createReflection_Fail_NotStudyMember() {
        // given
        ReflectionCreateRequestDto request = new ReflectionCreateRequestDto("실패할 회고", null, 5, 5, 5,
            "배운 점", "개선할 점");
        given(studyMemberRepository.findByStudyIdAndUserId(study.getId(),
            nonMember.getId())).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(
            () -> reflectionService.createReflection(study.getId(), nonMember, request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
    }

    @Test
    @DisplayName("회고 작성자가 본인의 회고를 수정하면 성공한다.")
    void updateReflection_Success_ByOwner() {
        // given
        ReflectionCreateRequestDto request = new ReflectionCreateRequestDto("수정된 제목", null, 4, 4, 4,
            "수정된 내용", "수정된 내용");
        given(reflectionRepository.findById(reflection.getId())).willReturn(
            Optional.of(reflection));
        given(
            studyMemberRepository.findByStudyIdAndUserId(study.getId(), author.getId())).willReturn(
            Optional.of(authorMember));

        // when
        reflectionService.updateReflection(study.getId(), reflection.getId(), author, request);
        // then
        assertThat(reflection.getTitle()).isEqualTo("수정된 제목");
        assertThat(reflection.getSatisfactionScore()).isEqualTo(4);
        assertThat(reflection.getLearnedContent()).isEqualTo("수정된 내용");
    }

    @Test
    @DisplayName("회고 작성자가 아닌 경우 회고 수정에 실패한다.")
    void updateReflection_Fail_NotOwner() {
        // given
        ReflectionCreateRequestDto request = new ReflectionCreateRequestDto("수정될 수 없는 제목", null, 4,
            4, 4, "수정된 내용", "수정된 내용");
        given(reflectionRepository.findById(reflection.getId())).willReturn(
            Optional.of(reflection));
        // 수정: new StudyMember() 대신 builder 사용
        given(studyMemberRepository.findByStudyIdAndUserId(study.getId(),
            anotherMember.getId())).willReturn(
            Optional.of(StudyMember.builder().user(anotherMember).study(study).build()));
        // when & then
        assertThatThrownBy(
            () -> reflectionService.updateReflection(study.getId(), reflection.getId(),
                anotherMember, request))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_REFLECTION_OWNER_ONLY);
    }

    @Test
    @DisplayName("회고 작성자가 본인의 회고를 삭제하면 성공한다.")
    void deleteReflection_Success_ByOwner() {
        // given
        given(reflectionRepository.findById(reflection.getId())).willReturn(
            Optional.of(reflection));
        given(
            studyMemberRepository.findByStudyIdAndUserId(study.getId(), author.getId())).willReturn(
            Optional.of(authorMember));

        // when
        reflectionService.deleteReflection(study.getId(), reflection.getId(), author);
        // then
        verify(reflectionRepository).deleteById(reflection.getId());
    }

    @Test
    @DisplayName("회고 작성자가 아닌 경우 회고 삭제에 실패한다.")
    void deleteReflection_Fail_NotOwner() {
        // given
        given(reflectionRepository.findById(reflection.getId())).willReturn(
            Optional.of(reflection));
        // 수정: new StudyMember() 대신 builder 사용
        given(studyMemberRepository.findByStudyIdAndUserId(study.getId(),
            anotherMember.getId())).willReturn(
            Optional.of(StudyMember.builder().user(anotherMember).study(study).build()));
        // when & then
        assertThatThrownBy(
            () -> reflectionService.deleteReflection(study.getId(), reflection.getId(),
                anotherMember))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_REFLECTION_OWNER_ONLY);
    }

    @Test
    @DisplayName("스터디 멤버는 다른 사람의 회고를 상세 조회할 수 있다.")
    void getReflection_Success_ByAnotherMember() {
        // given
        given(reflectionRepository.findById(reflection.getId())).willReturn(
            Optional.of(reflection));
        StudyMember anotherStudyMember = StudyMember.builder().user(anotherMember).study(study)
            .build();
        given(studyMemberRepository.findByStudyIdAndUserId(study.getId(), anotherMember.getId()))
            .willReturn(Optional.of(anotherStudyMember));
        // when
        reflectionService.getReflection(study.getId(), reflection.getId(), anotherMember);
        // then
        verify(reflectionRepository).findById(anyLong());
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 회고 상세 조회에 실패한다.")
    void getReflection_Fail_ByNonMember() {
        // given
        given(studyMemberRepository.findByStudyIdAndUserId(study.getId(),
            nonMember.getId())).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(
            () -> reflectionService.getReflection(study.getId(), reflection.getId(), nonMember))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
    }
}