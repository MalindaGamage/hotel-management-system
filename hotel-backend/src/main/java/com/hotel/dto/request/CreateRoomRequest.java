package com.hotel.dto.request;

import jakarta.validation.constraints.*;

public record CreateRoomRequest(
    @NotNull Long hotelId,
    @NotNull Long roomTypeId,
    @NotBlank @Size(max = 20) String roomNumber,
    @Min(0) int floor,
    String notes
) {}
