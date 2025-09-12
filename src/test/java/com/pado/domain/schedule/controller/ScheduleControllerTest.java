package com.pado.domain.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleDetailResponseDto;
import com.pado.domain.schedule.service.ScheduleService;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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

@WebMvcTest(ScheduleController.class)
class ScheduleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ScheduleService scheduleService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @WithMockUser
    @DisplayName("일정 상세 조회 API - 성공")
    void getScheduleDetail_Success() throws Exception {
        // given
        long scheduleId = 1L;
        ScheduleDetailResponseDto responseDto = new ScheduleDetailResponseDto(scheduleId, "테스트 일정",
            "상세 내용", LocalDateTime.now(), LocalDateTime.now().plusHours(2));
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
    @DisplayName("스터디 전체 일정 조회 API (결과 없음) - 성공")
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
    @DisplayName("일정 생성 API 유효성 검사 실패 (제목 누락) - 400 Bad Request")
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
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("일정 삭제 API 권한 없음 - 403 Forbidden")
    void deleteSchedule_Fail_Forbidden() throws Exception {
        // given
        long studyId = 1L;
        long scheduleId = 10L;
        willThrow(new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY))
            .given(scheduleService).deleteSchedule(studyId, scheduleId);

        // when & then
        mockMvc.perform(
                delete("/api/studies/{study_id}/schedules/{schedule_id}", studyId, scheduleId)
                    .with(csrf()))
            .andDo(print())
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code").value("FORBIDDEN_STUDY_LEADER_ONLY"));
    }
}
