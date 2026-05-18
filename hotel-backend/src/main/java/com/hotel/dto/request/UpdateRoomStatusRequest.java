package com.hotel.dto.request;

import com.hotel.entity.Room.RoomStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRoomStatusRequest(@NotNull RoomStatus status, String notes) {}
