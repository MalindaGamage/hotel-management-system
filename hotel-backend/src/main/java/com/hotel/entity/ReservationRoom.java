package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservation_rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @NotNull
    @DecimalMin("0.00")
    @Column(name = "rate_per_night", nullable = false, precision = 10, scale = 2)
    private BigDecimal ratePerNight;

    @NotNull
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @NotNull
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "actual_check_in")
    private LocalDateTime actualCheckIn;

    @Column(name = "actual_check_out")
    private LocalDateTime actualCheckOut;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public long getNights() {
        return checkInDate.until(checkOutDate).getDays();
    }

    public BigDecimal getTotalAmount() {
        return ratePerNight.multiply(BigDecimal.valueOf(getNights()));
    }
}
