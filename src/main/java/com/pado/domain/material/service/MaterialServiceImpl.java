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
import com.pado.domain.material.event.MaterialDeletedEvent;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.material.repository.MaterialRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.domain.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    // 자료 생성
    @Transactional
    @Override
    public MaterialDetailResponseDto createMaterial(User user, Long studyId, MaterialRequestDto request) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        Material material = new Material(request.title(), request.category(), request.week(), request.content(), study, user);

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
    public MaterialDetailResponseDto findMaterialById(User user, Long materialId) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));

        Study study = material.getStudy();

        if (!studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

        return convertToDetailResponseDto(material);
    }

    // 자료 목록 조회
    @Override
    public MaterialListResponseDto findAllMaterials(
            User user,
            Long studyId,
            List<String> categories,
            List<String> weeks,
            String keyword,
            Pageable pageable) {

        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        if (!studyMemberRepository.existsByStudyAndUser(study, user)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }

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

        // 자료에서 id만 추출
        List<Long> materialIds = materialPage.stream()
                .map(Material::getId)
                .toList();

        // 파일목록 조회
        List<File> files = fileRepository.findByMaterialIdIn(materialIds);

        // 자료 id와 파일 리스트들을 Map을 통해 관리
        Map<Long, List<File>> filesByMaterialIdMap = files.stream()
                .collect(Collectors.groupingBy(file -> file.getMaterial().getId()));

        List<MaterialSimpleResponseDto> materials =  materialPage.getContent().stream()
                .map(material -> {
                    List<File> mappedFiles = filesByMaterialIdMap.getOrDefault(material.getId(), Collections.emptyList());
                    return convertToSimpleResponseDto(material, mappedFiles);
                })
                .toList();

        return new MaterialListResponseDto(materials, pageable.getPageNumber(), pageable.getPageSize(), materialPage.hasNext());
    }

    // 자료 수정
    @Transactional
    @Override
    public MaterialDetailResponseDto updateMaterial(User user, Long materialId, MaterialRequestDto request) {

        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATERIAL_NOT_FOUND));

        if (!material.isOwnedBy(user)) {
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
    public void deleteMaterial(User user, List<Long> ids) {

        List<Material> materials = validateMaterialsExistAndAccess(ids, user);

        // 삭제할 파일 키 수집
        List<String> fileKeys = new ArrayList<>();
        ids.forEach(id -> {
            List<File> files = fileRepository.findByMaterialId(id);

            files.forEach((file -> fileKeys.add(file.getFileKey())));
            fileRepository.deleteByMaterialId(id);
        });

        materialRepository.deleteAll(materials);

        // 자료, 파일이 모두 삭제되면 S3 버킷에 있는 파일들을 삭제할 이벤트 등록
        if (!fileKeys.isEmpty()) {
            eventPublisher.publishEvent(new MaterialDeletedEvent(fileKeys));
        }
    }

    // 파일 엔티티 생성 메서드
    private File createFileEntity(FileRequestDto fileDto, Material material) {
        File file = new File(fileDto.name(), fileDto.key());
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

        // 삭제할 파일의 url 리스트 (요청에 없는 기존 파일들)
        List<String> fileKeysToDelete = currentFiles.stream()
                .filter(file -> !requestFileIds.contains(file.getId()))
                .map(File::getFileKey)
                .toList();

        // 새로 추가할 파일들 (ID가 null인 파일들)
        List<File> newFiles = requestFiles.stream()
                .filter(fileDto -> fileDto.id() == null)
                .map(fileDto -> createFileEntity(fileDto, materialRepository.getReferenceById(materialId)))
                .collect(Collectors.toList());

        // DB에서 파일 삭제 후 이벤트 발생시켜 s3에 있는 파일 삭제
        if (!fileKeysToDelete.isEmpty()) {
            fileRepository.deleteAllByFileKeyIn(fileKeysToDelete);
        }
        eventPublisher.publishEvent(new MaterialDeletedEvent(fileKeysToDelete));

        // 추가 실행
        if (!newFiles.isEmpty()) {
            fileRepository.saveAll(newFiles);
        }
    }

    // 자료 삭제 시, 자료가 존재하는지와 해당 자료가 본인이 작성한 글인지 검사하는 메서드
    private List<Material> validateMaterialsExistAndAccess(List<Long> ids, User user) {
        List<Material> materials = materialRepository.findByIdIn(ids);

        if (materials.size() != ids.size()) {
            throw new BusinessException(ErrorCode.MATERIAL_NOT_FOUND);
        }

        // 본인이 작성한 글인지 확인
        materials.forEach(
                material ->
                {
                    if (!material.isOwnedBy(user)) {
                        throw new BusinessException(ErrorCode.FORBIDDEN_MATERIAL_ACCESS);
                    }
                }
        );

        return materials;
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
                .map(file -> new FileResponseDto(file.getId(), file.getName(), file.getFileKey()))
                .collect(Collectors.toList());

        return new MaterialDetailResponseDto(
                material.getId(),
                material.getTitle(),
                material.getMaterialCategory().name,
                material.getWeek(),
                material.getContent(),
                material.getUser().getId(),
                "임시 닉네임",
                material.getCreatedAt(),
                material.getUpdatedAt(),
                fileResponseDtos
        );
    }

    // 자료 목록 조회 DTO 변환 메서드
    private MaterialSimpleResponseDto convertToSimpleResponseDto(Material material, List<File> files) {
        List<String> dataUrls = files.stream()
                .map(File::getFileKey)
                .collect(Collectors.toList());

        return new MaterialSimpleResponseDto(
                material.getId(),
                material.getTitle(),
                material.getMaterialCategory().name,
                material.getWeek(),
                material.getUser().getId(),
                "임시 닉네임",
                dataUrls,
                material.getCreatedAt()
        );
    }
}