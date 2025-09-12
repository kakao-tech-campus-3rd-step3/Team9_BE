package com.pado.domain.study.service;

import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.AlreadyAppliedException;
import com.pado.domain.study.exception.AlreadyMemberException;
import com.pado.domain.study.exception.StudyFullException;
import com.pado.domain.study.repository.StudyApplicationRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyMemberServiceImplTest {

    @InjectMocks
    private StudyMemberServiceImpl studyMemberService;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyApplicationRepository studyApplicationRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    private User leader;
    private User applicant;

    @BeforeEach
    void setUp() {
        // given
        leader = User.builder()
                .email("leader@test.com")
                .nickname("리더")
                .region(Region.SEOUL)
                .build();

        applicant = User.builder()
                .email("applicant@test.com")
                .nickname("지원자")
                .region(Region.SEOUL)
                .build();
    }

    @Test
    void 스터디_참여_신청_성공() {
        // given
        Study study = Study.builder().leader(leader).maxMembers(5).build();
        StudyApplyRequestDto requestDto = new StudyApplyRequestDto("참여하고 싶습니다.");

        when(studyRepository.findByIdWithPessimisticLock(study.getId())).thenReturn(Optional.of(study));
        when(studyMemberRepository.existsByStudyAndUser(study, applicant)).thenReturn(false);
        when(studyApplicationRepository.existsByStudyAndUserAndStatus(study, applicant, StudyApplicationStatus.PENDING)).thenReturn(false);
        when(studyMemberRepository.countByStudy(study)).thenReturn(3L);

        // when
        studyMemberService.applyToStudy(applicant, study.getId(), requestDto);

        // then
        ArgumentCaptor<StudyApplication> captor = ArgumentCaptor.forClass(StudyApplication.class);
        verify(studyApplicationRepository).save(captor.capture());
        StudyApplication saved = captor.getValue();

        assertAll(
                () -> assertThat(saved.getStudy()).isEqualTo(study),
                () -> assertThat(saved.getUser()).isEqualTo(applicant),
                () -> assertThat(saved.getMessage()).isEqualTo(requestDto.message()),
                () -> assertThat(saved.getStatus()).isEqualTo(StudyApplicationStatus.PENDING)
        );
    }

    @Test
    void 스터디장_본인_신청시_예외() {
        // given
        Long studyId = 1L;
        Study study = Study.builder()
                .id(studyId)
                .leader(leader)
                .status(StudyStatus.RECRUITING)
                .build();

        when(studyRepository.findByIdWithPessimisticLock(studyId)).thenReturn(Optional.of(study));

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(leader, studyId, new StudyApplyRequestDto("test")))
                .isInstanceOf(AlreadyMemberException.class)
                .hasMessageContaining("스터디장은 자신의 스터디에 참여 신청할 수 없습니다.");
    }


    @Test
    void 모집_중이_아닌_스터디에_신청시_예외() {
        // given
        Study study = Study.builder().leader(leader).status(StudyStatus.FINISHED).build();
        when(studyRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(study));

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(applicant, 1L, new StudyApplyRequestDto("test")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STUDY_NOT_RECRUITING.message);
    }

    @Test
    void 이미_멤버인_경우_예외() {
        // given
        Study study = Study.builder().leader(leader).status(StudyStatus.RECRUITING).build();
        when(studyRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(study));
        when(studyMemberRepository.existsByStudyAndUser(study, applicant)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(applicant, 1L, new StudyApplyRequestDto("test")))
                .isInstanceOf(AlreadyMemberException.class)
                .hasMessageContaining(ErrorCode.ALREADY_MEMBER.message);
    }

    @Test
    void 이미_신청_후_대기중인_경우_예외() {
        // given
        Study study = Study.builder().leader(leader).status(StudyStatus.RECRUITING).build();
        when(studyRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(study));
        when(studyMemberRepository.existsByStudyAndUser(study, applicant)).thenReturn(false);
        when(studyApplicationRepository.existsByStudyAndUserAndStatus(study, applicant, StudyApplicationStatus.PENDING)).thenReturn(true);

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(applicant, 1L, new StudyApplyRequestDto("test")))
                .isInstanceOf(AlreadyAppliedException.class)
                .hasMessageContaining(ErrorCode.ALREADY_APPLIED.message);
    }

    @Test
    void 스터디_정원이_가득_찬_경우_예외() {
        // given
        Study study = Study.builder().leader(leader).status(StudyStatus.RECRUITING).maxMembers(5).build();
        when(studyRepository.findByIdWithPessimisticLock(anyLong())).thenReturn(Optional.of(study));
        when(studyMemberRepository.existsByStudyAndUser(study, applicant)).thenReturn(false);
        when(studyApplicationRepository.existsByStudyAndUserAndStatus(study, applicant, StudyApplicationStatus.PENDING)).thenReturn(false);
        when(studyMemberRepository.countByStudy(study)).thenReturn(5L);

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(applicant, 1L, new StudyApplyRequestDto("test")))
                .isInstanceOf(StudyFullException.class)
                .hasMessageContaining(ErrorCode.STUDY_FULL.message);
    }
}