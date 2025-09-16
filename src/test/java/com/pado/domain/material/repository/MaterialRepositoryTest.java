package com.pado.domain.material.repository;

import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.config.QueryDslConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
class MaterialRepositoryTest {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRepository studyRepository;

    private User testUser;
    private Study testStudy;
    private Material learningMaterial1;
    private Material learningMaterial2;
    private Material noticeMaterial;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .passwordHash("password")
                .gender(Gender.MALE)
                .build();
        testUser = userRepository.save(testUser);

        testStudy = Study.builder()
                .title("Test Study")
                .description("Test Description")
                .maxMembers(10)
                .region(com.pado.domain.shared.entity.Region.SEOUL)
                .leader(testUser)
                .build();
        testStudy = studyRepository.save(testStudy);

        learningMaterial1 = new Material(
                "학습자료 1주차",
                MaterialCategory.LEARNING,
                1,
                "1주차 학습 내용입니다.",
                testStudy,
                testUser
        );

        learningMaterial2 = new Material(
                "학습자료 2주차",
                MaterialCategory.LEARNING,
                2,
                "2주차 학습 내용입니다.",
                testStudy,
                testUser
        );

        noticeMaterial = new Material(
                "중요 공지사항",
                MaterialCategory.NOTICE,
                null,
                "공지 내용입니다.",
                testStudy,
                testUser
        );

        materialRepository.saveAll(Arrays.asList(learningMaterial1, learningMaterial2, noticeMaterial));
    }

    @Test
    void 스터디ID로_자료_목록_최신순으로_조회() {
        // given
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                testStudy.getId(), null, null, null, pageable);
        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting("title")
                .containsExactly("중요 공지사항", "학습자료 2주차", "학습자료 1주차");
    }

    @Test
    void 자료ID로_자료목록_조회() {
        // given
        List<Long> ids = Arrays.asList(learningMaterial1.getId(), noticeMaterial.getId());

        // when
        List<Material> result = materialRepository.findByIdIn(ids);

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("title")
                .containsExactly("학습자료 1주차", "중요 공지사항");
    }

    @Test
    void 카테고리_필터로_자료_조회_최신순() {
        // given
        List<MaterialCategory> categories = List.of(MaterialCategory.LEARNING);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                testStudy.getId(), categories, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("materialCategory")
                .containsOnly(MaterialCategory.LEARNING);
    }

    @Test
    void 주차_필터로_자료_조회() {
        // given
        List<String> weeks = List.of("1");
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                testStudy.getId(), null, weeks, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("title")
                .containsExactlyInAnyOrder("학습자료 1주차", "중요 공지사항");
    }

    @Test
    void 키워드로_자료_검색() {
        // given
        String keyword = "공지";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                testStudy.getId(), null, null, keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).contains("공지");
    }

    @Test
    void 복합_필터로_자료_조회() {
        // given
        List<MaterialCategory> categories = List.of(MaterialCategory.LEARNING);
        List<String> weeks = Arrays.asList("1", "2");
        String keyword = "학습";
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                testStudy.getId(), categories, weeks, keyword, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("materialCategory")
                .containsOnly(MaterialCategory.LEARNING);
        assertThat(result.getContent()).extracting("title")
                .allMatch(title -> ((String) title).contains("학습"));
    }

    @Test
    void 존재하지_않는_스터디_ID로_목록_조회() {
        // given
        Long nonExistentStudyId = 999L;
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Material> result = materialRepository.findByStudyIdWithFiltersAndKeyword(
                nonExistentStudyId, null, null, null, pageable);
        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 존재하지_않는_자료ID_목록으로_조회() {
        // given
        List<Long> nonExistentIds = Arrays.asList(999L, 1000L);

        // when
        List<Material> result = materialRepository.findByIdIn(nonExistentIds);

        // then
        assertThat(result).isEmpty();
    }
}
