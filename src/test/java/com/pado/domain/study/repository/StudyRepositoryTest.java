package com.pado.domain.study.repository;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.config.QueryDslConfig;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(QueryDslConfig.class)
class StudyRepositoryTest {

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private User testUser;
    private Study study1, study2, study3, study4;

    // 영속성 컨텍스트 변경 사항 DB에 즉시 반영 후 캐시 초기화
    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = User.builder()
                .email("test@test.com")
                .passwordHash("1234")
                .nickname("tester")
                .gender(Gender.MALE)
                .region(Region.SEOUL)
                .build();

        // 관심사 추가
        testUser.addInterest(Category.PROGRAMMING);
        testUser.addInterest(Category.EMPLOYMENT);

        userRepository.save(testUser);

        // 테스트용 스터디 데이터 생성
        // study1: 관심사 2/2(50) + 지역(30) + 최신(30) = 110점
        study1 = Study.builder()
                .title("스프링 완전 정복")
                .description("설명1")
                .leader(testUser)
                .region(Region.SEOUL)
                .maxMembers(10)
                .build();
        study1.addInterests(List.of(Category.PROGRAMMING, Category.EMPLOYMENT));

        // study2: 관심사 1/2(25) + 지역(0) + 최신(30) = 55점
        study2 = Study.builder()
                .title("JPA 스터디")
                .description("설명2")
                .leader(testUser)
                .region(Region.BUSAN)
                .maxMembers(10)
                .build();
        study2.addInterests(List.of(Category.PROGRAMMING));

        // study3: 관심사 1/2(25) + 지역(30) + 최신(30) = 85점
        study3 = Study.builder()
                .title("영어 회화 스터디")
                .description("설명3")
                .leader(testUser)
                .region(Region.SEOUL)
                .maxMembers(10)
                .build();
        study3.addInterests(List.of( Category.EMPLOYMENT, Category.LANGUAGE));

        // study4: 관심사 1/2(25) + 지역(0) + 최신(30) = 55점
        study4 = Study.builder()
                .title("독서 모임")
                .description("설명4")
                .leader(testUser)
                .region(Region.DAEGU)
                .maxMembers(10)
                .build();
        study4.addInterests(List.of(Category.EMPLOYMENT));

        // 모든 스터디 DB 저장 후 flush & clear : study4가 가장 최신
        studyRepository.saveAll(List.of(study1, study2, study3, study4));
        flushAndClear();
    }

    @Test
    void 로그인사용자_필터없음_추천점수_최신순으로정렬() {
        // when
        Pageable pageable = PageRequest.of(0, 10);
        Slice<Study> result = studyRepository.findStudiesByFilter(testUser, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("스프링 완전 정복", "영어 회화 스터디", "독서 모임", "JPA 스터디");
    }

    @Test
    void 로그인사용자_필터있음_필터링된결과_추천점수순으로정렬() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Region> regionFilter = List.of(Region.SEOUL, Region.BUSAN);
        List<Category> categoryFilter = List.of(Category.PROGRAMMING, Category.LANGUAGE);

        // when
        Slice<Study> result = studyRepository.findStudiesByFilter(testUser, null, categoryFilter, regionFilter, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("스프링 완전 정복", "영어 회화 스터디", "JPA 스터디");
    }

    @Test
    void 비로그인사용자_필터없음_최신순으로정렬() {
        // given
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Slice<Study> result = studyRepository.findStudiesByFilter(null, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(4);
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("독서 모임", "영어 회화 스터디", "JPA 스터디", "스프링 완전 정복");
    }

    @Test
    void 비로그인사용자_필터있음_필터링된결과_최신순으로정렬() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        List<Category> categoryFilter = List.of(Category.EMPLOYMENT);

        // when
        Slice<Study> result = studyRepository.findStudiesByFilter(null, null, categoryFilter, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("독서 모임", "영어 회화 스터디", "스프링 완전 정복");
    }

    @Test
    void 페이징_다음페이지존재() {
        // given
        Pageable pageable = PageRequest.of(0, 2);

        // when
        Slice<Study> result = studyRepository.findStudiesByFilter(testUser, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isTrue();
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("스프링 완전 정복", "영어 회화 스터디");
    }

    @Test
    void 페이징_다음페이지없음() {
        // given
        Pageable pageable = PageRequest.of(1, 2);

        // when
        Slice<Study> result = studyRepository.findStudiesByFilter(testUser, null, null, null, pageable);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.hasNext()).isFalse();
        assertThat(result.getContent())
                .extracting(Study::getTitle)
                .containsExactly("독서 모임", "JPA 스터디");
    }

    @Test
    void 리더와_함께_스터디_조회() {
        // given, when
        Optional<Study> foundStudy = studyRepository.findByIdWithLeader(study1.getId());

        // then
        assertThat(foundStudy).isPresent();
        assertThat(foundStudy.get().getTitle()).isEqualTo("스프링 완전 정복");
        assertThat(foundStudy.get().getLeader()).isNotNull();
        assertThat(foundStudy.get().getLeader().getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void 존재하지_않는_스터디_ID로_조회시_결과_없음() {
        // given
        Long nonExistentId = 999L;

        // when
        Optional<Study> foundStudy = studyRepository.findByIdWithLeader(nonExistentId);

        // then
        assertThat(foundStudy).isEmpty();
    }
}