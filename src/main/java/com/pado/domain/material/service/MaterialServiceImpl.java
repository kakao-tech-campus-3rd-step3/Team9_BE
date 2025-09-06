package com.pado.domain.material.service;

import com.pado.domain.material.dto.request.FilePresignedUrlRequestDto;
import com.pado.domain.material.dto.request.FileRequestDto;
import com.pado.domain.material.dto.request.MaterialRequestDto;
import com.pado.domain.material.dto.response.FilePresignedUrlResponseDto;
import com.pado.domain.material.dto.response.FileResponseDto;
import com.pado.domain.material.dto.response.MaterialDetailResponseDto;
import com.pado.domain.material.dto.response.MaterialListResponseDto;
import com.pado.domain.material.dto.response.MaterialSimpleResponseDto;
import com.pado.domain.material.entity.File;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.material.repository.MaterialRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final FileRepository fileRepository;
    // private final S3Service s3Service; // TODO: S3 연동 시 주입
    // private final SecurityService securityService; // TODO: 인증 서비스 연동 시 주입

    // S3로부터 임시 파일 URL 요청
    @Override
    public FilePresignedUrlResponseDto createPresignedUrl(FilePresignedUrlRequestDto request) {
        // TODO: S3Service 구현 후 활성화
        String mockUrl = "https://pado-bucket.s3.ap-northeast-2.amazonaws.com/materials/" + request.name();
        return new FilePresignedUrlResponseDto(mockUrl, request.name());
    }

    // 자료 생성
    @Transactional
    @Override
    public MaterialDetailResponseDto createMaterial(Long studyId, MaterialRequestDto request) {
        Long userId = getCurrentUserId(); // 임시 메서드

        // Material 엔티티 생성
        Material material = createMaterialEntity(request, studyId, userId);
        Material savedMaterial = materialRepository.save(material);

        // 첨부파일 처리
        saveFiles(savedMaterial, request.files());

        return convertToDetailResponseDto(savedMaterial);
    }

    // 자료 상세조회
    @Override
    public MaterialDetailResponseDto findMaterialById(Long materialId) {

        Material material = getMaterialById(materialId);
        return convertToDetailResponseDto(material);
    }

    // 자료 목록 조회
    @Override
    public MaterialListResponseDto findAllMaterials(Long studyId, List<String> categories, int page, int size) {
        log.info("Finding materials for study: {}, categories: {}, page: {}, size: {}",
                studyId, categories, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Material> materialPage = findMaterialsWithFiltering(studyId, categories, pageable);

        List<MaterialSimpleResponseDto> materials = materialPage.getContent().stream()
                .map(this::convertToSimpleResponseDto)
                .collect(Collectors.toList());

        return new MaterialListResponseDto(materials, page, size, materialPage.hasNext());
    }

    // 자료 수정
    @Transactional
    @Override
    public MaterialDetailResponseDto updateMaterial(Long materialId, MaterialRequestDto request) {
        Long userId = getCurrentUserId();

        Material material = getMaterialById(materialId);
        validateMaterialAccess(material, userId);

        // 자료 정보 업데이트
        updateMaterialInfo(material, request);

        // 파일 차분 업데이트
        updateMaterialFiles(materialId, request.files());

        Material updatedMaterial = materialRepository.save(material);

        return convertToDetailResponseDto(updatedMaterial);
    }

    // 자료 삭제
    @Transactional
    @Override
    public void deleteMaterial(List<Long> ids) {
        Long userId = getCurrentUserId();

        List<Material> materials = validateMaterialsExistAndAccess(ids, userId);

        // 연관된 파일들 먼저 삭제 후 자료 삭제
        deleteAssociatedFiles(ids);
        materialRepository.deleteAll(materials);
    }

    // 자료 엔티티 생성 메서드
    private Material createMaterialEntity(MaterialRequestDto request, Long studyId, Long userId) {
        MaterialCategory category = MaterialCategory.fromString(request.category());
        return new Material(request.title(), category, request.content(), studyId, userId);
    }

    // 파일 저장 메서드
    private void saveFiles(Material material, List<FileRequestDto> fileRequests) {
        if (fileRequests == null || fileRequests.isEmpty()) {
            return;
        }

        List<File> files = fileRequests.stream()
                .map(fileDto -> createFileEntity(fileDto, material))
                .collect(Collectors.toList());

        fileRepository.saveAll(files);
    }

    // 파일 엔티티 생성 메서드
    private File createFileEntity(FileRequestDto fileDto, Material material) {
        File file = new File(fileDto.name(), fileDto.url());
        file.setMaterial(material);
        return file;
    }

    // 해당하는 카테고리의 자료들 조회
    private Page<Material> findMaterialsWithFiltering(Long studyId, List<String> categories, Pageable pageable) {
        if (categories != null && !categories.isEmpty()) {
            List<MaterialCategory> materialCategories = categories.stream()
                    .map(MaterialCategory::fromString)
                    .collect(Collectors.toList());

            return materialRepository.findByStudyIdAndMaterialCategoryInOrderByCreatedAtDesc(
                    studyId, materialCategories, pageable);
        }

        return materialRepository.findByStudyIdOrderByCreatedAtDesc(studyId, pageable);
    }

    // 자료 정보 수정 메서드
    private void updateMaterialInfo(Material material, MaterialRequestDto request) {
        MaterialCategory category = MaterialCategory.fromString(request.category());
        material.updateMaterial(request.title(), category, request.content());
    }

    // 자료 수정 시 연관된 파일 수정 메서드
    private void updateMaterialFiles(Long materialId, List<FileRequestDto> requestFiles) {

        // 변경사항 X
        if (requestFiles == null) {
            return;
        }

        // 모든 파일 삭제
        if (requestFiles.isEmpty()) {
            fileRepository.deleteByMaterialId(materialId);
            return;
        }

        // 일부 파일만 삭제 및 추가
        processDifferentialFileUpdate(materialId, requestFiles);
    }

    // 자료 수정 시 파일의 변경점이 있는 경우 생성 및 삭제 처리하는 메서드
    private void processDifferentialFileUpdate(Long materialId, List<FileRequestDto> requestFiles) {
        List<File> currentFiles = fileRepository.findByMaterialId(materialId);

        // 요청에 포함된 기존 파일 ID들
        Set<Long> requestFileIds = requestFiles.stream()
                .map(FileRequestDto::id)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 삭제할 파일들 (요청에 없는 기존 파일들)
        List<File> filesToDelete = currentFiles.stream()
                .filter(file -> !requestFileIds.contains(file.getId()))
                .collect(Collectors.toList());

        // 새로 추가할 파일들 (ID가 null인 파일들)
        List<File> newFiles = requestFiles.stream()
                .filter(fileDto -> fileDto.id() == null)
                .map(fileDto -> createFileEntity(fileDto, materialRepository.getReferenceById(materialId)))
                .collect(Collectors.toList());

        // 삭제 및 추가 실행
        if (!filesToDelete.isEmpty()) {
            fileRepository.deleteAll(filesToDelete);
        }

        if (!newFiles.isEmpty()) {
            fileRepository.saveAll(newFiles);
        }
    }

    // 자료 ID로 특정 자료 조회
    private Material getMaterialById(Long materialId) {

        return materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));
    }

    // 유저의 권한 확인 메서드
    private void validateMaterialAccess(Material material, Long userId) {
        // TODO: 추후 스터디 리더 권한 확인 로직 추가
        if (!material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MATERIAL_ACCESS);
        }
    }

    // 자료가 존재하고 접근 가능한지 검사하는 메서드
    private List<Material> validateMaterialsExistAndAccess(List<Long> ids, Long userId) {
        List<Material> materials = materialRepository.findByIdIn(ids);

        if (materials.size() != ids.size()) {
            throw new BusinessException(ErrorCode.MATERIAL_NOT_FOUND);
        }

        // 권한 확인
        materials.forEach(material -> validateMaterialAccess(material, userId));

        return materials;
    }

    // 해당 자료들과 관련된 파일들을 삭제하는 메서드
    private void deleteAssociatedFiles(List<Long> materialIds) {

        materialIds.forEach(fileRepository::deleteByMaterialId);
    }

    // 유저의 ID를 가져오는 메서드
    private Long getCurrentUserId() {
        // TODO: SecurityContext에서 실제 사용자 ID 가져오기
        return 1L;
    }

    // 자료 상세 조회 DTO 변환 메서드
    private MaterialDetailResponseDto convertToDetailResponseDto(Material material) {
        List<File> files = fileRepository.findByMaterialId(material.getId());
        List<FileResponseDto> fileResponseDtos = files.stream()
                .map(file -> new FileResponseDto(file.getId(), file.getName(), file.getUrl()))
                .collect(Collectors.toList());

        return new MaterialDetailResponseDto(
                material.getId(),
                material.getTitle(),
                material.getMaterialCategory().name,
                material.getContent(),
                material.getCreatedAt(),
                material.getUpdatedAt(),
                fileResponseDtos
        );
    }

    // 자료 목록 조회 DTO 변환 메서드
    private MaterialSimpleResponseDto convertToSimpleResponseDto(Material material) {
        List<File> files = fileRepository.findByMaterialId(material.getId());
        List<String> dataUrls = files.stream()
                .map(File::getUrl)
                .collect(Collectors.toList());

        return new MaterialSimpleResponseDto(
                material.getId(),
                material.getTitle(),
                material.getMaterialCategory().name,
                dataUrls,
                material.getUpdatedAt()
        );
    }
}