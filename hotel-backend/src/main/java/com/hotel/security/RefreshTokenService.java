package com.hotel.security;

import com.hotel.entity.RefreshToken;
import com.hotel.entity.Staff;
import com.hotel.exception.InvalidTokenException;
import com.hotel.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-token-expiry}")
    private long refreshTokenExpiryMs;

    @Transactional
    public RefreshToken createRefreshToken(Staff staff) {
        // Each login session gets a new family — groups all rotated tokens together
        String family = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
            .staff(staff)
            .token(UUID.randomUUID().toString())
            .family(family)
            .isRevoked(false)
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
            .build();

        return refreshTokenRepository.save(token);
    }

    @Transactional
    public RefreshToken rotateRefreshToken(String oldTokenValue) {
        RefreshToken old = refreshTokenRepository.findByToken(oldTokenValue)
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (old.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        // Detect token reuse — if token was already revoked, someone stole it:
        // invalidate the entire family to force re-login for all devices in session
        if (old.isRevoked()) {
            log.warn("Refresh token reuse detected for family '{}' — revoking entire family", old.getFamily());
            refreshTokenRepository.revokeAllByFamily(old.getFamily());
            throw new InvalidTokenException("Refresh token reuse detected — please log in again");
        }

        // Revoke old token and issue a new one in the same family
        old.setRevoked(true);
        refreshTokenRepository.save(old);

        RefreshToken newToken = RefreshToken.builder()
            .staff(old.getStaff())
            .token(UUID.randomUUID().toString())
            .family(old.getFamily())
            .isRevoked(false)
            .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiryMs / 1000))
            .build();

        return refreshTokenRepository.save(newToken);
    }

    @Transactional
    public void revokeAllUserTokens(Long staffId) {
        refreshTokenRepository.revokeAllByStaffId(staffId);
    }

    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.findByToken(tokenValue).ifPresent(t -> {
            t.setRevoked(true);
            refreshTokenRepository.save(t);
        });
    }
}
