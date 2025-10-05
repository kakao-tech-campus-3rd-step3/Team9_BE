package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.request.ReflectionCreateRequestDto;
import com.pado.domain.reflection.dto.response.ReflectionListResponseDto;
import com.pado.domain.reflection.dto.response.ReflectionResponseDto;
import com.pado.domain.reflection.dto.response.ReflectionSimpleResponseDto;
import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReflectionServiceImpl implements ReflectionService {

    private final ReflectionRepository reflectionRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ScheduleRepository scheduleRepository;

    @Override
    @Transactional
    public ReflectionResponseDto createReflection(Long studyId, User user,
        ReflectionCreateRequestDto request) {
        StudyMember member = checkStudyMember(studyId, user);
        Study study = studyRepository.findById(studyId)
            .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));
        Schedule schedule = request.scheduleId() != null ?
            scheduleRepository.findById(request.scheduleId()).orElse(null) : null;
        Reflection reflection = Reflection.builder()
            .title(request.title())
            .study(study)
            .studyMember(member)
            .schedule(schedule)
            .satisfactionScore(request.satisfactionScore())
            .understandingScore(request.understandingScore())
            .participationScore(request.participationScore())
            .learnedContent(request.learnedContent())
            .improvement(request.improvement())
            .build();

        reflectionRepository.save(reflection);
        return toDto(reflection);
    }

    @Override
    @Transactional(readOnly = true)
    public ReflectionListResponseDto getReflections(Long studyId, User user, String author,
        Pageable pageable) {
        checkStudyMember(studyId, user);

        Long filterByUserId = null;
        if ("me".equalsIgnoreCase(author)) {
            filterByUserId = user.getId();
        }

        Page<Reflection> reflectionPage = reflectionRepository.findReflectionsByStudy(studyId,
            filterByUserId, pageable);

        List<ReflectionSimpleResponseDto> dtoList = reflectionPage.getContent().stream()
            .map(this::toSimpleDto)
            .collect(Collectors.toList());

        return new ReflectionListResponseDto(dtoList, reflectionPage.getNumber(),
            reflectionPage.getSize(), reflectionPage.hasNext());
    }

    @Override
    @Transactional(readOnly = true)
    public ReflectionResponseDto getReflection(Long studyId, Long reflectionId, User user) {
        checkStudyMember(studyId, user);
        Reflection reflection = reflectionRepository.findById(reflectionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.getStudy().getId().equals(studyId)) {
            throw new BusinessException(ErrorCode.REFLECTION_NOT_FOUND);
        }
        return toDto(reflection);
    }

    @Override
    @Transactional
    public ReflectionResponseDto updateReflection(Long studyId, Long reflectionId, User user,
        ReflectionCreateRequestDto request) {
        checkStudyMember(studyId, user);
        Reflection reflection = reflectionRepository.findById(reflectionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.getStudy().getId().equals(studyId)) {
            throw new BusinessException(ErrorCode.REFLECTION_NOT_FOUND);
        }
        checkReflectionOwner(reflection, user);

        Schedule schedule = request.scheduleId() != null ?
            scheduleRepository.findById(request.scheduleId()).orElse(null) : null;
        reflection.update(
            request.title(),
            schedule,
            request.satisfactionScore(),
            request.understandingScore(),
            request.participationScore(),
            request.learnedContent(),
            request.improvement()
        );
        return toDto(reflection);
    }

    @Override
    @Transactional
    public void deleteReflection(Long studyId, Long reflectionId, User user) {
        checkStudyMember(studyId, user);
        Reflection reflection = reflectionRepository.findById(reflectionId)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFLECTION_NOT_FOUND));

        if (!reflection.getStudy().getId().equals(studyId)) {
            throw new BusinessException(ErrorCode.REFLECTION_NOT_FOUND);
        }
        checkReflectionOwner(reflection, user);
        reflectionRepository.deleteById(reflectionId);
    }

    private StudyMember checkStudyMember(Long studyId, User user) {
        return studyMemberRepository.findByStudyIdAndUserId(studyId, user.getId())
            .orElseThrow(() -> new BusinessException(ErrorCode.FORBIDDEN_STUDY_MEMBER_ONLY));
    }

    private void checkReflectionOwner(Reflection reflection, User user) {
        if (!reflection.getStudyMember().getUser().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_REFLECTION_OWNER_ONLY);
        }
    }

    private ReflectionResponseDto toDto(Reflection r) {
        return new ReflectionResponseDto(
            r.getId(),
            r.getTitle(),
            r.getStudy().getId(),
            r.getStudyMember().getId(),
            r.getSchedule() != null ? r.getSchedule().getId() : null,
            r.getSatisfactionScore(),
            r.getUnderstandingScore(),
            r.getParticipationScore(),
            r.getLearnedContent(),
            r.getImprovement(),
            r.getCreatedAt(),
            r.getUpdatedAt()
        );
    }

    private ReflectionSimpleResponseDto toSimpleDto(Reflection r) {
        return ReflectionSimpleResponseDto.builder()
            .reflectionId(r.getId())
            .title(r.getTitle())
            .authorName(r.getStudyMember().getUser().getNickname())
            .scheduleTitle(r.getSchedule() != null ? r.getSchedule().getTitle() : null)
            .updatedAt(r.getUpdatedAt())
            .build();
    }
}