package com.hotel.dto.request;

import com.hotel.entity.Guest.IdType;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateGuestRequest(
    @NotBlank @Size(max = 100) String firstName,
    @NotBlank @Size(max = 100) String lastName,
    @Email @NotBlank String email,
    @Size(max = 30) String phone,
    LocalDate dateOfBirth,
    String nationality,
    IdType idType,
    String idNumber,
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String country,
    String postalCode
) {}
