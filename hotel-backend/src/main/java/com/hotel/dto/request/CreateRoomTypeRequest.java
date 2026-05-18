package com.hotel.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record CreateRoomTypeRequest(
    @NotNull Long hotelId,
    @NotBlank @Size(max = 100) String name,
    String description,
    @Min(1) int maxOccupancy,
    @NotNull @DecimalMin("0.01") BigDecimal basePrice,
    List<String> amenities,
    List<String> imageUrls
) {}
