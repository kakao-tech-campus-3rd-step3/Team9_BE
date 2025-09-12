package com.pado.domain.study.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudy is a Querydsl query type for Study
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudy extends EntityPathBase<Study> {

    private static final long serialVersionUID = -668177880L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudy study = new QStudy("study");

    public final com.pado.domain.basetime.QAuditingEntity _super = new com.pado.domain.basetime.QAuditingEntity(this);

    public final ListPath<String, StringPath> conditions = this.<String, StringPath>createList("conditions", String.class, StringPath.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath description = createString("description");

    public final StringPath detailDescription = createString("detailDescription");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageUrl = createString("imageUrl");

    public final ListPath<StudyCategory, QStudyCategory> interests = this.<StudyCategory, QStudyCategory>createList("interests", StudyCategory.class, QStudyCategory.class, PathInits.DIRECT2);

    public final com.pado.domain.user.entity.QUser leader;

    public final NumberPath<Integer> maxMembers = createNumber("maxMembers", Integer.class);

    public final EnumPath<com.pado.domain.shared.entity.Region> region = createEnum("region", com.pado.domain.shared.entity.Region.class);

    public final EnumPath<StudyStatus> status = createEnum("status", StudyStatus.class);

    public final StringPath studyTime = createString("studyTime");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QStudy(String variable) {
        this(Study.class, forVariable(variable), INITS);
    }

    public QStudy(Path<? extends Study> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudy(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudy(PathMetadata metadata, PathInits inits) {
        this(Study.class, metadata, inits);
    }

    public QStudy(Class<? extends Study> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.leader = inits.isInitialized("leader") ? new com.pado.domain.user.entity.QUser(forProperty("leader")) : null;
    }

}

