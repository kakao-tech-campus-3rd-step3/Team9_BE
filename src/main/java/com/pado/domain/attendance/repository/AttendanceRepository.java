package com.pado.domain.attendance.repository;

import com.pado.domain.attendance.entity.Attendance;
import com.pado.domain.schedule.entity.Schedule;
import com.pado.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    boolean existsByScheduleAndUser(Schedule schedule, User user);

    @Query("""
        select a
        from Attendance a
        join fetch a.schedule s
        join fetch a.user u
        where s.studyId = :studyId
    """)
    List<Attendance> findAllByStudyIdWithScheduleAndUser(@Param("studyId") Long studyId);

    int countByScheduleId(Long scheduleId);

    interface UserAttendanceCount {
        Long getUserId();
        Long getCnt();
    }

    @Query("""
        select a.user.id as userId, count(a) as cnt
        from Attendance a
        join a.schedule s
        where s.studyId = :studyId
        group by a.user.id
    """)
    List<UserAttendanceCount> countByStudyGroupedByUser(@Param("studyId") Long studyId);

    default Map<Long, Long> countMapByStudy(Long studyId) {
        return countByStudyGroupedByUser(studyId).stream()
                .collect(Collectors.toMap(UserAttendanceCount::getUserId, UserAttendanceCount::getCnt));
    }
}
