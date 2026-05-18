package com.hotel.repository;

import com.hotel.entity.Staff;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff, Long> {

    Optional<Staff> findByEmailAndDeletedAtIsNull(String email);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    @Query("SELECT s FROM Staff s WHERE s.hotel.id = :hotelId AND s.deletedAt IS NULL")
    Page<Staff> findAllByHotelId(@Param("hotelId") Long hotelId, Pageable pageable);
}
