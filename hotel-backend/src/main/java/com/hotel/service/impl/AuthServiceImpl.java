package com.hotel.service.impl;

import com.hotel.dto.request.LoginRequest;
import com.hotel.dto.request.RefreshTokenRequest;
import com.hotel.dto.response.AuthResponse;
import com.hotel.dto.response.StaffSummary;
import com.hotel.entity.AuditLog;
import com.hotel.entity.RefreshToken;
import com.hotel.entity.Staff;
import com.hotel.repository.AuditLogRepository;
import com.hotel.repository.StaffRepository;
import com.hotel.security.JwtTokenProvider;
import com.hotel.security.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final AuthenticationManager authManager;
    private final JwtTokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final StaffRepository staffRepository;
    private final AuditLogRepository auditLogRepository;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress) {
        Authentication auth = authManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        Staff staff = (Staff) auth.getPrincipal();
        staff.setLastLogin(LocalDateTime.now());
        staffRepository.save(staff);

        String accessToken  = tokenProvider.generateAccessToken(staff);
        RefreshToken refresh = refreshTokenService.createRefreshToken(staff);

        auditLogRepository.save(AuditLog.builder()
            .entityType("STAFF")
            .entityId(staff.getId())
            .action(AuditLog.AuditAction.LOGIN)
            .changedBy(staff.getEmail())
            .ipAddress(ipAddress)
            .build());

        log.info("Staff '{}' logged in from {}", staff.getEmail(), ipAddress);
        return new AuthResponse(accessToken, refresh.getToken(), accessTokenExpiry / 1000,
            toSummary(staff));
    }

    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken newToken = refreshTokenService.rotateRefreshToken(request.refreshToken());
        Staff staff = newToken.getStaff();
        String accessToken = tokenProvider.generateAccessToken(staff);
        return new AuthResponse(accessToken, newToken.getToken(), accessTokenExpiry / 1000,
            toSummary(staff));
    }

    @Transactional
    public void logout(String refreshToken, String currentUserEmail) {
        refreshTokenService.revokeToken(refreshToken);
        staffRepository.findByEmailAndDeletedAtIsNull(currentUserEmail).ifPresent(staff ->
            auditLogRepository.save(AuditLog.builder()
                .entityType("STAFF")
                .entityId(staff.getId())
                .action(AuditLog.AuditAction.LOGOUT)
                .changedBy(currentUserEmail)
                .build())
        );
    }

    private StaffSummary toSummary(Staff s) {
        return new StaffSummary(s.getId(), s.getFirstName(), s.getLastName(),
            s.getEmail(), s.getRole().getName(), s.getProfileImage());
    }
}
