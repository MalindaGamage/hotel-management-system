package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "housekeeping_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HousekeepingTask extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to")
    private Staff assignedTo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false, length = 20)
    private TaskType taskType = TaskType.CLEANING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.PENDING;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.NORMAL;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @NotNull
    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public enum TaskType {
        CLEANING, INSPECTION, MAINTENANCE, TURNDOWN
    }

    public enum TaskStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    }

    public enum Priority {
        LOW, NORMAL, HIGH, URGENT
    }
}
