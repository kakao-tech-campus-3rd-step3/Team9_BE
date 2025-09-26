package com.pado.domain.reflection.service;

import com.pado.domain.reflection.dto.*;
import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.ReflectionRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.schedule.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
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
    public ReflectionResponseDto createReflection(Long studyId, Long studyMemberId,
        ReflectionCreateRequestDto request) {
        Study study = studyRepository.findById(studyId).orElseThrow();
        StudyMember member = studyMemberRepository.findById(studyMemberId).orElseThrow();
        Schedule schedule =
            request.scheduleId() != null ? scheduleRepository.findById(request.scheduleId())
                .orElse(null) : null;
        Reflection reflection = Reflection.builder()
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
    public List<ReflectionResponseDto> getReflections(Long studyId) {
        return reflectionRepository.findByStudyId(studyId).stream().map(this::toDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReflectionResponseDto getReflection(Long reflectionId) {
        Reflection r = reflectionRepository.findById(reflectionId).orElseThrow();
        return toDto(r);
    }

    @Override
    @Transactional
    public ReflectionResponseDto updateReflection(Long reflectionId,
        ReflectionCreateRequestDto request) {
        Reflection r = reflectionRepository.findById(reflectionId).orElseThrow();
        Schedule schedule =
            request.scheduleId() != null ? scheduleRepository.findById(request.scheduleId())
                .orElse(null) : null;
        r.update(
            schedule,
            request.satisfactionScore(),
            request.understandingScore(),
            request.participationScore(),
            request.learnedContent(),
            request.improvement()
        );
        return toDto(r);
    }
    
    @Override
    @Transactional
    public void deleteReflection(Long reflectionId) {
        reflectionRepository.deleteById(reflectionId);
    }

    private ReflectionResponseDto toDto(Reflection r) {
        return new ReflectionResponseDto(
            r.getId(),
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
}
