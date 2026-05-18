package com.hotel.dto.response;

import com.hotel.entity.Reservation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(
    Long id,
    String confirmationNumber,
    ReservationStatus status,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    int adults,
    int children,
    String specialRequests,
    CancellationPolicy cancellationPolicy,
    LocalDateTime cancelledAt,
    String cancellationReason,
    BigDecimal totalAmount,
    BigDecimal paidAmount,
    BigDecimal balanceDue,
    BookingSource source,
    GuestSummary guest,
    HotelSummary hotel,
    LocalDateTime createdAt
) {
    public record GuestSummary(Long id, String firstName, String lastName, String email, String phone) {}
    public record HotelSummary(Long id, String name, String city) {}
}
