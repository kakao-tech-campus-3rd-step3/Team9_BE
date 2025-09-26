package com.pado.global.exception.common;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVALID_INPUT", "요청 값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "일시적인 오류가 발생했습니다."),
    JSON_PARSE_ERROR(HttpStatus.BAD_REQUEST, "JSON_PARSE_ERROR", "요청 본문을 해석할 수 없습니다."),

    // Authentication and Authorization
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "인증에 실패했습니다."),
    UNAUTHENTICATED_USER(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED_USER", "인증되지 않은 사용자입니다."),
    VERIFICATION_CODE_MISMATCH(HttpStatus.UNAUTHORIZED, "VERIFICATION_CODE_MISMATCH",
        "인증 코드가 일치하지 않습니다."),
    FORBIDDEN_STUDY_LEADER_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN_STUDY_LEADER_ONLY",
        "스터디 리더만 접근할 수 있습니다."),
    FORBIDDEN_STUDY_MEMBER_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN_STUDY_MEMBER_ONLY",
        "스터디 멤버만 접근할 수 있습니다."),
    FORBIDDEN_OWNER_OR_LEADER_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN_OWNER_OR_LEADER_ONLY",
        "소유자 또는 스터디 리더만 접근할 수 있습니다."),


    // Validation
    INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_NICKNAME_FORMAT",
        "닉네임 형식이 올바르지 않습니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_FORMAT", "이메일 형식이 올바르지 않습니다."),
    INVALID_MAX_MEMBERS(HttpStatus.BAD_REQUEST, "INVALID_MAX_MEMBERS", "최대 멤버 수가 올바르지 않습니다."),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, "INVALID_ROLE", "역할이 올바르지 않습니다."),
    INVALID_MATERIAL_CATEGORY(HttpStatus.BAD_REQUEST, "INVALID_MATERIAL_CATEGORY",
        "자료 카테고리가 올바르지 않습니다."),
    INVALID_MATERIAL_WEEK_REQUIRED(HttpStatus.BAD_REQUEST, "INVALID_MATERIAL_WEEK_REQUIRED",
        "학습자료에는 주차 정보가 필수입니다."),
    INVALID_MATERIAL_WEEK_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "INVALID_MATERIAL_WEEK_NOT_ALLOWED",
        "학습자료가 아닌 카테고리에서는 주차를 설정할 수 없습니다."),
    INVALID_START_TIME(HttpStatus.BAD_REQUEST, "INVALID_START_TIME", "시작 시간이 올바르지 않습니다."),
    INVALID_CANDIDATE_DATES(HttpStatus.BAD_REQUEST, "INVALID_CANDIDATE_DATES", "가능 날짜가 올바르지 않습니다."),

    // Jwt
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "TOKEN_INVALID", "유효하지 않은 토큰입니다."),

    // Domain Common
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ENTITY_NOT_FOUND", "요청한 대상을 찾을 수 없습니다."),
    DUPLICATE_KEY(HttpStatus.CONFLICT, "DUPLICATE_KEY", "이미 존재하는 값입니다."),

    // User Domain
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "DUPLICATE_EMAIL", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),
    DUPLICATE_INTEREST(HttpStatus.CONFLICT, "DUPLICATE_INTEREST", "이미 등록된 관심분야입니다."),

    // Study Domain
    STUDY_NOT_FOUND(HttpStatus.NOT_FOUND, "STUDY_NOT_FOUND", "스터디를 찾을 수 없습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "멤버를 찾을 수 없습니다."),
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "일정을 찾을 수 없습니다."),
    PENDING_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "PENDING_SCHEDULE_NOT_FOUND",
        "승인 대기 중인 일정을 찾을 수 없습니다."),
    INVALID_STATE_CHANGE(HttpStatus.CONFLICT, "INVALID_STATE_CHANGE", "상태 변경이 유효하지 않습니다."),
    ALREADY_CHECKED_IN(HttpStatus.CONFLICT, "ALREADY_CHECKED_IN", "이미 출석 체크되었습니다."),
    ALREADY_MEMBER(HttpStatus.CONFLICT, "ALREADY_MEMBER", "이미 스터디의 멤버입니다."),
    ALREADY_APPLIED(HttpStatus.CONFLICT, "ALREADY_APPLIED", "이미 스터디에 참여 신청했습니다."),
    STUDY_NOT_RECRUITING(HttpStatus.BAD_REQUEST, "STUDY_NOT_RECRUITING", "모집 중인 스터디가 아닙니다."),
    STUDY_FULL(HttpStatus.BAD_REQUEST, "STUDY_FULL", "스터디 정원이 가득 찼습니다."),

    // Material Domain
    MATERIAL_NOT_FOUND(HttpStatus.NOT_FOUND, "MATERIAL_NOT_FOUND", "자료를 찾을 수 없습니다."),
    FORBIDDEN_MATERIAL_ACCESS(HttpStatus.FORBIDDEN, "FORBIDDEN_MATERIAL_ACCESS",
        "자료에 접근할 권한이 없습니다."),

    // Reflection Domain
    REFLECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "REFLECTION_NOT_FOUND", "회고를 찾을 수 없습니다."),
    FORBIDDEN_REFLECTION_OWNER_ONLY(HttpStatus.FORBIDDEN, "FORBIDDEN_REFLECTION_OWNER_ONLY",
        "본인만 수정/삭제할 수 있습니다."),
    ALREADY_REFLECTED(HttpStatus.CONFLICT, "ALREADY_REFLECTED", "이미 해당 일정에 회고를 작성했습니다."),
    INVALID_REFLECTION_SCORE(HttpStatus.BAD_REQUEST, "INVALID_REFLECTION_SCORE",
        "점수는 1~5 사이여야 합니다."),


    // File/S3 Domain
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "FILE_NOT_FOUND", "파일을 찾을 수 없습니다."),
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "FILE_DELETE_FAILED", "파일 삭제에 실패했습니다."),
    INVALID_FILE_FORMAT(HttpStatus.BAD_REQUEST, "INVALID_FILE_FORMAT", "지원하지 않는 파일 형식입니다."),
    S3_SERVICE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "S3_SERVICE_ERROR", "파일 서비스 오류가 발생했습니다."),

    //Progress Domain
    CHAPTER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAPTER_NOT_FOUND", "로드맵 차시를 찾을 수 없습니다."),

    //Redis
    REDIS_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "REDIS_UNAVAILABLE", "Redis 연결에 실패했습니다.");
    public final HttpStatus status;
    public final String code;
    public final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}