package com.hotel.repository;

import com.hotel.entity.Reservation;
import com.hotel.entity.Reservation.ReservationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByIdAndDeletedAtIsNull(Long id);

    Optional<Reservation> findByConfirmationNumberAndDeletedAtIsNull(String confirmationNumber);

    @Query("""
        SELECT r FROM Reservation r
        WHERE r.hotel.id   = :hotelId
          AND r.deletedAt  IS NULL
          AND (:status     IS NULL OR r.status = :status)
          AND (:guestSearch IS NULL OR :guestSearch = ''
               OR LOWER(r.guest.firstName) LIKE LOWER(CONCAT('%',:guestSearch,'%'))
               OR LOWER(r.guest.lastName)  LIKE LOWER(CONCAT('%',:guestSearch,'%')))
          AND (:checkIn    IS NULL OR r.checkInDate  >= :checkIn)
          AND (:checkOut   IS NULL OR r.checkOutDate <= :checkOut)
        """)
    Page<Reservation> findWithFilters(
        @Param("hotelId")      Long hotelId,
        @Param("status")       ReservationStatus status,
        @Param("guestSearch")  String guestSearch,
        @Param("checkIn")      LocalDate checkIn,
        @Param("checkOut")     LocalDate checkOut,
        Pageable pageable
    );

    @Query("SELECT r FROM Reservation r WHERE r.hotel.id = :hotelId AND r.checkInDate = :date AND r.status = 'CONFIRMED' AND r.deletedAt IS NULL")
    List<Reservation> findExpectedArrivals(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    @Query("SELECT r FROM Reservation r WHERE r.hotel.id = :hotelId AND r.checkOutDate = :date AND r.status = 'CHECKED_IN' AND r.deletedAt IS NULL")
    List<Reservation> findExpectedDepartures(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.hotel.id = :hotelId AND r.checkInDate = :date AND r.status NOT IN ('CANCELLED','NO_SHOW')")
    long countArrivalsForDate(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.hotel.id = :hotelId AND r.checkOutDate = :date AND r.status = 'CHECKED_IN'")
    long countDeparturesForDate(@Param("hotelId") Long hotelId, @Param("date") LocalDate date);

    @Query("""
        SELECT COALESCE(SUM(r.totalAmount), 0) FROM Reservation r
        WHERE r.hotel.id = :hotelId
          AND r.status NOT IN ('CANCELLED','NO_SHOW')
          AND r.checkInDate BETWEEN :from AND :to
          AND r.deletedAt IS NULL
        """)
    java.math.BigDecimal sumRevenueByDateRange(
        @Param("hotelId") Long hotelId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        SELECT r.checkInDate AS date, COUNT(r) AS reservations, COALESCE(SUM(r.totalAmount),0) AS revenue
        FROM Reservation r
        WHERE r.hotel.id = :hotelId
          AND r.status NOT IN ('CANCELLED','NO_SHOW')
          AND r.checkInDate BETWEEN :from AND :to
          AND r.deletedAt IS NULL
        GROUP BY r.checkInDate
        ORDER BY r.checkInDate
        """)
    List<Object[]> findDailyRevenue(
        @Param("hotelId") Long hotelId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
}
