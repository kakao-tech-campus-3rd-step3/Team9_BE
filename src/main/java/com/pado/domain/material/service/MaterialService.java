package com.pado.domain.material.service;

import com.pado.domain.material.dto.request.MaterialRequestDto;
import com.pado.domain.material.dto.response.MaterialDetailResponseDto;
import com.pado.domain.material.dto.response.MaterialListResponseDto;
import com.pado.domain.material.dto.response.RecentMaterialResponseDto;
import com.pado.domain.user.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MaterialService {

    // 자료 생성
    MaterialDetailResponseDto createMaterial(User user , Long studyId, MaterialRequestDto request);

    // 자료 단일 조회
    MaterialDetailResponseDto findMaterialById(User user ,Long materialId);

    // 자료 목록 조회 (페이징, 카테고리 필터, 주차 필터, 키워드 검색)
    MaterialListResponseDto findAllMaterials(
            User user,
            Long studyId,
            List<String> categories,
            List<String> weeks,
            String keyword,
            Pageable pageable
    );

    // 자료 수정
    MaterialDetailResponseDto updateMaterial(User user, Long materialId, MaterialRequestDto request);

    // 자료 삭제 (단일 또는 다중)
    void deleteMaterial(User user, List<Long> ids);

    // 최신 자료 조회
    List<RecentMaterialResponseDto> findRecentLearningMaterials(Long studyId);
}
