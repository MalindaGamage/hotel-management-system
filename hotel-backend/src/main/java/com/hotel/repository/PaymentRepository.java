package com.hotel.repository;

import com.hotel.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    boolean existsByIdempotencyKey(String idempotencyKey);

    List<Payment> findAllByReservationId(Long reservationId);

    @Query("SELECT COALESCE(SUM(p.amount),0) FROM Payment p WHERE p.reservation.id = :reservationId AND p.paymentStatus = 'COMPLETED'")
    BigDecimal sumCompletedPaymentsByReservation(@Param("reservationId") Long reservationId);
}
