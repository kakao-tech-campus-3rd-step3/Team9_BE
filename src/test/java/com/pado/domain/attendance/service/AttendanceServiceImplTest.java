package com.pado.domain.attendance.service;

import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;

import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceStatusResponseDto;
import com.pado.domain.attendance.dto.MemberAttendanceDto;
import com.pado.domain.attendance.entity.Attendance;
import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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

    private User testUser(Long id) {
        User u = User.builder()
            .email("test@test.com")
            .passwordHash("hashed")
            .nickname("tester")
            .region(Region.SEOUL)
            .gender(Gender.MALE)
            .build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private User testUser(Long id, String name) {
        User u = User.builder()
            .email("test@test.com")
            .passwordHash("hashed")
            .nickname(name)
            .region(Region.SEOUL)
            .gender(Gender.MALE)
            .build();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private Schedule testSchedule(Long id, Long studyId, LocalDateTime startDate) {
        Schedule s = Schedule.builder()
            .studyId(studyId)
            .title("스터디 일정")
            .description("설명")
            .startTime(startDate)
            .endTime(startDate.plusDays(1))
            .build();
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }

    private Schedule testSchedule(Long studyId) {
        return Schedule.builder()
            .studyId(studyId)
            .title("스터디 일정")
            .description("설명")
            .startTime(LocalDateTime.now())
            .endTime(LocalDateTime.now().plusHours(2))
            .build();
    }

    private Study testStudy(Long id) {
        return Study.builder().id(id).build();
    }

    private Attendance testAttendance(Long id, Schedule schedule, User user, boolean status) {
        Attendance a = Attendance.builder()
            .schedule(schedule)
            .user(user)
            .status(status)
            .checkInTime(LocalDateTime.now())
            .build();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    private StudyMember testStudyMember(Study study, User user, StudyMemberRole role) {
        return StudyMember.builder()
            .study(study)
            .user(user)
            .role(role)
            .build();
    }

    @Test
    @DisplayName("스케줄이 없으면 예외 발생")
    void 스케줄_없을_때_예외() {
        Long userId = 10L;
        Long scheduleId = 1L;
        User user = testUser(userId);

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
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
    }

    @Test
    @DisplayName("스터디 멤버가 아니면 예외 발생")
    void 스터디_멤버_아닐_때_예외() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
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
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> attendanceService.checkIn(scheduleId, user))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.ALREADY_CHECKED_IN.message);
    }

    @Test
    @DisplayName("출석 성공")
    void 출석_성공_여부() {
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(false);

        // when
        AttendanceStatusResponseDto response = attendanceService.checkIn(scheduleId, user);

        // then
        assertThat(response).isNotNull();
        assertThat(response.status()).isTrue();
        then(attendanceRepository).should(times(1)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("출석한 경우 개별 출석 여부 확인")
    void 개별_출석_확인_출석() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(true);

        AttendanceStatusResponseDto response = attendanceService.getIndividualAttendanceStatus(
            scheduleId, user);
        assertThat(response).isNotNull();
        assertThat(response.status()).isTrue();
        then(attendanceRepository).should(times(0)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("미출석한 경우 개별 출석 여부 확인")
    void 개별_출석_확인_미출석() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyAndUser(study, user)).willReturn(true);
        given(attendanceRepository.existsByScheduleAndUser(schedule, user)).willReturn(false);

        AttendanceStatusResponseDto response = attendanceService.getIndividualAttendanceStatus(
            scheduleId, user);
        assertThat(response).isNotNull();
        assertThat(response.status()).isFalse();
        then(attendanceRepository).should(times(0)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("모든 스케줄 기준으로 멤버별 출석 여부를 확인")
    void 모든_출석_확인() {
        // given
        Long studyId = 100L;
        Study study = testStudy(studyId);

        User u1 = testUser(1L, "u1");
        User u2 = testUser(2L, "u2");

        StudyMember sm1 = testStudyMember(study, u1, StudyMemberRole.MEMBER);
        StudyMember sm2 = testStudyMember(study, u2, StudyMemberRole.LEADER);
        List<StudyMember> members = List.of(sm1, sm2);

        LocalDateTime base = LocalDateTime.now().withNano(0);
        Schedule sc1 = testSchedule(11L, studyId, base.plusDays(1));
        Schedule sc2 = testSchedule(12L, studyId, base.plusDays(2));
        Schedule sc3 = testSchedule(13L, studyId, base.plusDays(3));
        List<Schedule> schedules = List.of(sc1, sc2, sc3);

        Attendance a1 = testAttendance(1000L, sc1, u1, true);
        List<Attendance> attendanceList = List.of(a1);

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.findByStudyWithUser(study)).willReturn(members);
        given(scheduleRepository.findByStudyIdOrderByStartTimeAsc(studyId)).willReturn(schedules);
        given(attendanceRepository.findAllByStudyIdWithScheduleAndUser(studyId)).willReturn(
            attendanceList);
        given(studyMemberRepository.findLeaderUserIdByStudy(study,
            StudyMemberRole.LEADER)).willReturn(u2.getId());

        // when
        AttendanceListResponseDto resp = attendanceService.getFullAttendance(studyId);

        // then
        assertThat(resp).isNotNull();

        List<MemberAttendanceDto> resultMembers = resp.members();
        assertThat(resultMembers).hasSize(2);

        MemberAttendanceDto m1 = resultMembers.get(0);
        MemberAttendanceDto m2 = resultMembers.get(1);

        assertThat(m1.attendance()).hasSize(3);
        assertThat(m2.attendance()).hasSize(3);
        assertThat(m1.name()).isEqualTo("u2");
        assertThat(m1.attendance().stream().allMatch(s -> !s.status())).isTrue();
        assertThat(m2.name()).isEqualTo("u1");
        assertThat(m2.attendance().get(0).status()).isTrue();
        assertThat(m2.attendance().get(1).status()).isFalse();
        assertThat(m2.attendance().get(2).status()).isFalse();
        assertThat(m1.attendance().get(0).schedule_date()).isEqualTo(sc1.getStartTime());
        assertThat(m1.attendance().get(1).schedule_date()).isEqualTo(sc2.getStartTime());
        assertThat(m1.attendance().get(2).schedule_date()).isEqualTo(sc3.getStartTime());

        then(studyRepository).should(times(1)).findById(studyId);
        then(studyMemberRepository).should(times(1)).findByStudyWithUser(study);
        then(scheduleRepository).should(times(1)).findByStudyIdOrderByStartTimeAsc(studyId);
        then(attendanceRepository).should(times(1)).findAllByStudyIdWithScheduleAndUser(studyId);
    }
}
