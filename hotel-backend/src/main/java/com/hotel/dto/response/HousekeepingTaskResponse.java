package com.hotel.dto.response;

import com.hotel.entity.HousekeepingTask.*;
import com.hotel.entity.Room.RoomStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record HousekeepingTaskResponse(
    Long id,
    TaskType taskType,
    TaskStatus status,
    Priority priority,
    String notes,
    LocalDate scheduledDate,
    LocalDateTime startedAt,
    LocalDateTime completedAt,
    LocalDateTime createdAt,
    RoomSummary room,
    StaffSummary assignedTo
) {
    public record RoomSummary(Long id, String roomNumber, int floor, RoomStatus status) {}
    public record StaffSummary(Long id, String firstName, String lastName) {}
}
