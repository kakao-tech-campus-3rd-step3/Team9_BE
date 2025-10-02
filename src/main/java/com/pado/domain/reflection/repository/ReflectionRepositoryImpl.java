package com.pado.domain.reflection.repository;

import com.pado.domain.reflection.entity.QReflection;
import com.pado.domain.reflection.repository.dto.UserReflectionCountDto;
import com.pado.domain.study.entity.QStudyMember;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ReflectionRepositoryImpl implements ReflectionRepositoryCustom{

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
}
