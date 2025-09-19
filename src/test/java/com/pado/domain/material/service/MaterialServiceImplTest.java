package com.pado.domain.material.service;

import com.pado.domain.material.dto.request.FileRequestDto;
import com.pado.domain.material.dto.request.MaterialRequestDto;
import com.pado.domain.material.dto.response.MaterialDetailResponseDto;
import com.pado.domain.material.dto.response.MaterialListResponseDto;
import com.pado.domain.material.dto.response.RecentMaterialResponseDto;
import com.pado.domain.material.entity.File;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.material.event.MaterialDeletedEvent;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.material.repository.MaterialRepository;
import com.pado.domain.s3.service.S3Service;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.entity.Gender;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaterialServiceImplTest {

    @InjectMocks
    private MaterialServiceImpl materialService;

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private FileRepository fileRepository;

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private User user;
    private Study study;
    private Material material;
    private File file;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .passwordHash("password")
                .gender(Gender.MALE)
                .build();
        ReflectionTestUtils.setField(user, "id", 1L);

        study = Study.builder()
                .title("testStudy")
                .description("Test Description")
                .maxMembers(10)
                .region(Region.SEOUL)
                .leader(user)
                .build();
        ReflectionTestUtils.setField(study, "id", 1L);

        material = new Material(
                "Test Title",
                MaterialCategory.LEARNING, 1,
                "Test Content",
                study,
                user);
        ReflectionTestUtils.setField(material, "id", 1L);

        file = new File(
                "Test File",
                "File Key",
                1024L
        );
        file.setMaterial(material);
        ReflectionTestUtils.setField(file, "id", 1L);
    }


    @Test
    void 자료생성_성공() {
        // given
        Long studyId = 1L;
        MaterialRequestDto request = new MaterialRequestDto(
                "Test Title",
                MaterialCategory.LEARNING,
                1,
                "Test Content",
                List.of(new FileRequestDto(null, "test.pdf", "test-key", 1024L))
        );

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(materialRepository.save(any(Material.class))).willReturn(material);
        given(fileRepository.saveAll(anyList())).willReturn(Arrays.asList(file));
        given(fileRepository.findByMaterialId(material.getId())).willReturn(Arrays.asList(file));

        // when
        MaterialDetailResponseDto result = materialService.createMaterial(user, studyId, request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Title");
        assertThat(result.category()).isEqualTo("학습자료");
        assertThat(result.week()).isEqualTo(1);
        assertThat(result.content()).isEqualTo("Test Content");
        assertThat(result.files()).hasSize(1);

        verify(materialRepository).save(any(Material.class));
        verify(fileRepository).saveAll(anyList());
    }

    @Test
    void 존재하지_않는_스터디로_인한_자료생성_실패() {
        // given
        Long studyId = 999L;
        MaterialRequestDto request = new MaterialRequestDto(
                "Test Title",
                MaterialCategory.LEARNING,
                1,
                "Test Content",
                Collections.emptyList()
        );

        given(studyRepository.findById(studyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> materialService.createMaterial(user, studyId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.STUDY_NOT_FOUND.message);
    }

    @Test
    void 스터디원이_아님으로_인한_자료생성_실패() {
        // given
        Long studyId = 1L;
        MaterialRequestDto request = new MaterialRequestDto(
                "Test Title",
                MaterialCategory.LEARNING,
                1,
                "Test Content",
                Collections.emptyList()
        );

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> materialService.createMaterial(user, studyId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY.message);
    }

    @Test
    void 자료_상세조회_성공() {
        // given
        Long materialId = 1L;

        given(materialRepository.findById(materialId)).willReturn(Optional.of(material));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(fileRepository.findByMaterialId(materialId)).willReturn(Arrays.asList(file));

        // when
        MaterialDetailResponseDto result = materialService.findMaterialById(user, materialId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(materialId);
        assertThat(result.title()).isEqualTo("Test Title");
        assertThat(result.files()).hasSize(1);
    }

    @Test
    void 존재하지_않는_자료로_인한_상세조회_실패() {
        // given
        Long materialId = 999L;

        given(materialRepository.findById(materialId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> materialService.findMaterialById(user, materialId))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATERIAL_NOT_FOUND.message);
    }

    @Test
    void 자료목록_조회_성공() {
        // given
        Long studyId = 1L;
        List<String> categories = List.of("학습자료");
        List<String> weeks = List.of("1");
        String keyword = "File Key";
        Pageable pageable = PageRequest.of(0, 10);

        Page<Material> materialPage = new PageImpl<>(Arrays.asList(material), pageable, 1);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(materialRepository.findByStudyIdWithFiltersAndKeyword(
                eq(studyId), anyList(), eq(weeks), eq(keyword), eq(pageable)))
                .willReturn(materialPage);
        given(fileRepository.findByMaterialIdIn(anyList())).willReturn(Arrays.asList(file));

        // when
        MaterialListResponseDto result = materialService.findAllMaterials(
                user, studyId, categories, weeks, keyword, pageable);

        // then
        assertThat(result).isNotNull();
        assertThat(result.materials()).hasSize(1);
        assertThat(result.hasNext()).isFalse();
    }

    @Test
    void 자료수정_성공() {
        // given
        Long materialId = 1L;
        MaterialRequestDto request = new MaterialRequestDto(
                "Updated Title",
                MaterialCategory.NOTICE,
                null,
                "Updated Content",
                Collections.emptyList()
        );

        given(materialRepository.findById(materialId)).willReturn(Optional.of(material));
        given(materialRepository.save(any(Material.class))).willReturn(material);
        given(fileRepository.findByMaterialId(materialId)).willReturn(Collections.emptyList());

        // when
        MaterialDetailResponseDto result = materialService.updateMaterial(user, materialId, request);

        // then
        assertThat(result).isNotNull();
        verify(materialRepository).save(any(Material.class));
    }

    @Test
    void 작성자가_아님으로_인한_자료수정_실패() {
        // given
        Long materialId = 1L;
        User otherUser = User.builder()
                .email("test2@example.com")
                .nickname("otherUser")
                .passwordHash("password")
                .gender(Gender.MALE)
                .build();
        MaterialRequestDto request = new MaterialRequestDto(
                "Updated Title",
                MaterialCategory.NOTICE,
                null,
                "Updated Content",
                Collections.emptyList()
        );

        given(materialRepository.findById(materialId)).willReturn(Optional.of(material));

        // when & then
        assertThatThrownBy(() -> materialService.updateMaterial(otherUser, materialId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_MATERIAL_ACCESS.message);
    }

    @Test
    void 자료삭제_성공() {
        // given
        List<Long> ids = List.of(1L);

        given(materialRepository.findByIdIn(ids)).willReturn(Arrays.asList(material));
        given(fileRepository.findByMaterialId(1L)).willReturn(Arrays.asList(file));

        // when
        materialService.deleteMaterial(user, ids);

        // then
        verify(fileRepository).deleteByMaterialId(1L);
        verify(materialRepository).deleteAll(anyList());
        verify(eventPublisher).publishEvent(any(MaterialDeletedEvent.class));
    }

    @Test
    void 존재하지_않는_자료로_인한_삭제_실패() {
        // given
        List<Long> ids = Arrays.asList(1L, 2L);

        given(materialRepository.findByIdIn(ids)).willReturn(Arrays.asList(material)); // 1개만 반환

        // when & then
        assertThatThrownBy(() -> materialService.deleteMaterial(user, ids))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.MATERIAL_NOT_FOUND.message);
    }

    @Test
    void 작성자가_이님으로_인한_삭제_실패() {
        // given
        List<Long> ids = List.of(1L);
        User otherUser = User.builder()
                .email("test2@example.com")
                .nickname("otherUser")
                .passwordHash("password")
                .gender(Gender.MALE)
                .build();

        given(materialRepository.findByIdIn(ids)).willReturn(Arrays.asList(material));

        // when & then
        assertThatThrownBy(() -> materialService.deleteMaterial(otherUser, ids))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.FORBIDDEN_MATERIAL_ACCESS.message);
    }

    @Test
    void 최신_학습_자료_목록_조회_성공() {
        // given
        Long studyId = 1L;
        List<RecentMaterialResponseDto> expectedList = List.of(
                new RecentMaterialResponseDto(101L, "자료1", "작성자1", 2L, 2048L),
                new RecentMaterialResponseDto(102L, "자료2", "작성자2", 1L, 1024L)
        );

        when(studyRepository.existsById(studyId)).thenReturn(true);

        when(materialRepository.findRecentLearningMaterialsAsDto(eq(studyId), anyInt()))
                .thenReturn(expectedList);

        // when
        List<RecentMaterialResponseDto> actualList = materialService.findRecentLearningMaterials(studyId);

        // then
        assertThat(actualList).isNotNull();
        assertThat(actualList).hasSize(2);
        assertThat(actualList.getFirst().material_title()).isEqualTo("자료1");
        assertThat(actualList).isEqualTo(expectedList);
    }

    @Test
    void 학습_자료가_없으면_빈_리스트_반환() {
        // given
        Long studyId = 1L;
        when(studyRepository.existsById(studyId)).thenReturn(true);
        when(materialRepository.findRecentLearningMaterialsAsDto(eq(studyId), anyInt()))
                .thenReturn(Collections.emptyList());

        // when
        List<RecentMaterialResponseDto> actualList = materialService.findRecentLearningMaterials(studyId);

        // then
        assertThat(actualList).isNotNull();
        assertThat(actualList).isEmpty();
    }

    @Test
    void 존재하지_않는_스터디ID로_조회_시_예외_발생() {
        // given
        Long studyId = 999L;
        when(studyRepository.existsById(studyId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> materialService.findRecentLearningMaterials(studyId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
    }
}
