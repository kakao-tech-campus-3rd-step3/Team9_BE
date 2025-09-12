package com.pado.domain.user.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 189425912L;

    public static final QUser user = new QUser("user");

    public final com.pado.domain.basetime.QAuditingEntity _super = new com.pado.domain.basetime.QAuditingEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final EnumPath<Gender> gender = createEnum("gender", Gender.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<UserInterest, QUserInterest> interests = this.<UserInterest, QUserInterest>createList("interests", UserInterest.class, QUserInterest.class, PathInits.DIRECT2);

    public final StringPath nickname = createString("nickname");

    public final StringPath passwordHash = createString("passwordHash");

    public final StringPath profileImageUrl = createString("profileImageUrl");

    public final EnumPath<com.pado.domain.shared.entity.Region> region = createEnum("region", com.pado.domain.shared.entity.Region.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

