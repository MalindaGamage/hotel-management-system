package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reservations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @NotBlank
    @Column(name = "confirmation_number", nullable = false, unique = true, length = 20)
    private String confirmationNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status = ReservationStatus.PENDING;

    @NotNull
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Min(1)
    @Column(nullable = false)
    private int adults = 1;

    @Min(0)
    @Column(nullable = false)
    private int children = 0;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_policy", nullable = false, length = 20)
    private CancellationPolicy cancellationPolicy = CancellationPolicy.MODERATE;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @DecimalMin("0.00")
    @Column(name = "paid_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingSource source = BookingSource.DIRECT;

    // Optimistic locking — critical: prevents double-booking under concurrent requests
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ReservationRoom> reservationRooms = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Invoice> invoices = new ArrayList<>();

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(paidAmount);
    }

    public enum ReservationStatus {
        PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW
    }

    public enum CancellationPolicy {
        FLEXIBLE, MODERATE, STRICT
    }

    public enum BookingSource {
        DIRECT, OTA, PHONE, WALK_IN
    }
}
