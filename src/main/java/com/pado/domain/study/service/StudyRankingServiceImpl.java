package com.pado.domain.study.service;

import com.pado.domain.quiz.repository.QuizPointLogRepository;
import com.pado.domain.study.dto.response.MyRankResponseDto;
import com.pado.domain.study.dto.response.RankerResponseDto;
import com.pado.domain.study.dto.response.TotalRankingResponseDto;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.repository.StudyMemberRepository;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyRankingServiceImpl implements StudyRankingService{

    private final StudyMemberRepository studyMemberRepository;
    private final StudyRepository studyRepository;
    private final QuizPointLogRepository quizPointLogRepository;

    @Override
    public MyRankResponseDto getMyRank(Long studyId, Long userId) {
        validateStudyExists(studyId);
        List<RankerResponseDto> totalRanking = getRankedStudyMembers(studyId);

        return totalRanking.stream()
                .filter(ranker -> ranker.userId().equals(userId))
                .findFirst()
                .map(ranker -> new MyRankResponseDto(ranker.rank(), ranker.score()))
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "사용자가 해당 스터디의 멤버가 아닙니다."));
    }

    @Override
    public TotalRankingResponseDto getTotalRanking(Long studyId) {
        validateStudyExists(studyId);

        List<RankerResponseDto> totalRanking = getRankedStudyMembers(studyId);
        return new TotalRankingResponseDto(totalRanking);
    }

    private List<RankerResponseDto> getRankedStudyMembers(Long studyId) {
        List<StudyMember> members = studyMemberRepository.findAllByStudyIdOrderByRankPointDesc(studyId);

        if (members.isEmpty()) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND, "해당 스터디에 멤버가 없습니다.");
        }

        List<RankerResponseDto> ranking = new ArrayList<>();
        int currentRank = 0;
        int lastScore = -1;

        for (int i = 0; i < members.size(); i++) {
            StudyMember member = members.get(i);
            int currentScore = member.getRankPoint();

            if (currentScore != lastScore) {
                currentRank = i + 1;
            }

            ranking.add(new RankerResponseDto(
                    currentRank,
                    currentScore,
                    member.getUser().getId(),
                    member.getUser().getNickname()
            ));

            lastScore = currentScore;
        }
        return ranking;
    }

    private void validateStudyExists(Long studyId) {
        if (!studyRepository.existsById(studyId)) {
            throw new BusinessException(ErrorCode.STUDY_NOT_FOUND);
        }
    }
}
