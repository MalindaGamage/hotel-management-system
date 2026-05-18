package com.hotel.dto.response;

import com.hotel.entity.Guest.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record GuestResponse(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phone,
    LocalDate dateOfBirth,
    String nationality,
    IdType idType,
    String idNumber,
    int loyaltyPoints,
    LoyaltyTier loyaltyTier,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String country,
    String postalCode,
    boolean isVerified,
    LocalDateTime createdAt
) {}
