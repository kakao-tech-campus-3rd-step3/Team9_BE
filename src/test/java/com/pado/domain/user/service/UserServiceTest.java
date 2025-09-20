package com.pado.domain.user.service;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.dto.UserStudyResponseDto;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Nested
    @DisplayName("getUserStudy()")
    class GetUserStudy {

        @Test
        @DisplayName("정상: 스터디/멤버 조회 성공 시 DTO 반환")
        void success() {
            // given
            Long studyId = 1L;
            User user = User.builder()
                    .nickname("baejw")
                    .profileImageUrl("https://cdn.example.com/p.png")
                    .build();
            ReflectionTestUtils.setField(user, "id", 1L);

            Study study = Study.builder()
                    .id(studyId)
                    .title("알고리즘 스터디")
                    .build();

            StudyMember studyMember = StudyMember.builder()
                    .study(study)
                    .user(user)
                    .role(StudyMemberRole.LEADER)
                    .build();
            ReflectionTestUtils.setField(studyMember, "id", 100L);

            given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
            given(studyMemberRepository.findByStudyIdAndUserId(studyId, user.getId()))
                    .willReturn(Optional.of(studyMember));

            // when
            UserStudyResponseDto dto = userService.getUserStudy(studyId, user);

            // then
            assertThat(dto.nickname()).isEqualTo("baejw");
            assertThat(dto.image_url()).isEqualTo("https://cdn.example.com/p.png");
            assertThat(dto.title()).isEqualTo("알고리즘 스터디");
            assertThat(dto.role()).isEqualTo(StudyMemberRole.LEADER);
        }

        @Test
        @DisplayName("예외: 스터디가 없으면 STUDY_NOT_FOUND")
        void studyNotFound() {
            // given
            Long studyId = 1L;
            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", 10L);
            given(studyRepository.findById(studyId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserStudy(studyId, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
        }

        @Test
        @DisplayName("예외: 스터디 멤버가 없으면 USER_NOT_FOUND")
        void studyMemberNotFound() {
            // given
            Long studyId = 1L;
            User user = User.builder().build();
            ReflectionTestUtils.setField(user, "id", 10L);
            Study study = Study.builder().id(studyId).title("알고리즘 스터디").build();

            given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
            given(studyMemberRepository.findByStudyIdAndUserId(studyId, user.getId()))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.getUserStudy(studyId, user))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(ErrorCode.USER_NOT_FOUND.message);
        }
    }
}

