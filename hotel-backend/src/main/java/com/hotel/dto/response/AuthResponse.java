package com.hotel.dto.response;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    StaffSummary user
) {
    public AuthResponse(String accessToken, String refreshToken, long expiresIn, StaffSummary user) {
        this(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
