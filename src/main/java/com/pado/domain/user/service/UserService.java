package com.pado.domain.user.service;

import com.pado.domain.user.dto.UserDetailResponseDto;
import com.pado.domain.user.dto.UserSimpleResponseDto;
import com.pado.domain.user.entity.User;

public interface UserService {
    //유저 주요 정보 검색
    UserSimpleResponseDto getUserSimple(User user);
    UserDetailResponseDto getUserDetail(Long userId);
}
