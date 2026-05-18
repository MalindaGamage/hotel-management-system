package com.hotel.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest extends BaseEntity {

    @NotBlank
    @Size(max = 100)
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 30)
    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(length = 100)
    private String nationality;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", length = 20)
    private IdType idType;

    @Column(name = "id_number", length = 100)
    private String idNumber;

    @Min(0)
    @Column(name = "loyalty_points", nullable = false)
    private int loyaltyPoints = 0;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "loyalty_tier", nullable = false, length = 20)
    private LoyaltyTier loyaltyTier = LoyaltyTier.BRONZE;

    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String country;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @OneToMany(mappedBy = "guest", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Reservation> reservations = new ArrayList<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Recalculate tier based on current points
    public void recalculateLoyaltyTier() {
        if (loyaltyPoints >= 10000) loyaltyTier = LoyaltyTier.PLATINUM;
        else if (loyaltyPoints >= 5000) loyaltyTier = LoyaltyTier.GOLD;
        else if (loyaltyPoints >= 1000) loyaltyTier = LoyaltyTier.SILVER;
        else loyaltyTier = LoyaltyTier.BRONZE;
    }

    public enum IdType {
        PASSPORT, NATIONAL_ID, DRIVERS_LICENSE
    }

    public enum LoyaltyTier {
        BRONZE, SILVER, GOLD, PLATINUM
    }
}
