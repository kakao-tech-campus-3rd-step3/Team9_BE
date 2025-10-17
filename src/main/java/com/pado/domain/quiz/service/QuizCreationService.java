package com.pado.domain.quiz.service;

import com.pado.domain.material.entity.File;
import com.pado.domain.material.repository.FileRepository;
import com.pado.domain.quiz.entity.Quiz;
import com.pado.domain.quiz.entity.QuizStatus;
import com.pado.domain.quiz.repository.QuizRepository;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.repository.StudyRepository;
import com.pado.domain.user.entity.User;
import com.pado.global.exception.common.BusinessException;
import com.pado.global.exception.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizCreationService {

    private final QuizRepository quizRepository;
    private final FileRepository fileRepository;
    private final StudyRepository studyRepository;

    @Transactional
    public Long createQuizRecord(User creator, String title, List<Long> fileIds, Long studyId) {
        // 파일 ID 검증
        if (fileIds == null || fileIds.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND, "퀴즈 생성을 위한 파일이 선택되지 않았습니다.");
        }

        // 스터디 존재 여부 검증
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STUDY_NOT_FOUND));

        // 파일 존재 여부 검증
        List<File> sourceFileList = fileRepository.findAllById(fileIds);
        if (sourceFileList.size() != fileIds.size()) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 퀴즈 객체 생성
        Quiz newQuiz = Quiz.builder()
                .study(study)
                .createdBy(creator)
                .title(title)
                .status(QuizStatus.GENERATING)
                .sourceFiles(new HashSet<>(sourceFileList))
                .build();

        Quiz savedQuiz = quizRepository.save(newQuiz);
        return savedQuiz.getId();
    }
}
