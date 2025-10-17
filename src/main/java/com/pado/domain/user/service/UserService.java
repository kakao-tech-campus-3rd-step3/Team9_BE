package com.pado.domain.user.service;

import com.pado.domain.study.dto.response.MyApplicationResponseDto;
import com.pado.domain.study.dto.response.MyStudyResponseDto;
import com.pado.domain.user.dto.UserDetailResponseDto;
import com.pado.domain.user.dto.UserSimpleResponseDto;
import com.pado.domain.user.dto.UserStudyResponseDto;
import com.pado.domain.user.entity.User;

import java.util.List;

public interface UserService {

    //유저 주요 정보 검색
    UserSimpleResponseDto getUserSimple(User user);

    //유저 세부 정보 검색
    UserDetailResponseDto getUserDetail(Long userId);

    //유저 스터디 정보 검색
    UserStudyResponseDto getUserStudy(Long studyId, User user);

    List<MyApplicationResponseDto> getMyApplications(User user);

    List<MyStudyResponseDto> findMyStudies(Long userId);
}