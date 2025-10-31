package com.pado.domain.quiz.mapper;

import com.pado.domain.quiz.dto.response.*;
import com.pado.domain.quiz.entity.*;
import com.pado.domain.quiz.repository.AnswerSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class QuizDtoMapper {

    private final AnswerSubmissionRepository answerSubmissionRepository;
    private final Clock clock;

    public QuizResultDto mapToResultDto(QuizSubmission submission) {
        Map<Long, AnswerSubmission> userAnswersMap = submission.getAnswers().stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        answer -> answer
                ));

        List<AnswerResultDto> results = submission.getQuiz().getQuestions().stream()
                .map(question -> mapToAnswerResultDto(question, userAnswersMap.get(question.getId())))
                .toList();

        return new QuizResultDto(
                submission.getId(),
                submission.getScore(),
                submission.getQuiz().getQuestions().size(),
                results
        );
    }

    public QuizProgressDto toQuizProgressDto(Quiz quiz, QuizSubmission submission) {
        Map<Long, String> userAnswerMap = answerSubmissionRepository.findWithQuestionBySubmissionId(submission.getId()).stream()
                .collect(Collectors.toMap(
                        answer -> answer.getQuestion().getId(),
                        AnswerSubmission::getSubmittedAnswer
                ));

        List<QuestionProgressDto> questionDtos = quiz.getQuestions().stream()
                .map(question -> mapToQuestionProgressDto(question, userAnswerMap.get(question.getId())))
                .toList();

        Long remainingSeconds = calculateRemainingSeconds(quiz.getTimeLimitSeconds(), submission.getStartedAt());

        return new QuizProgressDto(
                submission.getId(),
                quiz.getTitle(),
                quiz.getTimeLimitSeconds(),
                remainingSeconds,
                questionDtos
        );
    }

    private QuestionProgressDto mapToQuestionProgressDto(QuizQuestion question, String userAnswer) {
        String questionType = "";
        List<ChoiceDto> choices = Collections.emptyList();

        if (question instanceof MultipleChoiceQuestion mcq) {
            questionType = "MULTIPLE_CHOICE";
            choices = mcq.getChoices().stream()
                    .map(choice -> new ChoiceDto(choice.getId(), choice.getChoiceText()))
                    .toList();
        } else if (question instanceof ShortAnswerQuestion) {
            questionType = "SHORT_ANSWER";
        }

        return new QuestionProgressDto(
                question.getId(),
                questionType,
                question.getQuestionText(),
                choices,
                userAnswer
        );
    }

    private Long calculateRemainingSeconds(Integer timeLimitSeconds, Instant startTime) {
        if (timeLimitSeconds == null || timeLimitSeconds <= 0) {
            return null;
        }

        if (startTime == null) {
            return timeLimitSeconds.longValue();
        }

        long secondsElapsed = Duration.between(startTime, Instant.now(clock)).getSeconds();
        long secondsRemaining = timeLimitSeconds - secondsElapsed;

        return Math.max(0, secondsRemaining);
    }

    private AnswerResultDto mapToAnswerResultDto(QuizQuestion question, AnswerSubmission userAnswer) {
        return question.toAnswerResultDto(userAnswer);
    }
}