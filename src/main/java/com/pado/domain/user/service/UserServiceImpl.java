package com.pado.domain.user.service;

import com.pado.domain.user.dto.UserDetailResponseDto;
import com.pado.domain.user.dto.UserSimpleResponseDto;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

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
}
