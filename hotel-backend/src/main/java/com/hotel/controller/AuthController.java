package com.hotel.controller;

import com.hotel.config.RateLimitConfig;
import com.hotel.dto.request.LoginRequest;
import com.hotel.dto.request.RefreshTokenRequest;
import com.hotel.dto.response.AuthResponse;
import com.hotel.service.impl.AuthServiceImpl;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@Tag(name = "Authentication", description = "Login, token refresh, and logout")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;
    private final RateLimitConfig rateLimitConfig;

    @Operation(summary = "Log in and receive JWT tokens")
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        // Rate limit per IP
        String ip = getClientIp(httpRequest);
        Bucket bucket = rateLimitConfig.resolveBucket(ip);
        if (!bucket.tryConsume(1)) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS,
                "Too many login attempts. Please try again later.");
            pd.setType(URI.create("https://hotel.com/errors/rate-limit"));
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(pd);
        }

        AuthResponse response = authService.login(request, ip);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using a valid refresh token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "Log out and revoke refresh token")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(request.refreshToken(), userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get current authenticated user profile")
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userDetails);
    }

    private String getClientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isEmpty()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
