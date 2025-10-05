package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.reflection.dto.ReflectionListResponseDto;
import com.pado.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

public interface ReflectionService {

    ReflectionResponseDto createReflection(Long studyId, User user,
        ReflectionCreateRequestDto request);

    ReflectionListResponseDto getReflections(Long studyId, User user, String author,
        Pageable pageable);

    ReflectionResponseDto getReflection(Long studyId, Long reflectionId, User user);

    ReflectionResponseDto updateReflection(Long studyId, Long reflectionId, User user,
        ReflectionCreateRequestDto request);

    void deleteReflection(Long studyId, Long reflectionId, User user);
}