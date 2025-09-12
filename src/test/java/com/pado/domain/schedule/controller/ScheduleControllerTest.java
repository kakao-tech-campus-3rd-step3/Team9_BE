package com.pado.domain.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.service.ScheduleService;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.auth.userdetails.CustomUserDetailsService;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.global.exception.common.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration; // import 추가
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// --- 해결을 위한 핵심 코드 ---
// UserDetailsServiceAutoConfiguration을 제외하여 불필요한 UserDetailsService Bean 생성을 막습니다.
@WebMvcTest(controllers = ScheduleController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class)
@Import(GlobalExceptionHandler.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // SecurityConfig가 필요로 하는 의존성은 Mock으로 그대로 주입합니다.
    @MockitoBean
    private JwtProvider jwtProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @Nested
    @DisplayName("일정 생성 API 테스트")
    class CreateSchedule {

        @Test
        @WithMockUser
        @DisplayName("성공 - 201 Created")
        void createSchedule_Success() throws Exception {
            // given
            long studyId = 1L;
            ScheduleCreateRequestDto requestDto = new ScheduleCreateRequestDto("새 일정", "내용",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
            willDoNothing().given(scheduleService)
                .createSchedule(anyLong(), any(ScheduleCreateRequestDto.class));

            // when & then
            mockMvc.perform(post("/api/studies/{study_id}/schedules", studyId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser
        @DisplayName("실패 (유효성 검사 - 제목 누락) - 400 Bad Request")
        void createSchedule_Fail_Validation() throws Exception {
            // given
            long studyId = 1L;
            ScheduleCreateRequestDto invalidRequestDto = new ScheduleCreateRequestDto("", "내용",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

            // when & then
            mockMvc.perform(post("/api/studies/{study_id}/schedules", studyId)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
    }

    @Nested
    @DisplayName("일정 조회 API 테스트")
    class GetSchedule {

        @Test
        @WithMockUser
        @DisplayName("개별 조회 성공 - 200 OK")
        void getScheduleDetail_Success() throws Exception {
            // given
            long scheduleId = 1L;
            ScheduleDetailResponseDto responseDto = new ScheduleDetailResponseDto(scheduleId,
                "테스트 일정", "상세 내용", LocalDateTime.now(), LocalDateTime.now().plusHours(2));
            given(scheduleService.findScheduleDetailById(scheduleId)).willReturn(responseDto);

            // when & then
            mockMvc.perform(get("/api/schedules/{schedule_id}", scheduleId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.schedule_id").value(scheduleId))
                .andExpect(jsonPath("$.title").value("테스트 일정"));
        }

        @Test
        @WithMockUser
        @DisplayName("전체 조회 성공 (결과 없음) - 200 OK")
        void findAllSchedules_Success_Empty() throws Exception {
            // given
            long studyId = 1L;
            given(scheduleService.findAllSchedulesByStudyId(studyId)).willReturn(
                Collections.emptyList());

            // when & then
            mockMvc.perform(get("/api/studies/{study_id}/schedules", studyId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        @WithMockUser
        @DisplayName("개별 조회 실패 (존재하지 않는 일정) - 404 Not Found")
        void getScheduleDetail_Fail_NotFound() throws Exception {
            // given
            long nonExistentScheduleId = 999L;
            given(scheduleService.findScheduleDetailById(nonExistentScheduleId)).willThrow(
                new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND));

            // when & then
            mockMvc.perform(get("/api/schedules/{schedule_id}", nonExistentScheduleId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("일정 수정 API 테스트")
    class UpdateSchedule {

        @Test
        @WithMockUser
        @DisplayName("수정 성공 - 200 OK")
        void updateSchedule_Success() throws Exception {
            // given
            long studyId = 1L;
            long scheduleId = 10L;
            ScheduleCreateRequestDto requestDto = new ScheduleCreateRequestDto("수정된 일정", "수정된 내용",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));
            willDoNothing().given(scheduleService)
                .updateSchedule(anyLong(), anyLong(), any(ScheduleCreateRequestDto.class));

            // when & then
            mockMvc.perform(
                    put("/api/studies/{study_id}/schedules/{schedule_id}", studyId, scheduleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("수정 실패 (유효성 검사 - 내용 누락) - 400 Bad Request")
        void updateSchedule_Fail_Validation() throws Exception {
            // given
            long studyId = 1L;
            long scheduleId = 10L;
            ScheduleCreateRequestDto invalidRequestDto = new ScheduleCreateRequestDto("수정된 일정", "",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2));

            // when & then
            mockMvc.perform(
                    put("/api/studies/{study_id}/schedules/{schedule_id}", studyId, scheduleId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequestDto)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
    }

    @Nested
    @DisplayName("일정 삭제 API 테스트")
    class DeleteSchedule {

        @Test
        @WithMockUser
        @DisplayName("삭제 성공 - 204 No Content")
        void deleteSchedule_Success() throws Exception {
            // given
            long studyId = 1L;
            long scheduleId = 10L;
            willDoNothing().given(scheduleService).deleteSchedule(studyId, scheduleId);

            // when & then
            mockMvc.perform(
                    delete("/api/studies/{study_id}/schedules/{schedule_id}", studyId, scheduleId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser
        @DisplayName("삭제 실패 (권한 없음) - 403 Forbidden")
        void deleteSchedule_Fail_Forbidden() throws Exception {
            // given
            long studyId = 1L;
            long scheduleId = 10L;
            willThrow(new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY)).given(
                scheduleService).deleteSchedule(studyId, scheduleId);

            // when & then
            mockMvc.perform(
                    delete("/api/studies/{study_id}/schedules/{schedule_id}", studyId, scheduleId)
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_STUDY_LEADER_ONLY"));
        }
    }
}
