package com.pado.domain.attendance.service;

import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.attendance.dto.AttendanceStatusRequestDto;
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
import com.pado.domain.user.repository.UserRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

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
    @Mock
    private UserRepository userRepository;

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
        Schedule s =  Schedule.builder()
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
        assertThatThrownBy(() -> attendanceService.changeMyAttendanceStatus(scheduleId, new AttendanceStatusRequestDto(true),user))
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
        assertThatThrownBy(() -> attendanceService.changeMyAttendanceStatus(scheduleId, new AttendanceStatusRequestDto(true), user))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.STUDY_NOT_FOUND.message);
    }

    @Test
    @DisplayName("모든 스케줄 기준으로 멤버별 참가 여부를 확인")
    void 모든_참가_확인() {
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

    @Test
    @DisplayName("전체 출석 요약: 스케줄이 없으면 각 멤버 attendance 리스트는 빈 배열")
    void 전체출석_스케줄없음_빈리스트() {
        Long studyId = 100L;
        Study study = testStudy(studyId);
        User leader = testUser(1L, "leader");
        User m1 = testUser(2L, "m1");

        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.findByStudyWithUser(study))
                .willReturn(List.of(testStudyMember(study, leader, StudyMemberRole.LEADER),
                        testStudyMember(study, m1, StudyMemberRole.MEMBER)));
        given(scheduleRepository.findByStudyIdOrderByStartTimeAsc(studyId)).willReturn(List.of());
        given(attendanceRepository.findAllByStudyIdWithScheduleAndUser(studyId)).willReturn(List.of());
        given(studyMemberRepository.findLeaderUserIdByStudy(study, StudyMemberRole.LEADER))
                .willReturn(leader.getId());

        AttendanceListResponseDto resp = attendanceService.getFullAttendance(studyId);
        assertThat(resp.members()).hasSize(2);
        assertThat(resp.members().get(0).attendance()).isEmpty();
        assertThat(resp.members().get(1).attendance()).isEmpty();
    }


    @Test
    @DisplayName("참가한 경우 개별 참가 여부 확인")
    void 개별_참가_확인_출석() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);
        Attendance existing = Attendance.builder()
                .schedule(schedule).user(user).status(true).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(study.getId(), user.getId(), List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))).willReturn(true);
        given(attendanceRepository.findByScheduleAndUser(schedule, user)).willReturn(Optional.of(existing));

        AttendanceStatusResponseDto response = attendanceService.getMyAttendanceStatus(
                scheduleId, user);
        assertThat(response).isNotNull();
        assertThat(response.status()).isTrue();
        then(attendanceRepository).should(times(0)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("미참가한 경우 개별 참가 여부 확인")
    void 개별_참가_확인_미출석() {
        // given
        Long userId = 10L;
        Long scheduleId = 1L;
        Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);
        Attendance existing = Attendance.builder()
                .schedule(schedule).user(user).status(false).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(study.getId(), user.getId(), List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))).willReturn(true);
        given(attendanceRepository.findByScheduleAndUser(schedule, user)).willReturn(Optional.of(existing));

        AttendanceStatusResponseDto response = attendanceService.getMyAttendanceStatus(
                scheduleId, user);
        assertThat(response).isNotNull();
        assertThat(response.status()).isFalse();
        then(attendanceRepository).should(times(0)).save(any(Attendance.class));
    }

    @Test
    @DisplayName("미참가한 경우 개별 참가 여부 확인(엔티티 없을 경우)")
    void 개별_참가_확인_엔티티_없을_경우() {
        Long userId = 10L; Long scheduleId = 1L; Long studyId = 100L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, userId, List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))
        ).willReturn(true);
        given(attendanceRepository.findByScheduleAndUser(schedule, user)).willReturn(Optional.empty());

        AttendanceStatusResponseDto resp = attendanceService.getMyAttendanceStatus(scheduleId, user);
        assertThat(resp.status()).isFalse();
    }

    @Test
    @DisplayName("특정 스케줄 전체 참여 현황: 리더가 아니면 예외")
    void 특정스케줄_전체현황_리더아님_예외() {
        Long studyId = 100L; Long scheduleId = 1L;
        User actor = testUser(10L, "user");
        Study study = testStudy(studyId);
        Schedule schedule = testSchedule(scheduleId, studyId, LocalDateTime.now());

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, actor.getId(), List.of(StudyMemberRole.LEADER))
        ).willReturn(false); // 리더 아님

        assertThatThrownBy(() -> attendanceService.getScheduleAttendance(studyId, scheduleId, actor))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY.message);
    }

    @Test
    @DisplayName("리더가 멤버 출석 변경: 기존 레코드 있으면 상태만 변경, save() 없음")
    void 리더_멤버출석_업데이트() {
        Long scheduleId = 1L; Long studyId = 100L;
        User leader = testUser(1L, "leader");
        User target = testUser(2L, "target");
        Study study = testStudy(studyId);
        Schedule schedule = testSchedule(scheduleId, studyId, LocalDateTime.now());

        Attendance existing = testAttendance(99L, schedule, target, false);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, leader.getId(), List.of(StudyMemberRole.LEADER))
        ).willReturn(true);
        given(userRepository.findByNickname("target")).willReturn(Optional.of(target));
        given(attendanceRepository.findByScheduleAndUser(schedule, target)).willReturn(Optional.of(existing));

        attendanceService.changeMemberAttendanceStatus(scheduleId,
                new com.pado.domain.attendance.dto.AttendanceMemberStatusRequestDto("target", true),
                leader);

        assertThat(existing.isStatus()).isTrue();
        then(attendanceRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("리더가 멤버 출석 변경: 레코드 없으면 생성, save() 1회")
    void 리더_멤버출석_생성() {
        Long scheduleId = 1L; Long studyId = 100L;
        User leader = testUser(1L, "leader");
        User target = testUser(2L, "target");
        Study study = testStudy(studyId);
        Schedule schedule = testSchedule(scheduleId, studyId, LocalDateTime.now());

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, leader.getId(), List.of(StudyMemberRole.LEADER))
        ).willReturn(true);
        given(userRepository.findByNickname("target")).willReturn(Optional.of(target));
        given(attendanceRepository.findByScheduleAndUser(schedule, target)).willReturn(Optional.empty());

        attendanceService.changeMemberAttendanceStatus(scheduleId,
                new com.pado.domain.attendance.dto.AttendanceMemberStatusRequestDto("target", true),
                leader);

        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        then(attendanceRepository).should(times(1)).save(captor.capture());
        Attendance saved = captor.getValue();
        assertThat(saved.getUser()).isEqualTo(target);
        assertThat(saved.getSchedule()).isEqualTo(schedule);
        assertThat(saved.isStatus()).isTrue();
    }

    @Test
    @DisplayName("리더가 아닌 사용자가 멤버 출석 변경 시도 → 예외")
    void 리더아님_멤버출석_예외() {
        Long scheduleId = 1L; Long studyId = 100L;
        User actor = testUser(10L, "notLeader");
        Study study = testStudy(studyId);
        Schedule schedule = testSchedule(scheduleId, studyId, LocalDateTime.now());

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, actor.getId(), List.of(StudyMemberRole.LEADER))
        ).willReturn(false);

        assertThatThrownBy(() ->
                attendanceService.changeMemberAttendanceStatus(scheduleId,
                        new com.pado.domain.attendance.dto.AttendanceMemberStatusRequestDto("who", true),
                        actor)
        ).isInstanceOf(BusinessException.class)
                .hasMessageContaining(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY.message);
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
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(study.getId(), user.getId(), List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> attendanceService.changeMyAttendanceStatus(scheduleId, new AttendanceStatusRequestDto(true), user))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY.message);
    }


    @Test
    @DisplayName("참가 상태로 변경")
    void 참가_변경() {
        Long scheduleId = 1L; Long studyId = 100L; Long userId = 10L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, userId, List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))
        ).willReturn(true);
        // 핵심: findBy... 는 empty
        given(attendanceRepository.findByScheduleAndUser(schedule, user)).willReturn(Optional.empty());

        // when
        attendanceService.changeMyAttendanceStatus(scheduleId, new AttendanceStatusRequestDto(true), user);

        // then
        ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
        then(attendanceRepository).should(times(1)).save(captor.capture());
        Attendance saved = captor.getValue();
        assertThat(saved.isStatus()).isTrue();  // createCheckIn 내부에서 true 세팅되는지 검증
        assertThat(saved.getSchedule()).isEqualTo(schedule);
        assertThat(saved.getUser()).isEqualTo(user);

        // 존재 경로에선 save가 0이어야
        then(attendanceRepository).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("미참가 상태로 변경")
    void 미참가_변경() {
        Long scheduleId = 1L; Long studyId = 100L; Long userId = 10L;
        User user = testUser(userId);
        Schedule schedule = testSchedule(studyId);
        Study study = testStudy(studyId);

        Attendance existing = Attendance.builder()
                .schedule(schedule).user(user).status(true).build();

        given(scheduleRepository.findById(scheduleId)).willReturn(Optional.of(schedule));
        given(studyRepository.findById(studyId)).willReturn(Optional.of(study));
        given(studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(
                studyId, userId, List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER))
        ).willReturn(true);
        given(attendanceRepository.findByScheduleAndUser(schedule, user)).willReturn(Optional.of(existing));

        // when
        attendanceService.changeMyAttendanceStatus(scheduleId, new AttendanceStatusRequestDto(false), user);

        // then
        assertThat(existing.isStatus()).isFalse();  // changeStatus(false) 적용됐는지
        then(attendanceRepository).should(never()).save(any()); // 더티체킹만
    }



}
