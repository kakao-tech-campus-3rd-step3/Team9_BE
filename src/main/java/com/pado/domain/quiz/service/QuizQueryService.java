package com.pado.domain.quiz.service;

import com.pado.domain.quiz.dto.projection.DashboardQuizProjection;
import com.pado.domain.quiz.dto.projection.QuizInfoProjection;
import com.pado.domain.quiz.dto.projection.SubmissionStatusDto;
import com.pado.domain.quiz.dto.response.CursorResponseDto;
import com.pado.domain.quiz.dto.response.QuizDashboardDto;
import com.pado.domain.quiz.dto.response.QuizInfoDto;
import com.pado.domain.quiz.dto.response.QuizResultDto;
import com.pado.domain.quiz.entity.QuizSubmission;
import com.pado.domain.quiz.entity.SubmissionStatus;
import com.pado.domain.quiz.mapper.QuizDtoMapper;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.domain.quiz.repository.QuizSubmissionRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuizQueryService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final QuizRepository quizRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final QuizDtoMapper quizDtoMapper;

    private static final int MAX_PAGE_SIZE = 50;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MIN_DASHBOARD_SIZE = 1;
    private static final int MAX_DASHBOARD_SIZE = 20;

    @Transactional(readOnly = true)
    public CursorResponseDto<QuizInfoDto> findQuizzesByStudy(Long studyId, User user, Long cursor, int pageSize) {
        // 1. 스터디 존재 & 권한 확인
        validateMember(studyId, user.getId());

        // 2. 페이지 사이즈 조정
        int adjustedSize = clampPageSize(pageSize);

        // 3. 퀴즈 조회(hasNext 계산을 위해 +1개 조회)
        List<QuizInfoProjection> projections = quizRepository.findByStudyIdWithCursor(studyId, cursor, adjustedSize + 1);

        // 4. hasNext 계산 & 반환 개수만큼 자르기
        boolean hasNext = projections.size() > adjustedSize;
        List<QuizInfoProjection> contentProjections = hasNext
                ? projections.subList(0, adjustedSize)
                : projections;

        // 5. 퀴즈 ID 추출
        List<Long> quizIds = contentProjections.stream()
                .map(QuizInfoProjection::quizId)
                .toList();

        // 6. 퀴즈별 문항 개수 & 상태 조회
        Map<Long, Long> questionCountMap = quizRepository.findQuestionCountsByQuizIds(quizIds);
        Map<Long, SubmissionStatusDto> submissionInfoMap = fetchSubmissionInfo(quizIds, user.getId());

        // 7. dto로 매핑
        List<QuizInfoDto> dtos = mapToDtos(contentProjections, questionCountMap, submissionInfoMap);

        // 8. nextCursor 계산
        Long nextCursor = calculateNextCursor(dtos, hasNext);

        return new CursorResponseDto<>(dtos, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public QuizResultDto getSubmissionResult(Long submissionId, User user) {
        // 1. Submission 조회
        QuizSubmission submission = quizSubmissionRepository.findForGradingById(submissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SUBMISSION_NOT_FOUND));

        // 2. 권한 확인
        if (!submission.getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ACCESS);
        }

        // 3. 완료된 제출인지 확인
        if (submission.getStatus() != SubmissionStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.QUIZ_NOT_YET_COMPLETED);
        }

        return quizDtoMapper.mapToResultDto(submission);
    }

    @Transactional(readOnly = true)
    public List<QuizDashboardDto> findRecentQuizzesForDashboard(Long studyId, User user, int size) {

        // 1. 권한 검증
        validateMember(studyId, user.getId());

        // 2. 조회할 퀴즈 개수 조정
        int adjustedSize = clampDashboardSize(size);

        // 3. 퀴즈 목록 조회
        List<DashboardQuizProjection> projections = quizRepository.findRecentDashboardQuizzes(studyId, user.getId(), adjustedSize);

        // 4. DTO 변환
        return projections.stream()
                .map(proj -> toQuizDashboardDto(proj, user.getId()))
                .toList();
    }

    private void validateMember(Long studyId, Long userId) {
        if (!studyMemberRepository.existsByStudyIdAndUserId(studyId, userId)) {
            if (!studyRepository.existsById(studyId)) {
                throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
            }
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY);
        }
    }

    private int clampPageSize(int size) {
        if (size <= 0) return DEFAULT_PAGE_SIZE;
        return Math.min(size, MAX_PAGE_SIZE);
    }

    private int clampDashboardSize(int size) {
        return Math.max(MIN_DASHBOARD_SIZE, Math.min(size, MAX_DASHBOARD_SIZE));
    }

    private Map<Long, SubmissionStatusDto> fetchSubmissionInfo(List<Long> quizIds, Long userId) {
        if (quizIds.isEmpty()) return Map.of();

        return quizSubmissionRepository.findSubmissionStatuses(quizIds, userId)
                .stream()
                .collect(Collectors.toMap(
                        SubmissionStatusDto::quizId,
                        dto -> dto
                ));
    }

    private List<QuizInfoDto> mapToDtos(List<QuizInfoProjection> projections, Map<Long, Long> questionCountMap, Map<Long, SubmissionStatusDto> infoMap) {
        return projections.stream()
                .map(proj -> QuizInfoDto.of(
                        proj,
                        questionCountMap.getOrDefault(proj.quizId(), 0L).intValue(),
                        infoMap.get(proj.quizId())
                ))
                .toList();
    }

    private Long calculateNextCursor(List<QuizInfoDto> dtos, boolean hasNext) {
        if (!hasNext || dtos.isEmpty()) return null;
        return dtos.get(dtos.size() - 1).getCursorId();
    }

    private QuizDashboardDto toQuizDashboardDto(DashboardQuizProjection proj, Long userId) {
        String rawStatus = proj.submissionStatusString();
        SubmissionStatus status = SubmissionStatus.from(rawStatus);

        if (rawStatus != null && status == SubmissionStatus.NOT_TAKEN && !rawStatus.equals("NOT_TAKEN")) {
            log.warn("Invalid submission status in DB: '{}' (QuizID: {}, UserID: {}). Defaulting to NOT_TAKEN.",
                    rawStatus, proj.quizId(), userId);
        }

        return new QuizDashboardDto(
                proj.quizId(),
                proj.title(),
                status
        );
    }
}