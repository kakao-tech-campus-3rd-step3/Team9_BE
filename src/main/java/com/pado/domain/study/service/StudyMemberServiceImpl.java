package com.pado.domain.study.service;

import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.dto.response.StudyMemberDetailDto;
import com.pado.domain.study.dto.response.StudyMemberListResponseDto;
import com.pado.domain.study.dto.response.UserDetailDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.*;
import com.pado.domain.study.repository.StudyApplicationRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.entity.UserInterest;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyMemberServiceImpl implements StudyMemberService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    @Override
    @Transactional
    public void applyToStudy(User user, Long studyId, StudyApplyRequestDto requestDto) {
        Study study = studyRepository.findByIdWithPessimisticLock(studyId)
            .orElseThrow(StudyNotFoundException::new);
        validateApplication(study, user);

        StudyApplication application = StudyApplication.create(
            study,
            user,
            requestDto.message()
        );
        studyApplicationRepository.save(application);
    }

    @Override
    public boolean isStudyLeader(User user, Study study) {
        if (user == null || study == null || study.getLeader() == null) {
            return false;
        }
        return study.getLeader().getId().equals(user.getId());
    }

    @Override
    public boolean isStudyMember(User user, Long studyId) {
        Study study = studyRepository.findById(studyId).orElse(null);
        if (study == null || user == null) {
            return false;
        }

        if (isStudyLeader(user, study)) {
            return true;
        }

        return studyMemberRepository.existsByStudyAndUser(study, user);
    }

    @Transactional
    @Override
    public void updateApplicationStatus(
        User user,
        Long studyId,
        Long applicationId,
        StudyApplicationStatusChangeRequestDto request) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        StudyApplication application = studyApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));
        if (!isStudyLeader(user, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        StudyApplicationStatus newRole = StudyApplicationStatus.fromString(request.status());
        if (newRole.equals(StudyApplicationStatus.APPROVED)) {
            StudyMember member = new StudyMember(study, application.getUser(),
                StudyMemberRole.MEMBER, application.getMessage(), 0);
            studyMemberRepository.save(member);
            studyApplicationRepository.delete(application);
        } else if (newRole.equals(StudyApplicationStatus.REJECTED)) {
            studyApplicationRepository.delete(application);
        } else {
            throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StudyMemberListResponseDto getStudyMembers(User user, Long studyId) {
        Study study = studyRepository.findById(studyId).orElseThrow(StudyNotFoundException::new);
        if (!isStudyLeader(user, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        // 멤버 목록 조회
        List<StudyMember> members = studyMemberRepository.findByStudyWithUser(study);
        List<StudyMemberDetailDto> memberDtos = members.stream()
            .map(member -> new StudyMemberDetailDto(
                member.getUser().getNickname(),
                member.getRole().name(),
                null, // 확정된 멤버는 신청 메시지가 없음
                mapToUserDetailDto(member.getUser())
            ))
            .collect(Collectors.toList());

        // 신청자 목록 조회
        List<StudyApplication> applications = studyApplicationRepository.findByStudyWithUser(study);
        List<StudyMemberDetailDto> applicantDtos = applications.stream()
            .map(app -> new StudyMemberDetailDto(
                app.getUser().getNickname(),
                "Pending", // 신청자는 역할 대신 Pending 상태 표시
                app.getMessage(),
                mapToUserDetailDto(app.getUser())
            ))
            .collect(Collectors.toList());

        List<StudyMemberDetailDto> combinedList = new ArrayList<>();
        combinedList.addAll(memberDtos);
        combinedList.addAll(applicantDtos);

        return new StudyMemberListResponseDto(combinedList);
    }

    @Override
    @Transactional
    public void kickMember(User user, Long studyId, Long memberId) {
        Study study = studyRepository.findById(studyId).orElseThrow(StudyNotFoundException::new);
        if (!isStudyLeader(user, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        StudyMember memberToKick = studyMemberRepository.findById(memberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 스터디장이 자신을 강퇴시키는지 확인
        if (memberToKick.getRole() == StudyMemberRole.LEADER) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_LEADER);
        }

        studyMemberRepository.delete(memberToKick);
    }

    @Override
    @Transactional
    public void delegateLeadership(User user, Long studyId, Long newLeaderMemberId) {
        Study study = studyRepository.findById(studyId).orElseThrow(StudyNotFoundException::new);
        if (!isStudyLeader(user, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        StudyMember currentLeaderMember = studyMemberRepository.findByStudyAndUser(study, user)
            .orElseThrow(
                () -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "현재 리더 정보를 찾을 수 없습니다."));

        StudyMember newLeaderMember = studyMemberRepository.findById(newLeaderMemberId)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND,
                "새로운 리더로 지정할 멤버를 찾을 수 없습니다."));

        if (!newLeaderMember.getStudy().getId().equals(studyId)
            || newLeaderMember.getRole() != StudyMemberRole.MEMBER) {
            throw new BusinessException(ErrorCode.INVALID_LEADER_DELEGATION_TARGET);
        }

        // 1. Study 엔티티의 리더 변경
        study.setLeader(newLeaderMember.getUser());
        // 2. 기존 리더의 역할을 멤버로 변경
        currentLeaderMember.updateRole(StudyMemberRole.MEMBER);
        // 3. 새로운 리더의 역할을 리더로 변경
        newLeaderMember.updateRole(StudyMemberRole.LEADER);
    }

    private UserDetailDto mapToUserDetailDto(User user) {
        return new UserDetailDto(
            user.getImage_key(),
            user.getGender().name(),
            user.getInterests().stream().map(ui -> ui.getCategory().getKrName())
                .collect(Collectors.toList()),
            user.getRegion().getKrName()
        );
    }

    private void validateApplication(Study study, User user) {
        // 1. 스터디장 본인 신청 여부 확인
        if (study.getLeader().equals(user)) {
            throw new AlreadyMemberException("스터디장은 자신의 스터디에 참여 신청할 수 없습니다.");
        }

        // 2. 스터디 모집 상태 확인
        if (study.getStatus() != StudyStatus.RECRUITING) {
            throw new StudyNotRecruitingException();
        }

        // 3. 이미 확정된 멤버인지 확인
        if (studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new AlreadyMemberException();
        }

        // 4. 이미 신청 후 대기중인 상태인지 확인
        if (studyApplicationRepository.existsByStudyAndUserAndStatus(study, user,
            StudyApplicationStatus.PENDING)) {
            throw new AlreadyAppliedException();
        }

        // 5. 스터디 인원 확인 (현재 멤버 수 >= 최대 멤버 수)
        long currentMembers = studyMemberRepository.countByStudy(study);
        if (currentMembers >= study.getMaxMembers()) {
            throw new StudyFullException();
        }
    }
}