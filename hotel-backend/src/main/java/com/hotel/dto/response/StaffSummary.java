package com.hotel.dto.response;

public record StaffSummary(
    Long id,
    String firstName,
    String lastName,
    String email,
    String role,
    String profileImage
) {}
