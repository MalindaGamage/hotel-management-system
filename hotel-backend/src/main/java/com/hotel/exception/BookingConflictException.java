package com.hotel.exception;

public class BookingConflictException extends ConflictException {
    public BookingConflictException(String message) { super(message); }
    public BookingConflictException(long roomId, String checkIn, String checkOut) {
        super(String.format("Room %d is already booked for %s – %s", roomId, checkIn, checkOut));
    }
}
