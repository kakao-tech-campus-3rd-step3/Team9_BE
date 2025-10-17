package com.pado.domain.schedule.event;

public record ScheduleCreatedEvent(
        Long studyId,
        Long scheduleId,
        String title
) {
}
