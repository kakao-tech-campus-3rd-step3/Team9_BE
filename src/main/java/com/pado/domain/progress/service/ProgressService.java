package com.pado.domain.progress.service;

import com.pado.domain.attendance.dto.AttendanceListResponseDto;
import com.pado.domain.progress.dto.ProgressChapterRequestDto;
import com.pado.domain.progress.dto.ProgressRoadMapResponseDto;
import com.pado.domain.progress.dto.ProgressStatusResponseDto;
import com.pado.domain.user.entity.User;
import jakarta.validation.Valid;

public interface ProgressService {
    ProgressRoadMapResponseDto getRoadMap(Long studyId, User user);

    void createChapter(Long studyId, @Valid ProgressChapterRequestDto request, User user);

    void updateChapter(Long chapterId, @Valid ProgressChapterRequestDto request, User user);

    void deleteChapter(Long chapterId, User user);

    void completeChapter(Long chapterId, User user);

    ProgressStatusResponseDto getStudyStatus(Long studyId, User user);

    ProgressStatusResponseDto getmyStudyStatus(Long studyId, User user);
}
