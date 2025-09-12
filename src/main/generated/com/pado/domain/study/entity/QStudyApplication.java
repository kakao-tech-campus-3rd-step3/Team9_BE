package com.pado.domain.study.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStudyApplication is a Querydsl query type for StudyApplication
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStudyApplication extends EntityPathBase<StudyApplication> {

    private static final long serialVersionUID = 379378184L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStudyApplication studyApplication = new QStudyApplication("studyApplication");

    public final com.pado.domain.basetime.QAuditingEntity _super = new com.pado.domain.basetime.QAuditingEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath message = createString("message");

    public final EnumPath<StudyApplicationStatus> status = createEnum("status", StudyApplicationStatus.class);

    public final QStudy study;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final com.pado.domain.user.entity.QUser user;

    public QStudyApplication(String variable) {
        this(StudyApplication.class, forVariable(variable), INITS);
    }

    public QStudyApplication(Path<? extends StudyApplication> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStudyApplication(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStudyApplication(PathMetadata metadata, PathInits inits) {
        this(StudyApplication.class, metadata, inits);
    }

    public QStudyApplication(Class<? extends StudyApplication> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.study = inits.isInitialized("study") ? new QStudy(forProperty("study"), inits.get("study")) : null;
        this.user = inits.isInitialized("user") ? new com.pado.domain.user.entity.QUser(forProperty("user")) : null;
    }

}

