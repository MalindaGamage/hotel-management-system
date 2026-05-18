package com.hotel.repository;

import com.hotel.entity.Guest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GuestRepository extends JpaRepository<Guest, Long> {

    Optional<Guest> findByEmailAndDeletedAtIsNull(String email);

    Optional<Guest> findByIdAndDeletedAtIsNull(Long id);

    boolean existsByEmailAndDeletedAtIsNull(String email);

    @Query("""
        SELECT g FROM Guest g
        WHERE g.deletedAt IS NULL
          AND (:search IS NULL OR :search = ''
               OR LOWER(g.firstName) LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(g.lastName)  LIKE LOWER(CONCAT('%',:search,'%'))
               OR LOWER(g.email)     LIKE LOWER(CONCAT('%',:search,'%'))
               OR g.phone            LIKE CONCAT('%',:search,'%'))
        """)
    Page<Guest> searchGuests(@Param("search") String search, Pageable pageable);
}
