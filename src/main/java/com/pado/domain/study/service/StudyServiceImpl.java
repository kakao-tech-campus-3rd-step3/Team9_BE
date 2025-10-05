package com.pado.domain.study.service;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
import com.pado.domain.study.dto.response.MyStudyResponseDto;
import com.pado.domain.study.dto.response.StudyDetailResponseDto;
import com.pado.domain.study.dto.response.StudyListResponseDto;
import com.pado.domain.study.dto.response.StudySimpleResponseDto;
import com.pado.domain.study.entity.*;
import com.pado.domain.study.exception.StudyNotFoundException;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;

    private static final int MAX_PAGE_SIZE = 50;

    @Override
    @Transactional
    public void createStudy(User user, StudyCreateRequestDto requestDto) {
        Study newStudy = Study.builder()
            .leader(user)
            .title(requestDto.title())
            .description(requestDto.description())
            .detailDescription(requestDto.detail_description())
            .studyTime(requestDto.study_time())
            .region(requestDto.region())
            .maxMembers(requestDto.max_members())
            .fileKey(requestDto.file_key())
            .build();
        newStudy.addInterests(requestDto.interests());
        newStudy.addConditions(requestDto.conditions());

        StudyMember leaderMember = StudyMember.builder()
            .study(newStudy)
            .user(user)
            .role(StudyMemberRole.LEADER)
            .build();
        studyRepository.save(newStudy);
        studyMemberRepository.save(leaderMember);
    }

    public List<MyStudyResponseDto> findMyStudies(Long userId) {
        List<Study> studies = studyRepository.findByUserId(userId);
        return studies.stream()
            .map(study -> new MyStudyResponseDto(
                study.getId(),
                study.getTitle()
            ))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public StudyListResponseDto findStudies(User user, String keyword, List<Category> categories,
        List<Region> regions, int page, int size) {
        Pageable pageable = createPageable(page, size);
        Slice<Study> studySlice = studyRepository.findStudiesByFilter(user, keyword, categories,
            regions, pageable);

        List<StudySimpleResponseDto> studyDtos = studySlice.getContent().stream()
            .map(StudySimpleResponseDto::from)
            .collect(Collectors.toList());
        return new StudyListResponseDto(
            studyDtos,
            pageable.getPageNumber(),
            pageable.getPageSize(),
            studySlice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public StudyDetailResponseDto getStudyDetail(Long studyId) {
        Study study = studyRepository.findByIdWithLeader(studyId)
            .orElseThrow(StudyNotFoundException::new);
        int currentMembers = (int) studyMemberRepository.countByStudy(study);

        List<Category> categories = study.getInterests().stream()
            .map(StudyCategory::getCategory)
            .collect(Collectors.toList());
        List<String> conditions = study.getConditions().stream()
            .map(StudyCondition::getContent)
            .toList();
        return new StudyDetailResponseDto(
            study.getFileKey(),
            study.getTitle(),
            study.getDescription(),
            study.getDetailDescription(),
            categories,
            study.getRegion(),
            study.getStudyTime(),
            conditions,
            currentMembers,
            study.getMaxMembers()
        );
    }

    @Override
    @Transactional
    public void leaveStudy(User user, Long studyId) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(StudyNotFoundException::new);

        StudyMember studyMember = studyMemberRepository.findByStudyAndUser(study, user)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (studyMember.getRole() == StudyMemberRole.LEADER) {
            throw new BusinessException(ErrorCode.CANNOT_LEAVE_AS_LEADER);
        }

        studyMemberRepository.delete(studyMember);
    }

    private Pageable createPageable(int page, int size) {
        int validatedSize = Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(page, validatedSize);
    }
}