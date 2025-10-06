package com.pado.domain.study.service;

import com.pado.domain.chat.entity.ChatMessage;
import com.pado.domain.chat.entity.LastReadMessage;
import com.pado.domain.chat.repository.ChatMessageRepository;
import com.pado.domain.chat.repository.LastReadMessageRepository;
import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.dto.request.StudyCreateRequestDto;
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
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final LastReadMessageRepository lastReadMessageRepository;

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

        Study savedStudy = studyRepository.save(newStudy);
        StudyMember savedLeader = studyMemberRepository.save(leaderMember);

        // 멤버 새로 생성과 함께 해당 유저가 가장 마지막에 읽은 아이디 엔티티를 만들어 채팅방 기능이 정상적으로 작동하도록 구현
        LastReadMessage lastReadMessage = new LastReadMessage(savedLeader, 0L);
        lastReadMessageRepository.save(lastReadMessage);
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

    @Override
    @Transactional
    public void updateStudy(User user, Long studyId, StudyCreateRequestDto requestDto) {
        Study study = studyRepository.findById(studyId)
            .orElseThrow(StudyNotFoundException::new);

        if (!study.getLeader().getId().equals(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_STUDY_LEADER_ONLY);
        }

        study.update(
            requestDto.title(),
            requestDto.description(),
            requestDto.detail_description(),
            requestDto.region(),
            requestDto.study_time(),
            requestDto.max_members(),
            requestDto.file_key(),
            requestDto.interests(),
            requestDto.conditions()
        );
    }

    private Pageable createPageable(int page, int size) {
        int validatedSize = Math.min(size, MAX_PAGE_SIZE);
        return PageRequest.of(page, validatedSize);
    }
}