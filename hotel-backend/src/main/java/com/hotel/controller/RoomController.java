package com.hotel.controller;

import com.hotel.dto.request.CreateRoomRequest;
import com.hotel.dto.request.CreateRoomTypeRequest;
import com.hotel.dto.request.UpdateRoomStatusRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.dto.response.RoomResponse;
import com.hotel.service.impl.RoomServiceImpl;
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
import java.util.List;

@Tag(name = "Rooms", description = "Room and room-type management")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class RoomController {

    private final RoomServiceImpl roomService;

    @Operation(summary = "List all rooms for a hotel (paginated)")
    @GetMapping("/rooms")
    @PreAuthorize("hasAuthority('rooms:read')")
    public ResponseEntity<PagedResponse<RoomResponse>> getRooms(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "roomNumber") String sort) {
        return ResponseEntity.ok(roomService.getRooms(hotelId,
            PageRequest.of(page, size, Sort.by(sort))));
    }

    @Operation(summary = "Get room by ID")
    @GetMapping("/rooms/{id}")
    @PreAuthorize("hasAuthority('rooms:read')")
    public ResponseEntity<RoomResponse> getRoom(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @Operation(summary = "Check room availability")
    @GetMapping("/rooms/availability")
    public ResponseEntity<List<RoomResponse>> getAvailableRooms(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false) Long roomTypeId) {
        return ResponseEntity.ok(roomService.getAvailableRooms(hotelId, checkIn, checkOut, roomTypeId));
    }

    @Operation(summary = "Create a new room")
    @PostMapping("/rooms")
    @PreAuthorize("hasAuthority('rooms:write')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody CreateRoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @Operation(summary = "Update room status (optimistic locking)")
    @PatchMapping("/rooms/{id}/status")
    @PreAuthorize("hasAuthority('rooms:write')")
    public ResponseEntity<RoomResponse> updateStatus(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateRoomStatusRequest request) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request));
    }

    @Operation(summary = "Soft-delete a room")
    @DeleteMapping("/rooms/{id}")
    @PreAuthorize("hasAuthority('rooms:delete')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    // -------------------------------------------------------------------------
    // Room Types
    // -------------------------------------------------------------------------

    @Operation(summary = "List room types for a hotel")
    @GetMapping("/room-types")
    @PreAuthorize("hasAuthority('rooms:read')")
    public ResponseEntity<PagedResponse<RoomResponse>> getRoomTypes(
            @RequestParam Long hotelId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(roomService.getRoomTypes(hotelId, PageRequest.of(page, size)));
    }

    @Operation(summary = "Create a new room type")
    @PostMapping("/room-types")
    @PreAuthorize("hasAuthority('rooms:write')")
    public ResponseEntity<RoomResponse> createRoomType(@Valid @RequestBody CreateRoomTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoomType(request));
    }
}
