package com.pado.domain.study.service;

import com.pado.domain.chat.entity.LastReadMessage;
import com.pado.domain.chat.repository.ChatMessageRepository; // 추가
import com.pado.domain.chat.repository.LastReadMessageRepository; // 추가
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto; // 추가
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.dto.response.StudyApplicantDetailDto; // 추가
import com.pado.domain.study.dto.response.StudyApplicantListResponseDto; // 추가
import com.pado.domain.study.dto.response.StudyMemberDetailDto; // 추가
import com.pado.domain.study.dto.response.StudyMemberListResponseDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.*;
import com.pado.domain.study.repository.StudyApplicationRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private LastReadMessageRepository lastReadMessageRepository;

    private User leader;
    private User applicant;
    private User member;
    private Study study;
    private StudyMember leaderMember;
    private StudyMember normalMember;
    private StudyApplication application;

    @BeforeEach
    void setUp() {
        leader = User.builder()
            .email("leader@test.com")
            .nickname("리더")
            .region(Region.SEOUL)
            .gender(Gender.MALE)
            .build();
        ReflectionTestUtils.setField(leader, "id", 1L);

        applicant = User.builder()
            .email("applicant@test.com")
            .nickname("지원자")
            .region(Region.SEOUL)
            .gender(Gender.FEMALE)
            .build();
        ReflectionTestUtils.setField(applicant, "id", 2L);

        member = User.builder()
            .email("member@test.com")
            .nickname("멤버")
            .region(Region.GYEONGGI)
            .gender(Gender.FEMALE)
            .build();
        ReflectionTestUtils.setField(member, "id", 3L);

        study = Study.builder()
            .leader(leader)
            .title("테스트 스터디")
            .maxMembers(5)
            .status(StudyStatus.RECRUITING)
            .build();
        ReflectionTestUtils.setField(study, "id", 10L);

        leaderMember = StudyMember.builder()
            .study(study)
            .user(leader)
            .role(StudyMemberRole.LEADER)
            .build();
        ReflectionTestUtils.setField(leaderMember, "id", 100L);

        normalMember = StudyMember.builder()
            .study(study)
            .user(member)
            .role(StudyMemberRole.MEMBER)
            .build();
        ReflectionTestUtils.setField(normalMember, "id", 101L);

        application = StudyApplication.create(study, applicant, "참여하고 싶습니다.");
        ReflectionTestUtils.setField(application, "id", 1000L);
        ReflectionTestUtils.setField(application, "createdAt", LocalDateTime.now());

    }

    @Test
    void 스터디_참여_신청_성공() {
        // given
        StudyApplyRequestDto requestDto = new StudyApplyRequestDto("참여하고 싶습니다.");
        when(studyRepository.findByIdWithPessimisticLock(study.getId())).thenReturn(
            Optional.of(study));
        when(studyMemberRepository.existsByStudyAndUser(study, applicant)).thenReturn(false);
        when(studyApplicationRepository.existsByStudyAndUserAndStatus(study, applicant,
            StudyApplicationStatus.PENDING)).thenReturn(false);
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
        when(studyRepository.findByIdWithPessimisticLock(study.getId())).thenReturn(
            Optional.of(study));

        // when, then
        assertThatThrownBy(() -> studyMemberService.applyToStudy(leader, study.getId(),
            new StudyApplyRequestDto("test")))
            .isInstanceOf(AlreadyMemberException.class)
            .hasMessageContaining("스터디장은 자신의 스터디에 참여 신청할 수 없습니다.");
    }

    @Nested
    @DisplayName("getStudyMembers 테스트 (확정 멤버 조회)")
    class GetStudyMembersTest {

        @Test
        @DisplayName("성공: 리더가 확정된 멤버 목록 조회 시 리더와 멤버만 반환")
        void getStudyMembers_Success_ReturnsOnlyMembers() {
            // given
            when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
            when(studyMemberRepository.findByStudyWithUser(study)).thenReturn(
                List.of(leaderMember, normalMember));

            // when
            StudyMemberListResponseDto result = studyMemberService.getStudyMembers(leader,
                study.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.members()).hasSize(2);
            assertThat(result.members()).extracting(StudyMemberDetailDto::nickname)
                .containsExactlyInAnyOrder("리더", "멤버");
            assertThat(result.members()).extracting(StudyMemberDetailDto::role)
                .containsExactlyInAnyOrder("LEADER", "MEMBER");
            assertThat(result.members()).allMatch(
                dto -> dto.message() == null);
            verify(studyApplicationRepository, never()).findByStudyWithUser(
                any());
        }

        @Test
        @DisplayName("실패: 일반 멤버가 멤버 목록 조회 시 Forbidden 예외 발생")
        void getStudyMembers_Fail_ByNormalMember() {
            // given
            when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));

            // when & then
            assertThatThrownBy(() -> studyMemberService.getStudyMembers(member, study.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 스터디 ID로 조회 시 NotFound 예외 발생")
        void getStudyMembers_Fail_StudyNotFound() {
            // given
            Long invalidStudyId = 999L;
            when(studyRepository.findById(invalidStudyId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studyMemberService.getStudyMembers(leader, invalidStudyId))
                .isInstanceOf(StudyNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStudyApplicants 테스트 (신청자 조회)")
    class GetStudyApplicantsTest {

        @Test
        @DisplayName("성공: 리더가 신청자 목록 조회 시 PENDING 상태 신청자만 반환")
        void getStudyApplicants_Success_ReturnsOnlyApplicants() {
            // given
            StudyApplication rejectedApplication = StudyApplication.create(study,
                User.builder().nickname("거절된사람").build(), "거절됨");
            ReflectionTestUtils.setField(rejectedApplication, "status",
                StudyApplicationStatus.REJECTED);

            when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
            when(studyApplicationRepository.findByStudyWithUser(study))
                .thenReturn(List.of(application, rejectedApplication));

            // when
            StudyApplicantListResponseDto result = studyMemberService.getStudyApplicants(leader,
                study.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.applicants()).hasSize(1);
            StudyApplicantDetailDto applicantDto = result.applicants().get(0);
            assertThat(applicantDto.applicationId()).isEqualTo(application.getId());
            assertThat(applicantDto.nickname()).isEqualTo("지원자");
            assertThat(applicantDto.applicationMessage()).isEqualTo("참여하고 싶습니다.");
            assertThat(applicantDto.appliedAt()).isNotNull();
            verify(studyMemberRepository, never()).findByStudyWithUser(any());
        }

        @Test
        @DisplayName("성공: 신청자가 없을 경우 빈 리스트 반환")
        void getStudyApplicants_Success_NoApplicants() {
            // given
            when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
            when(studyApplicationRepository.findByStudyWithUser(study)).thenReturn(
                Collections.emptyList());

            // when
            StudyApplicantListResponseDto result = studyMemberService.getStudyApplicants(leader,
                study.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.applicants()).isEmpty();
        }

        @Test
        @DisplayName("실패: 일반 멤버가 신청자 목록 조회 시 Forbidden 예외 발생")
        void getStudyApplicants_Fail_ByNormalMember() {
            // given
            when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));

            // when & then
            assertThatThrownBy(() -> studyMemberService.getStudyApplicants(member, study.getId()))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 스터디 ID로 조회 시 NotFound 예외 발생")
        void getStudyApplicants_Fail_StudyNotFound() {
            // given
            Long invalidStudyId = 999L;
            when(studyRepository.findById(invalidStudyId)).thenReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> studyMemberService.getStudyApplicants(leader, invalidStudyId))
                .isInstanceOf(StudyNotFoundException.class);
        }
    }

    @Test
    @DisplayName("신청 수락 시 StudyMember 생성 및 StudyApplication 삭제, LastReadMessage 생성")
    void updateApplicationStatus_Approve_CreatesMemberAndLastRead_DeletesApplication() {
        // given
        StudyApplicationStatusChangeRequestDto request = new StudyApplicationStatusChangeRequestDto(
            "APPROVED");
        when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
        when(studyApplicationRepository.findById(application.getId())).thenReturn(
            Optional.of(application));
        when(chatMessageRepository.findTopByStudyIdOrderByIdDesc(study.getId())).thenReturn(
            Optional.empty());

        // when
        studyMemberService.updateApplicationStatus(leader, study.getId(), application.getId(),
            request);

        // then
        verify(studyMemberRepository).save(any(StudyMember.class));
        verify(lastReadMessageRepository).save(any(LastReadMessage.class));
        verify(studyApplicationRepository).delete(application);
    }

    @Test
    @DisplayName("신청 거절 시 StudyApplication 삭제")
    void updateApplicationStatus_Reject_DeletesApplication() {
        // given
        StudyApplicationStatusChangeRequestDto request = new StudyApplicationStatusChangeRequestDto(
            "REJECTED");
        when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
        when(studyApplicationRepository.findById(application.getId())).thenReturn(
            Optional.of(application));

        // when
        studyMemberService.updateApplicationStatus(leader, study.getId(), application.getId(),
            request);

        // then
        verify(studyMemberRepository, never()).save(any(StudyMember.class));
        verify(lastReadMessageRepository, never()).save(
            any(LastReadMessage.class));
        verify(studyApplicationRepository).delete(application);
    }

}