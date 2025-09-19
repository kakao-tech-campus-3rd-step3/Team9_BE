package com.pado.domain.dashboard.service;

import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.dashboard.dto.LatestNoticeDto;
import com.pado.domain.dashboard.dto.StudyDashboardResponseDto;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.material.repository.MaterialRepository;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Mock private StudyRepository studyRepository;
    @Mock private MaterialRepository materialRepository;
    @Mock private ScheduleRepository scheduleRepository;
    @Mock private StudyMemberRepository studyMemberRepository;
    @Mock private AttendanceRepository attendanceRepository;

    private MockedStatic<LocalDateTime> mockedTime;

    private Study study;
    private User author;
    private Material notice;
    private Schedule schedule;
    private final LocalDateTime fixedNow = LocalDateTime.of(2025, 9, 18, 1, 11, 8);

    @BeforeEach
    void setUp() {
        mockedTime = mockStatic(LocalDateTime.class);
        mockedTime.when(LocalDateTime::now).thenReturn(fixedNow);

        study = Study.builder().title("테스트 스터디").build();
        ReflectionTestUtils.setField(study, "id", 1L);

        author = User.builder().nickname("작성자").build();
        ReflectionTestUtils.setField(author, "id", 1L);

        notice = new Material("공지사항 제목", MaterialCategory.NOTICE, null, "내용", study, author);
        ReflectionTestUtils.setField(notice, "id", 101L);

        schedule = Schedule.builder()
                .studyId(1L)
                .title("다가오는 일정")
                .startTime(fixedNow.plusDays(2))
                .build();
        ReflectionTestUtils.setField(schedule, "id", 201L);
    }

    @AfterEach
    void tearDown() {
        mockedTime.close();
    }

    @Test
    void 스터디_대시보드_조회_성공() {
        // given
        LatestNoticeDto expectedNoticeDto = new LatestNoticeDto(101L, "공지사항 제목", "작성자", fixedNow.minusDays(1));
        when(studyRepository.findById(1L)).thenReturn(Optional.of(study));
        when(materialRepository.findRecentNoticeAsDto(1L, MaterialCategory.NOTICE)).thenReturn(Optional.of(expectedNoticeDto));
        when(scheduleRepository.findTopByStudyIdAndStartTimeAfterOrderByStartTimeAsc(1L, fixedNow)).thenReturn(Optional.of(schedule));
        when(studyMemberRepository.countByStudyId(1L)).thenReturn(5);
        when(attendanceRepository.countByScheduleId(201L)).thenReturn(3);

        // when
        StudyDashboardResponseDto response = dashboardService.getStudyDashboard(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.study_title()).isEqualTo("테스트 스터디");

        assertThat(response.latest_notice()).isNotNull();
        assertThat(response.latest_notice().title()).isEqualTo("공지사항 제목");

        assertThat(response.upcoming_schedule()).isNotNull();
        assertThat(response.upcoming_schedule().title()).isEqualTo("다가오는 일정");
        long expectedDday = ChronoUnit.DAYS.between(fixedNow.toLocalDate(), schedule.getStartTime().toLocalDate());
        assertThat(response.upcoming_schedule().d_day()).isEqualTo(expectedDday);
    }

    @Test
    void 스터디가_없을_때_예외_발생() {
        // given
        when(studyRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> dashboardService.getStudyDashboard(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.STUDY_NOT_FOUND);
    }
}