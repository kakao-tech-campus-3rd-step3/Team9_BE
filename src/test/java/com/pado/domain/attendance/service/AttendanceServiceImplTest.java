package com.pado.domain.attendance.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.attendance.entity.Attendance;
import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.Gender;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private StudyRepository studyRepository;
    @Mock
    private StudyMemberRepository studyMemberRepository;

    @Test
    @DisplayName("출석 성공")
    void 출석_성공_여부() {
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed")
                .nickname("tester")
                .region(Region.SEOUL)
                .gender(Gender.MALE)
                .build();

        ReflectionTestUtils.setField(user, "id", 10L);

        Schedule schedule = Schedule.builder()
                .studyId(100L)
                .title("스터디 일정")
                .description("스터디 설명")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        Study study = Study.builder().id(100L).build();

        given(scheduleRepository.findById(schedule.getId())).willReturn(Optional.of(schedule));
        given(studyRepository.findById(100L)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(false);

        // when
        AttendanceStatusResponseDto response = attendanceService.checkIn(schedule.getId(), user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isTrue();
        verify(attendanceRepository, times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("스케줄이 없으면 예외 발생")
    void 스케줄_없을_때_예외() {
        Long scheduleId = 1L;
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed")
                .nickname("tester")
                .region(Region.SEOUL)
                .gender(Gender.MALE)
                .build();

        ReflectionTestUtils.setField(user, "id", 10L);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.SCHEDULE_NOT_FOUND.message);
    }

    @Test
    @DisplayName("스터디가 없으면 예외 발생")
    void 스터디_없을_때_예외() {
        // given
        Long scheduleId = 1L;
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed")
                .nickname("tester")
                .region(Region.SEOUL)
                .gender(Gender.MALE)
                .build();

        ReflectionTestUtils.setField(user, "id", 10L);

        Schedule schedule = Schedule.builder()
                .studyId(100L)
                .title("스터디 일정")
                .description("스터디 설명")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusHours(2))
                .build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(100L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 예외 발생")
    void 스터디_맴버_아닐_때_예외() {
        // given
        Long scheduleId = 1L;
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed")
                .nickname("tester")
                .region(Region.SEOUL)
                .gender(Gender.MALE)
                .build();

        ReflectionTestUtils.setField(user, "id", 10L);

        Schedule schedule = Schedule.builder().studyId(100L).build();
        Study study = Study.builder().id(100L).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(100L)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY.message);
    }

    @Test
    @DisplayName("이미 출석한 경우 예외 발생")
    void 이미_출석했을_때_예외() {
        // given
        Long scheduleId = 1L;
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hashed")
                .nickname("tester")
                .region(Region.SEOUL)
                .gender(Gender.MALE)
                .build();

        ReflectionTestUtils.setField(user, "id", 10L);

        Schedule schedule = Schedule.builder().studyId(100L).build();
        Study study = Study.builder().id(100L).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(100L)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.ALREADY_CHECKED_IN.message);
    }
}
