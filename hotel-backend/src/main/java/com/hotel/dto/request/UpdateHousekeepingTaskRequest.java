package com.hotel.dto.request;

import com.hotel.entity.HousekeepingTask.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record UpdateHousekeepingTaskRequest(
    TaskStatus status,
    Priority priority,
    Long assignedToId,
    LocalDate scheduledDate,
    String notes
) {}
