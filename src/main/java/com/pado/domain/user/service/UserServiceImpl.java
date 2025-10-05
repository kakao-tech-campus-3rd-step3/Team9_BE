package com.pado.domain.user.service;

import com.pado.domain.study.dto.response.MyApplicationResponseDto;
import com.pado.domain.study.dto.response.MyStudyResponseDto;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyApplication;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyApplicationRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.dto.UserDetailResponseDto;
import com.pado.domain.user.dto.UserSimpleResponseDto;
import com.pado.domain.user.dto.UserStudyResponseDto;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyApplicationRepository studyApplicationRepository;

    @Override
    public UserSimpleResponseDto getUserSimple(User user) {
        return new UserSimpleResponseDto(user.getNickname(), user.getImage_key());
    }

    @Transactional(readOnly = true)
    @Override
    public UserDetailResponseDto getUserDetail(Long userId) {
        //영속성 컨텍스트 문제로 User를 바로 사용했을 시 Userinterest 조회가 안됨
        User user = userRepository.findWithInterestsById(userId)
            .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        return new UserDetailResponseDto(
            user.getNickname(),
            user.getImage_key(),
            user.getInterests().stream()
                .map(ui -> ui.getCategory().name())
                .toList(),
            user.getRegion()
        );
    }

    @Override
    public UserStudyResponseDto getUserStudy(Long studyId, User user) {
        Study study = studyRepository.findById(studyId).orElseThrow(
            () -> new BusinessException(ErrorCode.STUDY_NOT_FOUND)
        );
        StudyMember studyMember = studyMemberRepository.findByStudyIdAndUserId(studyId,
            user.getId()).orElseThrow(
            () -> new BusinessException(ErrorCode.USER_NOT_FOUND)
        );
        return new UserStudyResponseDto(user.getNickname(), user.getImage_key(), study.getTitle(),
            studyMember.getRole());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyApplicationResponseDto> getMyApplications(User user) {
        List<StudyApplication> applications = studyApplicationRepository.findByUserWithStudy(user);
        return applications.stream()
            .map(app -> MyApplicationResponseDto.builder()
                .applicationId(app.getId())
                .studyId(app.getStudy().getId())
                .studyTitle(app.getStudy().getTitle())
                .status(app.getStatus().name())
                .message(app.getMessage())
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyStudyResponseDto> findMyStudies(Long userId) {
        List<Study> studies = studyRepository.findByUserId(userId);
        return studies.stream()
            .map(study -> new MyStudyResponseDto(
                study.getId(),
                study.getTitle()
            ))
            .collect(Collectors.toList());
    }
}