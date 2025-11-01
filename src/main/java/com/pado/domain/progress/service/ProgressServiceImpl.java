package com.pado.domain.progress.service;

import com.pado.domain.attendance.repository.AttendanceRepository;
import com.pado.domain.progress.dto.*;
import com.pado.domain.progress.entity.Chapter;
import com.pado.domain.progress.repository.ChapterRepository;
import com.pado.domain.quiz.entity.Quiz;
import com.pado.domain.quiz.repository.QuizSubmissionRepository;
import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {
    private final ChapterRepository chapterRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final AttendanceRepository attendanceRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ReflectionRepository reflectionRepository;

    @Override
    public ProgressRoadMapResponseDto getRoadMap(Long studyId, User user) {
        checkException(studyId, user, StudyMemberRole.MEMBER);
        List<Chapter> chapters = chapterRepository.findByStudyId(studyId);
        List<ProgressChapterDto> progressChapterDtos = chapters.stream().map(
                chapter -> new ProgressChapterDto(chapter.getContent(), chapter.isCompleted()))
                .toList();

        return new ProgressRoadMapResponseDto(progressChapterDtos);
    }

    @Override
    @Transactional
    public void createChapter(Long studyId, ProgressChapterRequestDto request, User user) {
        checkException(studyId, user, StudyMemberRole.LEADER);
        Study study = studyRepository.findById(studyId).orElse(null);
        chapterRepository.save(Chapter.createChapter(study, request.content(), false));
    }

    @Override
    @Transactional
    public void updateChapter(Long chapterId, ProgressChapterRequestDto request, User user) {
        Chapter chapter = chapterRepository.findById(chapterId).orElseThrow(
                ()-> new BusinessException(ErrorCode.CHAPTER_NOT_FOUND)
        );
        checkException(chapter.getStudy().getId(), user, StudyMemberRole.LEADER);

        int updated = chapterRepository.updateContent(chapterId, request.content());
        if (updated == 0) throw new BusinessException(ErrorCode.CHAPTER_NOT_FOUND);
    }

    @Override
    @Transactional
    public void deleteChapter(Long chapterId, User user) {
        Chapter chapter = chapterRepository.findById(chapterId).orElseThrow(
                ()-> new BusinessException(ErrorCode.CHAPTER_NOT_FOUND)
        );
        checkException(chapter.getStudy().getId(), user, StudyMemberRole.LEADER);
        chapterRepository.deleteById(chapterId);
    }

    @Override
    @Transactional
    public void completeChapter(Long chapterId, User user) {
        Chapter chapter = chapterRepository.findById(chapterId).orElseThrow(
                ()-> new BusinessException(ErrorCode.CHAPTER_NOT_FOUND)
        );
        checkException(chapter.getStudy().getId(), user, StudyMemberRole.LEADER);
        int updated = chapterRepository.complete(chapterId);
        if (updated == 0) throw new BusinessException(ErrorCode.CHAPTER_NOT_FOUND);
    }

    @Override
    public ProgressStatusResponseDto getStudyStatus(Long studyId, User user) {
        checkException(studyId, user, StudyMemberRole.MEMBER);

        List<StudyMember> studyMembers = studyMemberRepository.findByStudyIdFetchUser(studyId);
        Map<Long, Long> attendanceCountMap = attendanceRepository.countMapByStudy(studyId);
        Map<Long, Long> quizCountMap = quizSubmissionRepository.countMapByStudy(studyId);
        Map<Long, Long> reflectionCountMap = reflectionRepository.countMapByStudy(studyId);

        List<ProgressMemberStatusDto> progressMemberStatusDtos = studyMembers.stream().map(
                studyMember -> new ProgressMemberStatusDto(
                        studyMember.getUser().getNickname(),
                        studyMember.getRole(),
                        Math.toIntExact(attendanceCountMap.getOrDefault(studyMember.getUser().getId(), 0L)),
                        Math.toIntExact(quizCountMap.getOrDefault(studyMember.getUser().getId(), 0L)),
                        Math.toIntExact(reflectionCountMap.getOrDefault(studyMember.getUser().getId(), 0L))
                        )
        ).toList();
        return new ProgressStatusResponseDto(progressMemberStatusDtos);
    }

    @Override
    public ProgressStatusResponseDto getmyStudyStatus(Long studyId, User user) {
        checkException(studyId, user, StudyMemberRole.MEMBER);
        Long userId = user.getId();

        List<StudyMember> studyMembers = studyMemberRepository.findByStudyIdFetchUser(studyId);
        Map<Long, Long> attendanceCountMap = attendanceRepository.countMapByStudy(studyId);
        Map<Long, Long> quizCountMap = quizSubmissionRepository.countMapByStudy(studyId);
        Map<Long, Long> reflectionCountMap = reflectionRepository.countMapByStudy(studyId);

        List<ProgressMemberStatusDto> progressMemberStatusDtos = studyMembers.stream()
                .filter(studyMember -> studyMember.getUser().getId().equals(userId))
                .map(
                        studyMember -> new ProgressMemberStatusDto(
                        studyMember.getUser().getNickname(),
                        studyMember.getRole(),
                        Math.toIntExact(attendanceCountMap.getOrDefault(studyMember.getUser().getId(), 0L)),
                        Math.toIntExact(quizCountMap.getOrDefault(studyMember.getUser().getId(), 0L)),
                        Math.toIntExact(reflectionCountMap.getOrDefault(studyMember.getUser().getId(), 0L))
                        )
                ).toList();

        return new ProgressStatusResponseDto(progressMemberStatusDtos);
    }

    // 권한 있는지, study가 존재하는 지 확인
    private void checkException(Long studyId, User user, StudyMemberRole required) {
        if(!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }
        Collection<StudyMemberRole> allowed =
                (required == StudyMemberRole.LEADER) ? List.of(StudyMemberRole.LEADER) : List.of(StudyMemberRole.LEADER, StudyMemberRole.MEMBER);

        boolean ok = studyMemberRepository.existsByStudyIdAndUserIdAndRoleIn(studyId, user.getId(), allowed);
        if (!ok) {
            throw new BusinessException(
                    (required == StudyMemberRole.LEADER) ? ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY
                            : ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY
            );
        }

    }

}
