package com.pado.domain.study.service;

import com.pado.domain.study.dto.request.StudyApplicationStatusChangeRequestDto;
import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.User;

public interface StudyMemberService {

    void applyToStudy(User user, Long studyId, StudyApplyRequestDto requestDto);

    boolean isStudyLeader(User user, Study study);

    boolean isStudyMember(User user, Long studyId);

    void updateApplicationStatus(User user, Long studyId, Long applicationId, StudyApplicationStatusChangeRequestDto request);
}