package com.pado.domain.study.repository;

import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyMember;
import com.pado.domain.study.entity.StudyMemberRole;
import com.pado.domain.user.entity.User;

import java.util.Collection;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    int countByStudyId(long studyId);

    long countByStudy(Study study);

    boolean existsByStudyAndUser(Study study, User user);

    List<StudyMember> findByStudyId(Long studyId);

    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

    @Query("""
            select sm
            from StudyMember sm
            join fetch sm.user u
            where sm.study = :study
        """)
    List<StudyMember> findByStudyWithUser(@Param("study") Study study);

    @Query("""
            select u.id
            from StudyMember sm
            join sm.user u
            where sm.role = :role and sm.study = :study
        """)
    Long findLeaderUserIdByStudy(@Param("study") Study study, @Param("role") StudyMemberRole role);

    List<StudyMember> findAllByStudyIdOrderByRankPointDesc(Long studyId);
           
    @Query("""
        select sm
        from StudyMember sm
        join fetch sm.user u
        where sm.study.id = :studyId
    """)
    List<StudyMember> findByStudyIdFetchUser(@Param("studyId") Long studyId);

    boolean existsByStudyIdAndUserIdAndRoleIn(Long studyId, Long userId, Collection<StudyMemberRole> roles);
}