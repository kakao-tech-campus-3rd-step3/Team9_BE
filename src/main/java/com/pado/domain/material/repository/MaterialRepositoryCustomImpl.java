package com.pado.domain.material.repository;

import com.pado.domain.dashboard.dto.LatestNoticeDto;
import com.pado.domain.dashboard.dto.QLatestNoticeDto;
import com.pado.domain.material.entity.Material;
import com.pado.domain.material.entity.MaterialCategory;
import com.pado.domain.material.entity.QMaterial;
import com.pado.domain.user.entity.QUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MaterialRepositoryCustomImpl implements MaterialRepositoryCustom{

    private final JPAQueryFactory queryFactory;
    private final QMaterial material = QMaterial.material;
    private final QUser user = QUser.user;

    @Override
    public Page<Material> findByStudyIdWithFiltersAndKeyword(
            Long studyId,
            List<MaterialCategory> categories,
            List<String> weeks,
            String keyword,
            Pageable pageable) {

        BooleanExpression predicate = material.study.id.eq(studyId)
                .and(categoriesIn(categories))
                .and(weeksIn(weeks))
                .and(keywordContains(keyword));

        List<Material> content = queryFactory
                .selectFrom(material)
                .join(material.user, user).fetchJoin()
                .where(predicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(material.createdAt.desc(), material.id.desc())
                .fetch();

        Long total = queryFactory
                .select(material.count())
                .from(material)
                .where(predicate)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    public Optional<LatestNoticeDto> findRecentNoticeAsDto(Long studyId, MaterialCategory category) {
        QMaterial material = QMaterial.material;
        QUser user = QUser.user;

        return Optional.ofNullable(
                queryFactory
                        .select(new QLatestNoticeDto(
                                material.id,
                                material.title,
                                user.nickname,
                                material.createdAt
                        ))
                        .from(material)
                        .join(material.user, user)
                        .where(
                                material.study.id.eq(studyId),
                                material.materialCategory.eq(category)
                        )
                        .orderBy(material.createdAt.desc())
                        .limit(1)
                        .fetchOne()
        );
    }

    private BooleanExpression categoriesIn(List<MaterialCategory> categories) {
        return (categories != null && !categories.isEmpty()) ? material.materialCategory.in(categories) : null;
    }

    private BooleanExpression weeksIn(List<String> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return null;
        }
        List<Integer> weekInts = weeks.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return material.materialCategory.eq(MaterialCategory.LEARNING).and(material.week.in(weekInts))
                .or(material.materialCategory.ne(MaterialCategory.LEARNING));
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return material.title.containsIgnoreCase(keyword)
                .or(material.content.containsIgnoreCase(keyword));
    }

}