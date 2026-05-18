package com.hotel.service.impl;

import com.hotel.dto.request.UpdateHousekeepingTaskRequest;
import com.hotel.dto.response.HousekeepingTaskResponse;
import com.hotel.dto.response.PagedResponse;
import com.hotel.entity.*;
import com.hotel.entity.HousekeepingTask.*;
import com.hotel.exception.ResourceNotFoundException;
import com.hotel.mapper.HousekeepingTaskMapper;
import com.hotel.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HousekeepingServiceImpl {

    private final HousekeepingTaskRepository taskRepository;
    private final RoomRepository roomRepository;
    private final StaffRepository staffRepository;
    private final HousekeepingTaskMapper taskMapper;

    public PagedResponse<HousekeepingTaskResponse> getTasks(
            Long hotelId, LocalDate date, TaskStatus status, Pageable pageable) {
        return PagedResponse.of(
            taskRepository.findWithFilters(hotelId, date, status, pageable).map(taskMapper::toResponse));
    }

    @Transactional
    public HousekeepingTaskResponse createTask(Long roomId, Long assignedToId, TaskType type,
                                               Priority priority, LocalDate date, String notes) {
        Room room = roomRepository.findByIdAndDeletedAtIsNull(roomId)
            .orElseThrow(() -> new ResourceNotFoundException("Room", roomId));
        Staff staff = assignedToId != null
            ? staffRepository.findById(assignedToId).orElse(null) : null;

        HousekeepingTask task = HousekeepingTask.builder()
            .room(room).assignedTo(staff).taskType(type)
            .status(TaskStatus.PENDING).priority(priority != null ? priority : Priority.NORMAL)
            .scheduledDate(date != null ? date : LocalDate.now())
            .notes(notes).build();
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Transactional
    public HousekeepingTaskResponse updateTask(Long id, UpdateHousekeepingTaskRequest req) {
        HousekeepingTask task = taskRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("HousekeepingTask", id));

        if (req.status() != null) {
            task.setStatus(req.status());
            if (req.status() == TaskStatus.IN_PROGRESS && task.getStartedAt() == null) {
                task.setStartedAt(LocalDateTime.now());
            }
            if (req.status() == TaskStatus.COMPLETED) {
                task.setCompletedAt(LocalDateTime.now());
                if (task.getTaskType() == TaskType.CLEANING) {
                    task.getRoom().setStatus(Room.RoomStatus.CLEAN);
                    roomRepository.save(task.getRoom());
                } else if (task.getTaskType() == TaskType.INSPECTION) {
                    task.getRoom().setStatus(Room.RoomStatus.INSPECTED);
                    roomRepository.save(task.getRoom());
                }
            }
        }
        if (req.priority() != null) task.setPriority(req.priority());
        if (req.scheduledDate() != null) task.setScheduledDate(req.scheduledDate());
        if (req.notes() != null) task.setNotes(req.notes());
        if (req.assignedToId() != null) {
            staffRepository.findById(req.assignedToId()).ifPresent(task::setAssignedTo);
        }
        return taskMapper.toResponse(taskRepository.save(task));
    }
}
