package com.pado.domain.material.event;

public record NoticeCreatedEvent(
        Long studyId,
        Long noticeId,
        String title
) {}
