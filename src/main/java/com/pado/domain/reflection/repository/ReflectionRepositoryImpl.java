package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.QReflection;
import com.pado.domain.reflection.entity.Reflection;
import com.pado.domain.reflection.repository.dto.UserReflectionCountDto;
import com.pado.domain.study.entity.QStudyMember;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.pado.domain.reflection.entity.QReflection.reflection;
import static com.pado.domain.schedule.entity.QSchedule.schedule;
import static com.pado.domain.study.entity.QStudyMember.studyMember;
import static com.pado.domain.user.entity.QUser.user;


@Repository
@RequiredArgsConstructor
public class ReflectionRepositoryImpl implements ReflectionRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<UserReflectionCountDto> countByStudyGroupByStudyMember(Long studyId) {
        QReflection r = QReflection.reflection;
        QStudyMember sm = QStudyMember.studyMember;

        return queryFactory
            .select(Projections.constructor(UserReflectionCountDto.class,
                sm.id,
                r.count()))
            .from(r)
            .join(r.studyMember, sm)
            .where(r.study.id.eq(studyId))
            .groupBy(sm.id)
            .fetch();
    }

    @Override
    public Page<Reflection> findReflectionsByStudy(Long studyId, Long userId, Pageable pageable) {
        BooleanExpression predicate = reflection.study.id.eq(studyId);
        if (userId != null) {
            predicate = predicate.and(studyMember.user.id.eq(userId));
        }

        List<Reflection> content = queryFactory
            .selectFrom(reflection)
            .leftJoin(reflection.studyMember, studyMember).fetchJoin()
            .leftJoin(studyMember.user, user).fetchJoin()
            .leftJoin(reflection.schedule, schedule).fetchJoin()
            .where(predicate)
            .orderBy(reflection.updatedAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(reflection.count())
            .from(reflection)
            .join(reflection.studyMember, studyMember)
            .where(predicate)
            .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}