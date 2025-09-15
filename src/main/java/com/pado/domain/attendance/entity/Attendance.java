package com.pado.domain.attendance.entity;

import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status", nullable = false)
    private boolean status;

    @Column(name = "check_in_time")
    private LocalDateTime checkInTime;

    @Builder
    public Attendance(Schedule schedule, User user, boolean status, LocalDateTime checkInTime) {
        this.schedule = schedule;
        this.user = user;
        this.status = status;
        this.checkInTime = checkInTime;
    }

    public static Attendance createCheckIn(Schedule schedule, User user) {
        return Attendance.builder()
                .schedule(schedule)
                .user(user)
                .status(true)
                .checkInTime(LocalDateTime.now())
                .build();
    }
}

