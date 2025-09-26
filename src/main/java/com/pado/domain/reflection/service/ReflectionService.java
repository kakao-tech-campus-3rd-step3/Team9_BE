package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.user.entity.User;

import java.util.List;

public interface ReflectionService {

    ReflectionResponseDto createReflection(Long studyId, User user,
        ReflectionCreateRequestDto request);

    List<ReflectionResponseDto> getReflections(Long studyId, User user);

    ReflectionResponseDto getReflection(Long reflectionId, User user);

    ReflectionResponseDto updateReflection(Long reflectionId, User user,
        ReflectionCreateRequestDto request);

    void deleteReflection(Long reflectionId, User user);
}