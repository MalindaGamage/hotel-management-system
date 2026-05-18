package com.hotel.dto.response;

import com.hotel.entity.Room.RoomStatus;

import java.math.BigDecimal;
import java.util.List;

public record RoomResponse(
    Long id,
    String roomNumber,
    int floor,
    RoomStatus status,
    String notes,
    Long hotelId,
    RoomTypeSummary roomType
) {
    public record RoomTypeSummary(
        Long id,
        String name,
        int maxOccupancy,
        BigDecimal basePrice,
        List<String> amenities
    ) {}
}
