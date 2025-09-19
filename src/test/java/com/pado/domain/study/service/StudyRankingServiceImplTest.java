package com.pado.domain.study.service;

import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.TotalRankingResponseDto;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudyRankingServiceImplTest {

    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Mock
    private StudyRepository studyRepository;

    @InjectMocks
    private StudyRankingServiceImpl rankingService;

    private User user1, user2, user3;
    private Study study;

    @BeforeEach
    void setUp() {
        study = Study.builder().title("스터디1").build();
        ReflectionTestUtils.setField(study, "id", 1L);

        user1 = User.builder().nickname("김민준").build();
        user2 = User.builder().nickname("이수빈").build();
        user3 = User.builder().nickname("파도타기").build();

        ReflectionTestUtils.setField(user1, "id", 1L);
        ReflectionTestUtils.setField(user2, "id", 2L);
        ReflectionTestUtils.setField(user3, "id", 3L);
    }

    @Test
    void 내_랭킹_조회_성공() {
        // given
        StudyMember m1 = StudyMember.builder().study(study).user(user1).rankPoint(1000).build();
        StudyMember m2 = StudyMember.builder().study(study).user(user2).rankPoint(800).build();

        when(studyRepository.existsById(1L)).thenReturn(true);

        when(studyMemberRepository.findAllByStudyIdOrderByRankPointDesc(1L))
                .thenReturn(Arrays.asList(m1, m2));

        // when
        MyRankResponseDto myRank = rankingService.getMyRank(1L, 2L);

        // then
        assertThat(myRank.my_rank()).isEqualTo(2);
        assertThat(myRank.my_score()).isEqualTo(800);
    }

    @Test
    void 존재하지_않는_스터디ID로_조회시_예외_발생() {
        // given
        when(studyRepository.existsById(99L)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> rankingService.getMyRank(99L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
    }

    @Test
    void 유저가_스터디_멤버가_아니면_예외_발생() {
        // given
        StudyMember m1 = StudyMember.builder().study(study).user(user1).rankPoint(1000).build();

        when(studyRepository.existsById(1L)).thenReturn(true);
        when(studyMemberRepository.findAllByStudyIdOrderByRankPointDesc(1L))
                .thenReturn(List.of(m1));

        // when & then
        assertThatThrownBy(() -> rankingService.getMyRank(1L, 99L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("사용자가 해당 스터디의 멤버가 아닙니다.");
    }

    @Test
    void 스터디_전체_랭킹_조회_성공() {
        // given
        User user4 = User.builder().nickname("갈매기").build();
        StudyMember m1 = StudyMember.builder().study(study).user(user1).rankPoint(1800).build();
        StudyMember m2 = StudyMember.builder().study(study).user(user2).rankPoint(1500).build();
        StudyMember m3 = StudyMember.builder().study(study).user(user3).rankPoint(1500).build();
        StudyMember m4 = StudyMember.builder().study(study).user(user4).rankPoint(1300).build();

        when(studyRepository.existsById(1L)).thenReturn(true);
        when(studyMemberRepository.findAllByStudyIdOrderByRankPointDesc(1L))
                .thenReturn(Arrays.asList(m1, m2, m3, m4));

        // when
        TotalRankingResponseDto result = rankingService.getTotalRanking(1L);

        // then
        assertThat(result.ranking()).hasSize(4);
        assertThat(result.ranking().get(0).rank()).isEqualTo(1);
        assertThat(result.ranking().get(0).userName()).isEqualTo("김민준");
        assertThat(result.ranking().get(1).rank()).isEqualTo(2);
        assertThat(result.ranking().get(1).userName()).isEqualTo("이수빈");
        assertThat(result.ranking().get(2).rank()).isEqualTo(2);
        assertThat(result.ranking().get(2).userName()).isEqualTo("파도타기");
        assertThat(result.ranking().get(3).rank()).isEqualTo(4);
        assertThat(result.ranking().get(3).userName()).isEqualTo("갈매기");
    }

}