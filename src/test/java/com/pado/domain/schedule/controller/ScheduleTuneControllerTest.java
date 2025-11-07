package com.pado.domain.schedule.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pado.domain.schedule.dto.request.ScheduleCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneCreateRequestDto;
import com.pado.domain.schedule.dto.request.ScheduleTuneParticipantRequestDto;
import com.pado.domain.schedule.dto.response.ScheduleCompleteResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneDetailResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneParticipantResponseDto;
import com.pado.domain.schedule.dto.response.ScheduleTuneResponseDto;
import com.pado.domain.schedule.service.ScheduleTuneService;
import com.pado.global.auth.jwt.JwtProvider;
import com.pado.global.auth.userdetails.CustomUserDetailsService;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import com.pado.global.exception.common.GlobalExceptionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ScheduleTuneController.class,
    excludeAutoConfiguration = UserDetailsServiceAutoConfiguration.class
)
@Import(GlobalExceptionHandler.class)
class ScheduleTuneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtProvider jwtProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @MockitoBean
    private ScheduleTuneService scheduleTuneService;

    @Nested
    @DisplayName("조율 생성 API")
    class CreateTune {

        @Test
        @WithMockUser
        @DisplayName("성공 - 201 Created")
        void create_success() throws Exception {
            ScheduleTuneCreateRequestDto req = new ScheduleTuneCreateRequestDto(
                "정기회의", "안건",

                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 0),
                LocalTime.of(12, 0)
            );
            given(scheduleTuneService.createScheduleTune(anyLong(), any())).willReturn(100L);

            mockMvc.perform(post("/api/studies/{study_id}/schedule-tunes", 1L)

                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())

                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("조율 목록/상세 API")
    class ListAndDetail {

        @Test
        @WithMockUser
        @DisplayName("목록 성공 - 200 OK")
        void list_success() throws Exception {
            given(scheduleTuneService.findAllScheduleTunes(1L))
                .willReturn(List.of(
                    new ScheduleTuneResponseDto(
                        101L, // tune_id 추가
                        "정기회의",
                        LocalDateTime.now().plusDays(1).withHour(10).withMinute(0),
                        LocalDateTime.now().plusDays(1).withHour(12).withMinute(0)
                    )
                ));

            mockMvc.perform(get("/api/studies/{study_id}/schedule-tunes", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tune_id").value(101L))
                .andExpect(jsonPath("$[0].title").value("정기회의"));
        }

        @Test
        @WithMockUser
        @DisplayName("상세 성공 - 200 OK")
        void detail_success() throws Exception {
            ScheduleTuneDetailResponseDto dto = new ScheduleTuneDetailResponseDto(

                "정기회의", "안건",
                List.of(3L, 5L, 0L),
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(12),
                List.of(new ScheduleTuneParticipantDto(1L, "Alice", 1L))

            );
            given(scheduleTuneService.findScheduleTuneDetail(100L)).willReturn(dto);

            mockMvc.perform(get("/api/schedule-tunes/{tune_id}", 100L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("정기회의"))
                .andExpect(jsonPath("$.candidate_dates[0]").value(3));
        }

        @Test

        @WithMockUser
        @DisplayName("상세 실패 - 404 Not Found")
        void detail_not_found() throws Exception {
            given(scheduleTuneService.findScheduleTuneDetail(999L))
                .willThrow(new BusinessException(ErrorCode.PENDING_SCHEDULE_NOT_FOUND));

            mockMvc.perform(get("/api/schedule-tunes/{tune_id}", 999L))
                .andDo(print())

                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PENDING_SCHEDULE_NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("조율 참여 API")
    class Participate {

        @Test
        @WithMockUser
        @DisplayName("참여 성공 - 200 OK")
        void participate_success() throws Exception {
            ScheduleTuneParticipantRequestDto req = new ScheduleTuneParticipantRequestDto(
                List.of(1L, 0L, 1L));
            given(scheduleTuneService.participate(anyLong(), any()))
                .willReturn(new ScheduleTuneParticipantResponseDto("updated"));

            mockMvc.perform(post("/api/schedule-tunes/{tune_id}/participants", 100L)
                    .with(csrf())

                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("updated"));
        }


        @Test
        @WithMockUser
        @DisplayName("참여 실패 - 403 Forbidden(스터디 멤버 아님)")
        void participate_forbidden() throws Exception {
            ScheduleTuneParticipantRequestDto req = new ScheduleTuneParticipantRequestDto(
                List.of(1L));
            given(scheduleTuneService.participate(anyLong(), any()))

                .willThrow(new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));

            mockMvc.perform(post("/api/schedule-tunes/{tune_id}/participants", 100L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())

                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN_STUDY_MEMBER_ONLY"));
        }
    }

    @Nested
    @DisplayName("조율 완료 API")
    class Complete {

        @Test
        @WithMockUser
        @DisplayName("완료 성공 - 200 OK")
        void complete_success() throws Exception {
            ScheduleCreateRequestDto req = new ScheduleCreateRequestDto(
                "확정 회의", "최종 안건",
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(11)

            );
            given(scheduleTuneService.complete(anyLong(), any()))
                .willReturn(new ScheduleCompleteResponseDto(true));

            mockMvc.perform(put("/api/schedule-tunes/{tune_id}/complete", 100L)
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)

                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        }

        @Test
        @WithMockUser
        @DisplayName("완료 실패 - 400 Bad Request(슬롯 불일치)")
        void complete_slot_mismatch() throws Exception {
            ScheduleCreateRequestDto req = new ScheduleCreateRequestDto(
                "확정 회의", "최종 안건",
                LocalDateTime.now().plusDays(1).withHour(10),
                LocalDateTime.now().plusDays(1).withHour(11)

            );
            given(scheduleTuneService.complete(anyLong(), any()))
                .willThrow(
                    new BusinessException(ErrorCode.INVALID_INPUT, "선택 시간이 생성된 슬롯과 일치하지 않습니다."));

            mockMvc.perform(put("/api/schedule-tunes/{tune_id}/complete", 100L)
                    .with(csrf())

                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
        }
    }
}