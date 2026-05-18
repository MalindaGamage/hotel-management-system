package com.hotel.controller;

import com.hotel.dto.request.UpdateHousekeepingTaskRequest;
import com.hotel.dto.response.HousekeepingTaskResponse;
import com.hotel.dto.response.PagedResponse;
import com.hotel.entity.HousekeepingTask.*;
import com.hotel.service.impl.HousekeepingServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Tag(name = "Housekeeping", description = "Room status board and task management")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/housekeeping")
@RequiredArgsConstructor
public class HousekeepingController {

    private final HousekeepingServiceImpl housekeepingService;

    @GetMapping("/tasks")
    @PreAuthorize("hasAuthority('housekeeping:read')")
    public ResponseEntity<PagedResponse<HousekeepingTaskResponse>> getTasks(
            @RequestParam Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(housekeepingService.getTasks(hotelId, date, status,
            PageRequest.of(page, size, Sort.by("priority").descending().and(Sort.by("scheduledDate")))));
    }

    @Operation(summary = "Create a housekeeping task")
    @PostMapping("/tasks")
    @PreAuthorize("hasAuthority('housekeeping:write')")
    public ResponseEntity<HousekeepingTaskResponse> createTask(@RequestBody Map<String, Object> body) {
        Long roomId      = Long.parseLong(body.get("roomId").toString());
        Long assignedTo  = body.get("assignedToId") != null ? Long.parseLong(body.get("assignedToId").toString()) : null;
        TaskType type    = TaskType.valueOf(body.getOrDefault("taskType", "CLEANING").toString());
        Priority priority = Priority.valueOf(body.getOrDefault("priority", "NORMAL").toString());
        LocalDate date   = body.get("scheduledDate") != null
            ? LocalDate.parse(body.get("scheduledDate").toString()) : LocalDate.now();
        String notes     = body.getOrDefault("notes", "").toString();
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(housekeepingService.createTask(roomId, assignedTo, type, priority, date, notes));
    }

    @Operation(summary = "Update task status, priority, or assignment")
    @PatchMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('housekeeping:write')")
    public ResponseEntity<HousekeepingTaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody UpdateHousekeepingTaskRequest request) {
        return ResponseEntity.ok(housekeepingService.updateTask(id, request));
    }
}
