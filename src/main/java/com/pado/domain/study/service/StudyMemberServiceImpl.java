package com.pado.domain.study.service;

import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.dto.request.StudyMemberRoleChangeRequestDto;
import com.pado.domain.study.dto.response.StudyMemberDetailDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.*;
import com.pado.domain.study.repository.StudyApplicationRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            StudyApplicationStatusChangeRequestDto request)
    {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        StudyApplication application = studyApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_APPLICATION_NOT_FOUND));

        if (!isStudyLeader(user, study)){
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        StudyApplicationStatus newRole = StudyApplicationStatus.fromString(request.status());

        if (newRole.equals(StudyApplicationStatus.APPROVED)){
            StudyMember member = new StudyMember(study, application.getUser(), StudyMemberRole.MEMBER, application.getMessage(), 0);
            studyMemberRepository.save(member);
            studyApplicationRepository.delete(application);
        }
        else if (newRole.equals(StudyApplicationStatus.REJECTED)) {
            studyApplicationRepository.delete(application);
        }
        else {
            throw new BusinessException(ErrorCode.INVALID_STATE_CHANGE);
        }
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

