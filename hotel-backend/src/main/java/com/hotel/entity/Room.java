package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "rooms",
       uniqueConstraints = @UniqueConstraint(name = "uq_room_number_hotel",
               columnNames = {"hotel_id", "room_number"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @NotBlank
    @Size(max = 20)
    @Column(name = "room_number", nullable = false, length = 20)
    private String roomNumber;

    @Min(0)
    @Column(nullable = false)
    private int floor = 1;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(columnDefinition = "TEXT")
    private String notes;

    // Optimistic locking — prevents concurrent room-status races
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public enum RoomStatus {
        AVAILABLE, OCCUPIED, DIRTY, CLEAN, INSPECTED, OUT_OF_ORDER, MAINTENANCE
    }
}
