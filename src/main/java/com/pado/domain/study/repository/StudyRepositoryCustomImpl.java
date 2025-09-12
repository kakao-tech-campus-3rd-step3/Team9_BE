package com.pado.domain.study.repository;

import com.pado.domain.shared.entity.Category;
import com.pado.domain.shared.entity.Region;
import com.pado.domain.study.entity.QStudy;
import com.pado.domain.study.entity.QStudyCategory;
import com.pado.domain.study.entity.Study;
import com.pado.domain.study.entity.StudyStatus;
import com.pado.domain.user.entity.User;
import com.pado.domain.user.entity.UserInterest;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class StudyRepositoryCustomImpl implements StudyRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final QStudy study = QStudy.study;

    @Override
    public Slice<Study> findStudiesByFilter(User user, String keyword, List<Category> categories, List<Region> regions, Pageable pageable) {
        // 로그인 유무 검증 후 추천 점수 생성
        List<OrderSpecifier<?>> orderSpecifiers = createOrderSpecifiers(user);

        // QueryDSL로 조회
        List<Study> content = queryFactory
                .selectFrom(study)
                .where(
                        study.status.eq(StudyStatus.RECRUITING), // 모집중인 스터디만 조회
                        keywordContains(keyword), // 키워드 필터
                        categoriesIn(categories), // 관심사 필터
                        regionsIn(regions) // 지역 필터
                )
                .offset(pageable.getOffset()) // 조회 시작할 데이터 위치
                .limit(pageable.getPageSize() + 1) // 다음 페이지 존재 확인을 위해 +1개 조회
                .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0])) // 정렬 적용
                .fetch();

        // +1로 조회한 extra 데이터 제거 및 hasNext 판단
        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.remove(pageable.getPageSize());
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }

    private List<OrderSpecifier<?>> createOrderSpecifiers(User user) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        // 로그인 된 사용자의 경우 : 추천 점수 생성
        if (user != null && user.getId() != null) {
            // 1. 관심사 점수 (최대 50점): (겹치는 개수 / 사용자 전체 관심사 개수) * 50
            Expression<Integer> interestScore;
            int totalUserInterests = user.getInterests().size(); // 사용자 관심사 개수

            if (totalUserInterests > 0) {
                List<Category> userCategories = user.getInterests().stream() // 사용자 관심사
                        .map(UserInterest::getCategory)
                        .collect(Collectors.toList());

                QStudyCategory studyCategory = QStudyCategory.studyCategory;
                // 관심사 점수 계산
                interestScore = JPAExpressions
                        .select(studyCategory.count()
                                .multiply(50)
                                .divide(totalUserInterests)
                                .castToNum(Integer.class))
                        .from(studyCategory)
                        .where(
                                studyCategory.study.eq(study),
                                studyCategory.category.in(userCategories)
                        );
            } else {
                interestScore = Expressions.asNumber(0);
            }

            // 2. 지역 점수 (30점)
            BooleanExpression regionMatches = user.getRegion() != null // 사용자의 지역과 일치하면 true, 아니면 false
                    ? study.region.eq(user.getRegion())
                    : Expressions.asBoolean(false).isTrue();
            // regionMatches가 true이면 30점, 아니면 0점
            NumberExpression<Integer> regionScore = new CaseBuilder()
                    .when(regionMatches).then(30)
                    .otherwise(0);

            // 3. 최신성 점수 (최대 30점)
            NumberExpression<Long> daysSinceCreation = Expressions.numberTemplate( // 스터디 생성일로부터 지난 일수
                    Long.class,
                    "TIMESTAMPDIFF(DAY, {0}, CURRENT_TIMESTAMP)",
                    study.createdAt
            );
            NumberExpression<Integer> recencyScore = new CaseBuilder()
                    .when(daysSinceCreation.loe(7)).then(30) // 7일 이하 : 30점
                    .when(daysSinceCreation.loe(14)).then(20) // 14일 이하 : 20점
                    .when(daysSinceCreation.loe(30)).then(10) // 30일 이하 : 10점
                    .otherwise(0);

            // 최종 추천 점수 계산 후 내림차순 정렬
            NumberExpression<Integer> totalScore = Expressions.numberTemplate(
                    Integer.class,
                    "({0} + {1} + {2})",
                    interestScore,
                    regionScore,
                    recencyScore
            );
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, totalScore));
        }

        // 최신순 -> id순 정렬
        orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, study.createdAt));
        orderSpecifiers.add(new OrderSpecifier<>(Order.ASC, study.id));

        return orderSpecifiers;
    }

    // 검색어 필터 : title 또는 description에 검색어가 포함되는지, 대소문자 무시
    private BooleanExpression keywordContains(String keyword) {
        return StringUtils.hasText(keyword)
                ? study.title.containsIgnoreCase(keyword)
                .or(study.description.containsIgnoreCase(keyword))
                : null;
    }

    // 카테고리 필터
    private BooleanExpression categoriesIn(List<Category> categories) {
        return !CollectionUtils.isEmpty(categories)
                ? study.interests.any().category.in(categories)
                : null;
    }

    // 지역 필터
    private BooleanExpression regionsIn(List<Region> regions) {
        return !CollectionUtils.isEmpty(regions)
                ? study.region.in(regions)
                : null;
    }
}