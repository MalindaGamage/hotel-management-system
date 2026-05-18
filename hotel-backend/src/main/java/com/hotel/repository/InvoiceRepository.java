package com.hotel.repository;

import com.hotel.entity.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByIdAndDeletedAtIsNull(Long id);

    Optional<Invoice> findByInvoiceNumberAndDeletedAtIsNull(String invoiceNumber);

    Page<Invoice> findAllByDeletedAtIsNull(Pageable pageable);

    List<Invoice> findAllByReservationIdAndDeletedAtIsNull(Long reservationId);

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING_INDEX(i.invoiceNumber,'-',-1) AS INTEGER)),0) FROM Invoice i")
    Integer findMaxInvoiceSequence();
}
