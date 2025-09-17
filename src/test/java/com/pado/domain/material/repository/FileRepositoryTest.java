package com.pado.domain.material.repository;

import com.pado.domain.material.entity.File;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import com.pado.global.config.QueryDslConfig;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(QueryDslConfig.class)
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudyRepository studyRepository;

    private User testUser;
    private Study testStudy;
    private Material testMaterial1;
    private Material testMaterial2;
    private File file1;
    private File file2;
    private File file3;

    @BeforeEach
    void setUp() {
        // 테스트 사용자 생성
        testUser = User.builder()
                .email("test@example.com")
                .nickname("testUser")
                .passwordHash("password")
                .gender(Gender.MALE)
                .build();
        testUser = userRepository.save(testUser);

        // 테스트 스터디 생성
        testStudy = Study.builder()
                .title("Test Study")
                .description("Test Description")
                .maxMembers(10)
                .region(com.pado.domain.shared.entity.Region.SEOUL)
                .leader(testUser)
                .build();
        testStudy = studyRepository.save(testStudy);

        // 테스트 자료들 생성
        testMaterial1 = new Material(
                "자료 1",
                MaterialCategory.LEARNING,
                1,
                "내용 1",
                testStudy,
                testUser
        );

        testMaterial2 = new Material(
                "자료 2",
                MaterialCategory.NOTICE,
                null,
                "내용 2",
                testStudy,
                testUser
        );

        testMaterial1 = materialRepository.save(testMaterial1);
        testMaterial2 = materialRepository.save(testMaterial2);

        // 테스트 파일들 생성
        file1 = new File("file1.pdf", "s3-key-1", 1024L);
        file1.setMaterial(testMaterial1);

        file2 = new File("file2.docx", "s3-key-2", 1024L);
        file2.setMaterial(testMaterial1);

        file3 = new File("file3.jpg", "s3-key-3", 1024L);
        file3.setMaterial(testMaterial2);

        fileRepository.saveAll(Arrays.asList(file1, file2, file3));
    }

    @Test
    void 자료ID로_파일목록_조회() {
        // when
        List<File> result = fileRepository.findByMaterialId(testMaterial1.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting("name")
                .containsExactlyInAnyOrder("file1.pdf", "file2.docx");
        assertThat(result).extracting("fileKey")
                .containsExactlyInAnyOrder("s3-key-1", "s3-key-2");
    }

    @Test
    @DisplayName("자료 ID 목록으로 파일들 조회")
    void findByMaterialIdIn_Success() {
        // given
        List<Long> materialIds = Arrays.asList(testMaterial1.getId(), testMaterial2.getId());

        // when
        List<File> result = fileRepository.findByMaterialIdIn(materialIds);

        // then
        assertThat(result).hasSize(3);
        assertThat(result).extracting("name")
                .containsExactlyInAnyOrder("file1.pdf", "file2.docx", "file3.jpg");
    }

    @Test
    void 자료ID로_파일삭제() {
        // given
        Long materialId = testMaterial1.getId();

        // when
        fileRepository.deleteByMaterialId(materialId);

        // then
        List<File> remainingFiles = fileRepository.findByMaterialId(materialId);
        assertThat(remainingFiles).isEmpty();

        List<File> otherMaterialFiles = fileRepository.findByMaterialId(testMaterial2.getId());
        assertThat(otherMaterialFiles).hasSize(1);
    }

    @Test
    void 파일키_목록으로_파일삭제() {
        // given
        List<String> fileKeys = Arrays.asList("s3-key-1", "s3-key-3");

        // when
        fileRepository.deleteAllByFileKeyIn(fileKeys);

        // then
        List<File> remainingFiles = fileRepository.findAll();
        assertThat(remainingFiles).hasSize(1);
        assertThat(remainingFiles.getFirst().getFileKey()).isEqualTo("s3-key-2");
    }

    @Test
    void 존재하지_않는_자료ID로_파일조회() {
        // given
        Long nonExistentMaterialId = 999L;

        // when
        List<File> result = fileRepository.findByMaterialId(nonExistentMaterialId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 존재하지_않는_자료ID_목록으로_파일조회() {
        // given
        List<Long> nonExistentMaterialIds = Arrays.asList(999L, 1000L);

        // when
        List<File> result = fileRepository.findByMaterialIdIn(nonExistentMaterialIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 존재하지_않는_파일키로_삭제() {
        // given
        List<String> nonExistentFileKeys = Arrays.asList("non-existent-key-1", "non-existent-key-2");
        int initialCount = fileRepository.findAll().size();

        // when
        fileRepository.deleteAllByFileKeyIn(nonExistentFileKeys);

        // then
        List<File> remainingFiles = fileRepository.findAll();
        assertThat(remainingFiles).hasSize(initialCount);
    }

    @Test
    void 빈_자료ID_목록으로_파일_조회() {
        // given
        List<Long> emptyMaterialIds = Arrays.asList();

        // when
        List<File> result = fileRepository.findByMaterialIdIn(emptyMaterialIds);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void 빈_파일키_목록으로_삭제() {
        // given
        List<String> emptyFileKeys = Arrays.asList();
        int initialCount = fileRepository.findAll().size();

        // when
        fileRepository.deleteAllByFileKeyIn(emptyFileKeys);

        // then
        List<File> remainingFiles = fileRepository.findAll();
        assertThat(remainingFiles).hasSize(initialCount); // 개수 변화 없음
    }
}
