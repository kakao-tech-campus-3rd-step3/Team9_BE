package com.pado.domain.material.service;

import com.pado.domain.material.dto.request.FileRequestDto;
import com.pado.domain.material.dto.request.MaterialRequestDto;
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
import com.pado.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialServiceImpl implements MaterialService {

    private final MaterialRepository materialRepository;
    private final FileRepository fileRepository;
    private final S3Service s3Service;

    // 자료 생성
    @Transactional
    @Override
    public MaterialDetailResponseDto createMaterial(Long studyId, MaterialRequestDto request) {
        // TODO: 토큰을 통해 실제 사용자 ID 가져오기
        Long userId = getCurrentUserId(); // 임시 메서드

        Material material = new Material(request.title(), request.category(), request.week(), request.content(), studyId, userId);

        Material savedMaterial = materialRepository.save(material);

        // 첨부파일 처리
        if (!request.files().isEmpty()) {
            List<File> fileEntities = request.files().stream()
                    .map(fileDto -> createFileEntity(fileDto, material))
                    .collect((Collectors.toList()));
            fileRepository.saveAll(fileEntities);
        }

        return convertToDetailResponseDto(savedMaterial);
    }

    // 자료 상세조회
    @Override
    public MaterialDetailResponseDto findMaterialById(Long materialId) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));

        return convertToDetailResponseDto(material);
    }

    // 자료 목록 조회
    @Override
    public MaterialListResponseDto findAllMaterials(
            Long studyId,
            List<String> categories,
            List<String> weeks,
            String keyword,
            Pageable pageable) {

        Page<Material> materialPage;
        Optional<List<MaterialCategory>> materialCategoriesOpt = Optional.empty();

        if (categories != null && !categories.isEmpty() && !categories.contains("전체")) {
            List<MaterialCategory> materialCategories = categories.stream()
                    .map(MaterialCategory::fromString)
                    .collect(Collectors.toList());
            materialCategoriesOpt = Optional.of(materialCategories);
        }

        // 필터 조건이 있는 경우 필터링 후 조회
        if (materialCategoriesOpt.isPresent() || weeks != null || keyword != null) {
            materialPage = materialRepository.findByStudyIdWithFiltersAndKeyword(
                    studyId, materialCategoriesOpt.orElse(null), weeks, keyword, pageable);
        } else {
            materialPage = materialRepository.findByStudyId(studyId, pageable);
        }

        List<MaterialSimpleResponseDto> materials = materialPage.getContent().stream()
                .map(this::convertToSimpleResponseDto)
                .collect(Collectors.toList());

        return new MaterialListResponseDto(materials, pageable.getPageNumber(), pageable.getPageSize(), materialPage.hasNext());
    }

    // 자료 수정
    @Transactional
    @Override
    public MaterialDetailResponseDto updateMaterial(Long materialId, MaterialRequestDto request) {
        // TODO: 토큰을 통해 실제 사용자 ID 가져오기
        Long userId = getCurrentUserId(); // 임시

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));

        if (!material.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_MATERIAL_ACCESS);
        }

        // 자료 정보 업데이트
        material.updateMaterial(request.title(), request.category(), request.week(), request.content());

        // 파일 업데이트
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

    // 파일 엔티티 생성 메서드
    private File createFileEntity(FileRequestDto fileDto, Material material) {
        File file = new File(fileDto.name(), fileDto.url());
        file.setMaterial(material);
        return file;
    }

    // 자료 수정 시 파일의 변경점이 있는 경우 생성 및 삭제 처리하는 메서드
    private void updateMaterialFiles(Long materialId, List<FileRequestDto> requestFiles) {
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

        // S3에서 파일 삭제 후 DB에서 삭제 (S3Service에서 예외 처리됨)
        if (!filesToDelete.isEmpty()) {
            filesToDelete.forEach(file -> s3Service.deleteFileByUrl(file.getUrl()));
            fileRepository.deleteAll(filesToDelete);
        }

        // 추가 실행
        if (!newFiles.isEmpty()) {
            fileRepository.saveAll(newFiles);
        }
    }

    // 자료 삭제 시, 자료가 존재하는지와 해당 자료가 본인이 작성한 글인지 검사하는 메서드
    private List<Material> validateMaterialsExistAndAccess(List<Long> ids, Long userId) {
        List<Material> materials = materialRepository.findByIdIn(ids);

        if (materials.size() != ids.size()) {
            throw new BusinessException(ErrorCode.MATERIAL_NOT_FOUND);
        }

        // 본인이 작성한 글인지 확인
        materials.forEach(
                material ->
                {
                    if (!material.getUserId().equals(userId)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN_MATERIAL_ACCESS);
                    }
                }
        );

        return materials;
    }

    // 자료 삭제 시 연관된 파일들을 S3에서 삭제
    private void deleteAssociatedFiles(List<Long> materialIds) {
        materialIds.forEach(materialId -> {
            List<File> files = fileRepository.findByMaterialId(materialId);
            
            // S3에서 파일 삭제
            files.forEach(file -> s3Service.deleteFileByUrl(file.getUrl()));

            fileRepository.deleteByMaterialId(materialId);
        });
    }

    // 유저의 ID를 가져오는 메서드
    private Long getCurrentUserId() {
        // TODO: 토큰을 통해 실제 사용자 ID 가져오기
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
                material.getWeek(),
                material.getContent(),
                material.getUserId(),
                "임시 닉네임",
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
                material.getWeek(),
                material.getUserId(),
                "임시 닉네임",
                dataUrls,
                material.getUpdatedAt()
        );
    }
}