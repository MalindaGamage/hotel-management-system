package com.hotel.repository;

import com.hotel.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true WHERE t.family = :family")
    void revokeAllByFamily(@Param("family") String family);

    @Modifying
    @Query("UPDATE RefreshToken t SET t.isRevoked = true WHERE t.staff.id = :staffId")
    void revokeAllByStaffId(@Param("staffId") Long staffId);
}
