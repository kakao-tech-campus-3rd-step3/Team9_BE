package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleByDateResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleResponseDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.service.StudyMemberService;
import com.pado.domain.user.entity.User;
import com.pado.global.auth.userdetails.CustomUserDetails;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyMemberService studyMemberService;

    private User leader;
    private User member;
    private Study study;

    private void setAuthentication(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null,
            userDetails.getAuthorities()));
        SecurityContextHolder.setContext(context);
    }

    @BeforeEach
    void setUp() {
        leader = User.builder().email("leader@test.com").nickname("리더").build();
        member = User.builder().email("member@test.com").nickname("멤버").build();
        study = Study.builder().leader(leader).title("테스트 스터디").build();

        ReflectionTestUtils.setField(leader, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(study, "id", 1L);
    }

    @Nested
    @DisplayName("일정 생성 테스트")
    class CreateScheduleTests {

        @Test
        @DisplayName("스터디 리더는 일정을 성공적으로 생성한다.")
        void createSchedule_Success_ByLeader() {
            // given
            setAuthentication(leader);
            ScheduleCreateRequestDto request = new ScheduleCreateRequestDto("새 일정", "내용",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(studyMemberService.isStudyLeader(leader, study)).thenReturn(true);

            // when
            scheduleService.createSchedule(1L, request);

            // then
            verify(scheduleRepository, times(1)).save(any(Schedule.class));
        }

        @Test
        @DisplayName("일반 멤버는 일정 생성 시 FORBIDDEN 예외가 발생한다.")
        void createSchedule_Fail_ByMember() {
            // given
            setAuthentication(member);
            ScheduleCreateRequestDto request = new ScheduleCreateRequestDto("새 일정", "내용",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(studyMemberService.isStudyLeader(member, study)).thenReturn(false);

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> scheduleService.createSchedule(1L, request));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }
    }

    @Nested
    @DisplayName("월별 일정 조회")
    class FindSchedulesByMonthTests {

        @Test
        @DisplayName("성공: 2025년 9월 조회 시, 8/31 ~ 10/11 사이의 일정을 정확히 반환한다.")
        void findMySchedulesByMonth_Success() {
            // given
            Long userId = 1L;
            int year = 2025;
            int month = 9;

            LocalDate expectedStartDate = LocalDate.of(2025, 8, 31);
            LocalDate expectedEndDate = LocalDate.of(2025, 10, 11);
            LocalDateTime expectedPeriodStart = expectedStartDate.atStartOfDay();
            LocalDateTime expectedPeriodEnd = expectedEndDate.plusDays(1).atStartOfDay();

            Schedule scheduleInPeriod = Schedule.builder().studyId(1L).title("9월 스터디").startTime(LocalDateTime.of(2025, 9, 15, 10, 0)).endTime(LocalDateTime.of(2025, 9, 15, 12, 0)).build();
            ReflectionTestUtils.setField(scheduleInPeriod, "id", 101L);

            when(scheduleRepository.findAllByUserIdAndPeriod(eq(userId), eq(expectedPeriodStart), eq(expectedPeriodEnd)))
                    .thenReturn(List.of(scheduleInPeriod));

            // when
            List<ScheduleByDateResponseDto> result = scheduleService.findMySchedulesByMonth(userId, year, month);

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().title()).isEqualTo("9월 스터디");
            assertThat(result.getFirst().schedule_id()).isEqualTo(101L);
        }

        @Test
        @DisplayName("성공: 해당 기간에 일정이 없으면 빈 리스트를 반환한다.")
        void findMySchedulesByMonth_WhenNoSchedules_ShouldReturnEmptyList() {
            // given
            Long userId = 1L;
            int year = 2025;
            int month = 11;

            when(scheduleRepository.findAllByUserIdAndPeriod(eq(userId), any(), any())).thenReturn(Collections.emptyList());

            // when
            List<ScheduleByDateResponseDto> result = scheduleService.findMySchedulesByMonth(userId, year, month);

            // then
            assertThat(result).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("일정 수정 테스트")
    class UpdateScheduleTests {

        @Test
        @DisplayName("스터디 리더는 일정을 성공적으로 수정한다.")
        void updateSchedule_Success_ByLeader() {
            // given
            setAuthentication(leader);
            ScheduleCreateRequestDto request = new ScheduleCreateRequestDto("수정된 제목", "수정된 내용",
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
            Schedule existingSchedule = Schedule.builder().studyId(1L).title("원본 제목").build();

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(studyMemberService.isStudyLeader(leader, study)).thenReturn(true);
            when(scheduleRepository.findById(10L)).thenReturn(Optional.of(existingSchedule));

            // when
            scheduleService.updateSchedule(1L, 10L, request);

            // then
            assertThat(existingSchedule.getTitle()).isEqualTo("수정된 제목");
            assertThat(existingSchedule.getDescription()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("다른 스터디의 일정을 수정하려 하면 NOT_FOUND 예외가 발생한다.")
        void updateSchedule_Fail_WrongStudy() {
            // given
            setAuthentication(leader);
            ScheduleCreateRequestDto request = new ScheduleCreateRequestDto("수정된 제목", "수정된 내용",
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(2).plusHours(2));
            Schedule anotherStudySchedule = Schedule.builder().studyId(999L)
                .build();

            when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
            when(studyMemberService.isStudyLeader(leader, study)).thenReturn(true);
            when(scheduleRepository.findById(10L)).thenReturn(Optional.of(anotherStudySchedule));

            // when & then
            BusinessException exception = assertThrows(BusinessException.class,
                () -> scheduleService.updateSchedule(1L, 10L, request));
            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.SCHEDULE_NOT_FOUND);
        }
    }
}
