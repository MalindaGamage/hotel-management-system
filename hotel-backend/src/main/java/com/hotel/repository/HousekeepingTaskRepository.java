package com.hotel.repository;

import com.hotel.entity.HousekeepingTask;
import com.hotel.entity.HousekeepingTask.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HousekeepingTaskRepository extends JpaRepository<HousekeepingTask, Long> {

    @Query("""
        SELECT t FROM HousekeepingTask t
        WHERE t.room.hotel.id = :hotelId
          AND (:date IS NULL OR t.scheduledDate = :date)
          AND (:status IS NULL OR t.status = :status)
        """)
    Page<HousekeepingTask> findWithFilters(
        @Param("hotelId") Long hotelId,
        @Param("date")    LocalDate date,
        @Param("status")  TaskStatus status,
        Pageable pageable
    );

    List<HousekeepingTask> findAllByAssignedToIdAndScheduledDateAndStatusNot(
        Long staffId, LocalDate date, TaskStatus status);

    long countByRoomIdAndScheduledDateAndStatusIn(Long roomId, LocalDate date, List<TaskStatus> statuses);

    boolean existsByRoomIdAndScheduledDateAndStatusIn(Long roomId, LocalDate date, List<TaskStatus> statuses);
}
