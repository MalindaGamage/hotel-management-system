package com.hotel.controller;

import com.hotel.dto.request.CreateReservationRequest;
import com.hotel.dto.response.PagedResponse;
import com.hotel.dto.response.ReservationResponse;
import com.hotel.entity.Reservation.ReservationStatus;
import com.hotel.service.impl.ReservationServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "Reservations", description = "Full reservation lifecycle management")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationServiceImpl reservationService;

    @Operation(summary = "List reservations with filtering and pagination")
    @GetMapping
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<PagedResponse<ReservationResponse>> getReservations(
            @RequestParam Long hotelId,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(required = false) String guestSearch,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "checkInDate") String sort) {
        return ResponseEntity.ok(reservationService.getReservations(
            hotelId, status, guestSearch, checkIn, checkOut,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @GetMapping("/confirmation/{number}")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<ReservationResponse> getByConfirmation(@PathVariable String number) {
        return ResponseEntity.ok(reservationService.getByConfirmationNumber(number));
    }

    @Operation(summary = "Get expected arrivals for a date")
    @GetMapping("/arrivals")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<List<ReservationResponse>> getArrivals(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getExpectedArrivals(hotelId, date));
    }

    @Operation(summary = "Get expected departures for a date")
    @GetMapping("/departures")
    @PreAuthorize("hasAuthority('reservations:read')")
    public ResponseEntity<List<ReservationResponse>> getDepartures(
            @RequestParam Long hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(reservationService.getExpectedDepartures(hotelId, date));
    }

    @Operation(summary = "Create a new reservation")
    @PostMapping
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.create(request));
    }

    @Operation(summary = "Confirm a pending reservation")
    @PatchMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ReservationResponse> confirm(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirm(id));
    }

    @Operation(summary = "Check in a guest")
    @PatchMapping("/{id}/check-in")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ReservationResponse> checkIn(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.checkIn(id));
    }

    @Operation(summary = "Check out a guest")
    @PatchMapping("/{id}/check-out")
    @PreAuthorize("hasAuthority('reservations:write')")
    public ResponseEntity<ReservationResponse> checkOut(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.checkOut(id));
    }

    @Operation(summary = "Cancel a reservation")
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('reservations:delete')")
    public ResponseEntity<ReservationResponse> cancel(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", null) : null;
        return ResponseEntity.ok(reservationService.cancel(id, reason));
    }
}
