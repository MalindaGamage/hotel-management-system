package com.hotel.dto.request;

import com.hotel.entity.Reservation.BookingSource;
import com.hotel.entity.Reservation.CancellationPolicy;
import com.hotel.validation.FutureDateRange;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

@FutureDateRange(maxNights = 30)
public record CreateReservationRequest(
    @NotNull Long hotelId,
    @NotNull Long guestId,
    @NotNull @FutureOrPresent LocalDate checkInDate,
    @NotNull LocalDate checkOutDate,
    @Min(1) int adults,
    @Min(0) int children,
    @NotEmpty List<RoomSelection> rooms,
    String specialRequests,
    CancellationPolicy cancellationPolicy,
    BookingSource source
) {
    public record RoomSelection(
        @NotNull Long roomId,
        @NotNull Long roomTypeId
    ) {}
}
