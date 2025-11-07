package com.pado.domain.study.service;

import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.LastReadMessage;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.chat.repository.LastReadMessageRepository;
import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.dto.response.StudyApplicantDetailDto;
import com.pado.domain.study.dto.response.StudyApplicantListResponseDto;
import com.pado.domain.study.dto.response.StudyMemberDetailDto;
import com.pado.domain.study.dto.response.StudyMemberListResponseDto;
import com.pado.domain.study.dto.response.UserDetailDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.*;
import com.pado.domain.study.repository.StudyApplicationRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StudyMemberServiceImpl implements StudyMemberService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyApplicationRepository studyApplicationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final LastReadMessageRepository lastReadMessageRepository;

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

        StudyApplicationStatus newStatus = StudyApplicationStatus.fromString(request.status());
        if (newStatus.equals(StudyApplicationStatus.APPROVED)) {
            StudyMember member = new StudyMember(study, application.getUser(),
                StudyMemberRole.MEMBER, application.getMessage(), 0);
            studyMemberRepository.save(member);
            studyApplicationRepository.delete(application);

            Optional<ChatMessage> latestMessage = chatMessageRepository.findTopByStudyIdOrderByIdDesc(
                studyId);
            long latestMessageId = latestMessage.map(ChatMessage::getId).orElse(0L);

            LastReadMessage lastReadMessage = new LastReadMessage(member, latestMessageId);
            lastReadMessageRepository.save(lastReadMessage);

        } else if (newStatus.equals(StudyApplicationStatus.REJECTED)) {
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

        List<StudyMember> members = studyMemberRepository.findByStudyWithUser(study);
        List<StudyMemberDetailDto> memberDtos = members.stream()
            .map(member -> new StudyMemberDetailDto(
                member.getId(),
                member.getUser().getNickname(),
                member.getRole().name(),
                null,
                mapToUserDetailDto(member.getUser())
            ))
            .collect(Collectors.toList());

        return new StudyMemberListResponseDto(memberDtos);
    }

    @Override
    @Transactional(readOnly = true)
    public StudyApplicantListResponseDto getStudyApplicants(User user, Long studyId) {
        Study study = studyRepository.findById(studyId).orElseThrow(StudyNotFoundException::new);
        if (!isStudyLeader(user, study)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        List<StudyApplication> applications = studyApplicationRepository.findByStudyWithUser(study);
        List<StudyApplicantDetailDto> applicantDtos = applications.stream()
            .filter(app -> app.getStatus() == StudyApplicationStatus.PENDING)
            .map(app -> new StudyApplicantDetailDto(
                app.getId(),
                app.getUser().getNickname(),
                app.getMessage(),
                mapToUserDetailDto(app.getUser()),
                app.getCreatedAt()
            ))
            .collect(Collectors.toList());

        return new StudyApplicantListResponseDto(applicantDtos);
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

        if (memberToKick.getRole() == StudyMemberRole.LEADER) {
            throw new BusinessException(ErrorCode.CANNOT_KICK_LEADER);
        }

        if (!memberToKick.getStudy().getId().equals(studyId)) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "해당 스터디의 멤버가 아닙니다.");
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

        study.setLeader(newLeaderMember.getUser());
        currentLeaderMember.updateRole(StudyMemberRole.MEMBER);
        newLeaderMember.updateRole(StudyMemberRole.LEADER);
    }

    @Override
    @Transactional
    public void cancelApplication(User user, Long studyId) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(StudyNotFoundException::new);

        StudyApplication application = studyApplicationRepository.findByStudyAndUserAndStatus(study,
                user, StudyApplicationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_APPLICATION_NOT_FOUND,
                "대기 중인 스터디 신청이 없습니다."));

        studyApplicationRepository.delete(application);
    }

    private UserDetailDto mapToUserDetailDto(User user) {
        List<String> interestNames = user.getInterests() == null ? List.of() :
            user.getInterests().stream()
                .map(ui -> ui.getCategory().getKrName())
                .collect(Collectors.toList());

        return new UserDetailDto(
            user.getImage_key(),
            user.getGender().name(),
            interestNames,
            user.getRegion().getKrName()
        );
    }


    private void validateApplication(Study study, User user) {
        if (study.getLeader().equals(user)) {
            throw new AlreadyMemberException("스터디장은 자신의 스터디에 참여 신청할 수 없습니다.");
        }
        if (study.getStatus() != StudyStatus.RECRUITING) {
            throw new StudyNotRecruitingException();
        }
        if (studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new AlreadyMemberException();
        }
        if (studyApplicationRepository.existsByStudyAndUserAndStatus(study, user,
            StudyApplicationStatus.PENDING)) {
            throw new AlreadyAppliedException();
        }
        long currentMembers = studyMemberRepository.countByStudy(study);
        if (currentMembers >= study.getMaxMembers()) {
            throw new StudyFullException();
        }
    }
}