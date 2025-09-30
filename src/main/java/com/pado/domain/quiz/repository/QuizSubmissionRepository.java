package com.pado.domain.quiz.repository;

import com.pado.domain.quiz.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long>, QuizSubmissionRepositoryCustom {
    Optional<QuizSubmission> findByQuizIdAndUserId(Long quizId, Long userId);

    interface UserQuizCount {
        Long getUserId();
        Long getCnt();
    }

    @Query("""
                select qs.user.id as userId, count(qs) as cnt
                from QuizSubmission qs
                where qs.quiz.study.id = :studyId
                group by qs.user.id
            """
    )
    List<UserQuizCount> countByStudyGroupByUser(@Param("studyId") Long studyId);

    default Map<Long, Long> countMapByStudy(Long studyId) {
        return countByStudyGroupByUser(studyId).stream()
                .collect(Collectors.toMap(UserQuizCount::getUserId, UserQuizCount::getCnt));
    }
}