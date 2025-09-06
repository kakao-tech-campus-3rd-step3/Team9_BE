package com.pado.domain.material.service;

import com.pado.domain.material.dto.request.FilePresignedUrlRequestDto;
import com.pado.domain.material.dto.request.MaterialRequestDto;
import com.pado.domain.material.dto.response.FilePresignedUrlResponseDto;
import com.pado.domain.material.dto.response.MaterialDetailResponseDto;
import com.pado.domain.material.dto.response.MaterialListResponseDto;

import java.util.List;

public interface MaterialService {

    // S3 Pre-signed URL 생성 (추후 S3Service 구현 시 활성화)
    FilePresignedUrlResponseDto createPresignedUrl(FilePresignedUrlRequestDto request);
    
    // 자료 생성
    MaterialDetailResponseDto createMaterial(Long studyId, MaterialRequestDto request);
    
    // 자료 단일 조회
    MaterialDetailResponseDto findMaterialById(Long materialId);
    
    // 자료 목록 조회 (페이징, 카테고리 필터)
    MaterialListResponseDto findAllMaterials(Long studyId, List<String> category, int page, int size);
    
    // 자료 수정
    MaterialDetailResponseDto updateMaterial(Long materialId, MaterialRequestDto request);
    
    // 자료 삭제 (단일 또는 다중)
    void deleteMaterial(List<Long> ids);
}
