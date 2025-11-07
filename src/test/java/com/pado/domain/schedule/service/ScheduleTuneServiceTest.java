package com.pado.domain.schedule.service;

import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.entity.ScheduleTune;
import com.pado.domain.schedule.entity.ScheduleTuneParticipant;
import com.pado.domain.schedule.entity.ScheduleTuneSlot;
import com.pado.domain.schedule.entity.ScheduleTuneStatus;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.schedule.repository.ScheduleTuneParticipantRepository;
import com.pado.domain.schedule.repository.ScheduleTuneRepository;
import com.pado.domain.schedule.repository.ScheduleTuneSlotRepository;
import com.pado.domain.schedule.util.BitMaskUtils;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ScheduleTuneServiceTest {

    @InjectMocks
    private ScheduleTuneServiceImpl service;

    @Mock
    private ScheduleTuneRepository tuneRepo;
    @Mock
    private ScheduleTuneParticipantRepository partRepo;
    @Mock
    private ScheduleTuneSlotRepository slotRepo;
    @Mock
    private ScheduleRepository scheduleRepo;

    @Mock
    private StudyRepository studyRepo;
    @Mock
    private StudyMemberRepository studyMemberRepo;
    @Mock
    private StudyMemberService studyMemberService;

    private User leader;
    private User member;
    private Study study;

    @BeforeEach
    void setUp() {
        leader = User.builder().email("leader@test.com").nickname("leader").build();
        member = User.builder().email("member@test.com").nickname("member").build();
        study = Study.builder().title("스터디").build();
        ReflectionTestUtils.setField(leader, "id", 1L);
        ReflectionTestUtils.setField(member, "id", 2L);
        ReflectionTestUtils.setField(study, "id", 10L);
    }

    private void setAuth(User u) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(
            new UsernamePasswordAuthenticationToken(new CustomUserDetails(u), null, null));
        SecurityContextHolder.setContext(ctx);
    }

    @Nested
    @DisplayName("createScheduleTune")
    class Create {

        @Test
        @DisplayName("리더가 조율 생성 시 참가자 비트 부여 및 슬롯 생성")
        @SuppressWarnings({"unchecked", "rawtypes"})
        void create_by_leader_success() {
            setAuth(leader);
            given(studyRepo.findById(10L)).willReturn(Optional.of(study));
            given(studyMemberService.isStudyLeader(leader, study)).willReturn(true);

            StudyMember m1 = StudyMember.builder().study(study).user(leader).build();
            StudyMember m2 = StudyMember.builder().study(study).user(member).build();
            ReflectionTestUtils.setField(m1, "id", 100L);
            ReflectionTestUtils.setField(m2, "id", 101L);
            given(studyMemberRepo.findByStudyId(10L)).willReturn(List.of(m1, m2));

            ScheduleTune saved = ScheduleTune.builder()
                .studyId(10L).title("정기회의").description("안건")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .availableStartTime(LocalTime.of(10, 0))
                .availableEndTime(LocalTime.of(12, 0))
                .slotMinutes(30).status(ScheduleTuneStatus.PENDING).build();
            ReflectionTestUtils.setField(saved, "id", 777L);
            given(tuneRepo.save(any(ScheduleTune.class))).willReturn(saved);
            ScheduleTuneCreateRequestDto req = new ScheduleTuneCreateRequestDto(
                "정기회의", "안건",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0)
            );

            Long tuneId = service.createScheduleTune(10L, req);
            assertThat(tuneId).isEqualTo(777L);

            ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
            verify(partRepo).saveAll(captor.capture());
            List<ScheduleTuneParticipant> savedParts = captor.getValue();
            assertThat(savedParts).hasSize(2);
            assertThat(savedParts.get(0).getCandidateNumber()).isEqualTo(1L);
            assertThat(savedParts.get(1).getCandidateNumber()).isEqualTo(2L);

            verify(slotRepo).saveAll(anyList());
        }

        @Test
        @DisplayName("리더가 아니면 생성 거부")
        void create_forbidden() {
            setAuth(member);
            given(studyRepo.findById(10L)).willReturn(Optional.of(study));
            given(studyMemberService.isStudyLeader(member, study)).willReturn(false);

            ScheduleTuneCreateRequestDto req = new ScheduleTuneCreateRequestDto(
                "정기회의", "안건",
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(12, 0)
            );

            BusinessException ex = assertThrows(BusinessException.class,
                () -> service.createScheduleTune(10L, req));
            assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }
    }

    @Nested
    @DisplayName("participate")
    class Participate {

        @Test
        @DisplayName("멤버가 후보 슬롯 제출 시 해당 비트만 OR/UNSET 반영")
        void participate_updates_bitmap() {
            setAuth(member);
            given(studyMemberService.isStudyMember(member, 10L)).willReturn(true);

            ScheduleTune tune = ScheduleTune.builder()
                .studyId(10L).title("정기회의").description("안건")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .availableStartTime(LocalTime.of(10, 0))
                .availableEndTime(LocalTime.of(11, 0))
                .slotMinutes(30).status(ScheduleTuneStatus.PENDING).build();
            ReflectionTestUtils.setField(tune, "id", 777L);
            given(tuneRepo.findById(777L)).willReturn(Optional.of(tune));

            StudyMember sm = StudyMember.builder().study(study).user(member).build();
            ReflectionTestUtils.setField(sm, "id", 100L);
            given(studyMemberRepo.findByStudyIdAndUserId(10L, 2L)).willReturn(Optional.of(sm));

            ScheduleTuneParticipant part = ScheduleTuneParticipant.builder()
                .scheduleTune(tune).studyMemberId(100L).candidateNumber(1L).build();
            given(partRepo.findByScheduleTuneIdAndStudyMemberId(777L, 100L))
                .willReturn(Optional.of(part));

            LocalDateTime base = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            ScheduleTuneSlot s1 = ScheduleTuneSlot.builder()
                .scheduleTune(tune).slotIndex(0)
                .startTime(base)
                .endTime(base.plusMinutes(30))
                .occupancyBits(new byte[1]).build();
            ScheduleTuneSlot s2 = ScheduleTuneSlot.builder()
                .scheduleTune(tune).slotIndex(1)
                .startTime(base.plusMinutes(30))
                .endTime(base.plusMinutes(60))
                .occupancyBits(new byte[1]).build();

            given(slotRepo.findByScheduleTuneIdOrderBySlotIndexAscForUpdate(777L))
                .willReturn(List.of(s1, s2));

            ScheduleTuneParticipantRequestDto req = new ScheduleTuneParticipantRequestDto(
                List.of(1L, 0L));

            ScheduleTuneParticipantResponseDto resp = service.participate(777L, req);
            assertThat(resp.message()).isEqualTo("updated");
            assertThat(BitMaskUtils.popcount(s1.getOccupancyBits())).isEqualTo(1);
            assertThat(BitMaskUtils.popcount(s2.getOccupancyBits())).isEqualTo(0);
            verify(partRepo).save(any(ScheduleTuneParticipant.class));
            verify(slotRepo).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("complete")
    class Complete {

        @Test
        @DisplayName("리더가 슬롯과 일치하는 시간으로 완료 → schedule 생성 + status=COMPLETED 전이")
        void complete_success() {
            setAuth(leader);

            ScheduleTune tune = ScheduleTune.builder()
                .studyId(10L).title("정기회의").description("안건")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .availableStartTime(LocalTime.of(10, 0))
                .availableEndTime(LocalTime.of(11, 0))
                .slotMinutes(30).status(ScheduleTuneStatus.PENDING).build();
            ReflectionTestUtils.setField(tune, "id", 777L);

            given(tuneRepo.findById(777L)).willReturn(Optional.of(tune));
            given(studyRepo.findById(10L)).willReturn(Optional.of(study));
            given(studyMemberService.isStudyLeader(leader, study)).willReturn(true);

            LocalDateTime st = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            LocalDateTime et = st.plusMinutes(30);
            ScheduleTuneSlot s1 = ScheduleTuneSlot.builder()
                .scheduleTune(tune).slotIndex(0).startTime(st).endTime(et)
                .occupancyBits(new byte[1]).build();

            // [수정됨] complete_slot_mismatch 테스트에서 이 로직을 삭제했으므로, 이 테스트에서는 유지합니다.
            given(slotRepo.findByScheduleTuneIdOrderBySlotIndexAsc(777L))
                .willReturn(List.of(s1));

            ScheduleCreateRequestDto req = new ScheduleCreateRequestDto(
                "확정 회의", "최종 안건", st, et
            );

            ScheduleCompleteResponseDto resp = service.complete(777L, req);
            assertThat(resp.success()).isTrue();
            assertThat(tune.getStatus()).isEqualTo(ScheduleTuneStatus.COMPLETED);
            verify(scheduleRepo).save(any(Schedule.class));
            verify(tuneRepo).save(tune);
        }

        @Test
        @DisplayName("[수정됨] 완료 실패 (400) -> 1시간 단위로 요청해도 성공 (200 OK)")
        void complete_flexible_time_success() {
            setAuth(leader);

            ScheduleTune tune = ScheduleTune.builder()
                .studyId(10L).title("정기회의").description("안건")
                .startDate(LocalDate.now().plusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .availableStartTime(LocalTime.of(10, 0))
                .availableEndTime(LocalTime.of(11, 0))
                .slotMinutes(30).status(ScheduleTuneStatus.PENDING).build();
            ReflectionTestUtils.setField(tune, "id", 777L);

            given(tuneRepo.findById(777L)).willReturn(Optional.of(tune));
            given(studyRepo.findById(10L)).willReturn(Optional.of(study));
            given(studyMemberService.isStudyLeader(leader, study)).willReturn(true);

            // [수정됨] 30분짜리 슬롯만 존재한다고 가정
            LocalDateTime st_slot = LocalDateTime.now().plusDays(1).withHour(10);
            LocalDateTime et_slot = st_slot.plusMinutes(30);
            ScheduleTuneSlot s1 = ScheduleTuneSlot.builder()
                .scheduleTune(tune).slotIndex(0).startTime(st_slot).endTime(et_slot)
                .occupancyBits(new byte[1]).build();

            given(slotRepo.findByScheduleTuneIdOrderBySlotIndexAsc(777L))
                .willReturn(List.of(s1));

            // [수정됨] 1시간 단위로 요청 (슬롯과 불일치)
            LocalDateTime st_req = LocalDateTime.now().plusDays(1).withHour(10);
            LocalDateTime et_req = st_req.plusHours(1); // 10:00 ~ 11:00

            ScheduleCreateRequestDto req = new ScheduleCreateRequestDto(
                "확정 회의 (1시간)", "최종 안건", st_req, et_req
            );

            // [수정됨] BusinessException 예외를 던지는 대신, 성공(true)을 반환해야 함
            ScheduleCompleteResponseDto resp = service.complete(777L, req);

            assertThat(resp.success()).isTrue();
            assertThat(tune.getStatus()).isEqualTo(ScheduleTuneStatus.COMPLETED);

            // [수정됨] scheduleRepo.save()가 호출되었는지 검증
            ArgumentCaptor<Schedule> scheduleCaptor = ArgumentCaptor.forClass(Schedule.class);
            verify(scheduleRepo).save(scheduleCaptor.capture());

            // 저장된 Schedule이 1시간 단위인지 확인
            assertThat(scheduleCaptor.getValue().getStartTime()).isEqualTo(st_req);
            assertThat(scheduleCaptor.getValue().getEndTime()).isEqualTo(et_req);
        }
    }
}