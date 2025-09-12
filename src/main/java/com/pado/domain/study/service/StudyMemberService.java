package com.pado.domain.study.service;

import com.pado.domain.study.dto.request.StudyApplyRequestDto;
import com.pado.domain.user.entity.User;

public interface StudyMemberService {
    void applyToStudy(User user, Long studyId, StudyApplyRequestDto requestDto);
}