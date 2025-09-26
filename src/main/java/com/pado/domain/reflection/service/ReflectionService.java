package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.*;
import java.util.List;

public interface ReflectionService {

    ReflectionResponseDto createReflection(Long studyId, Long studyMemberId,
        ReflectionCreateRequestDto request);

    List<ReflectionResponseDto> getReflections(Long studyId);

    ReflectionResponseDto getReflection(Long reflectionId);

    ReflectionResponseDto updateReflection(Long reflectionId, ReflectionCreateRequestDto request);

    void deleteReflection(Long reflectionId);
}