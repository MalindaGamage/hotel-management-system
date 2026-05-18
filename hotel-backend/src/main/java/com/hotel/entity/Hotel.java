package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hotel extends BaseEntity {

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String name;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String address;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @NotBlank
    @Column(nullable = false, length = 30)
    private String phone;

    @Email
    @NotBlank
    @Column(nullable = false, length = 150)
    private String email;

    @Min(1) @Max(5)
    @Column(name = "star_rating", nullable = false, columnDefinition = "TINYINT")
    private int starRating = 3;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "check_in_time", nullable = false)
    private LocalTime checkInTime = LocalTime.of(14, 0);

    @Column(name = "check_out_time", nullable = false)
    private LocalTime checkOutTime = LocalTime.of(11, 0);

    @Column(length = 60)
    private String timezone = "UTC";

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Room> rooms = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<RoomType> roomTypes = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Staff> staff = new ArrayList<>();

    @OneToMany(mappedBy = "hotel", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();
}
