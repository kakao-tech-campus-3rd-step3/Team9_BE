package com.pado.domain.study.service;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.request.StudyUpdateRequestDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.user.entity.User;

import java.util.List;

public interface StudyService {

    void createStudy(User user, StudyCreateRequestDto requestDto);

    StudyListResponseDto findStudies(User user, String keyword, List<Category> categories,
        List<Region> regions, int page, int size);

    StudyDetailResponseDto getStudyDetail(Long studyId);

    void leaveStudy(User user, Long studyId);

    void updateStudy(User user, Long studyId, StudyUpdateRequestDto requestDto);
}