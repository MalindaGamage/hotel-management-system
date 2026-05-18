package com.hotel.security;

import com.hotel.exception.InvalidTokenException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.private-key-path}")
    private String privateKeyPath;

    @Value("${app.jwt.public-key-path}")
    private String publicKeyPath;

    @Value("${app.jwt.access-token-expiry}")
    private long accessTokenExpiry;

    private PrivateKey privateKey;
    private PublicKey  publicKey;

    @PostConstruct
    public void init() {
        try {
            privateKey = loadPrivateKey(privateKeyPath);
            publicKey  = loadPublicKey(publicKeyPath);
        } catch (Exception e) {
            log.error("Failed to load RSA keys — ensure private.pem and public.pem are in src/main/resources/keys/", e);
            throw new IllegalStateException("JWT key initialization failed", e);
        }
    }

    public String generateAccessToken(UserDetails userDetails) {
        List<String> roles = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        return Jwts.builder()
            .subject(userDetails.getUsername())
            .claim("roles", roles)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + accessTokenExpiry))
            .signWith(privateKey, Jwts.SIG.RS256)
            .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(publicKey).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT token expired");
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
        }
        return false;
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return (List<String>) parseClaims(token).get("roles");
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser().verifyWith(publicKey).build()
                .parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // Allow reading claims from expired tokens when explicitly needed
        } catch (JwtException e) {
            throw new InvalidTokenException("Cannot parse JWT: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // PEM key loading helpers
    // -------------------------------------------------------------------------
    private PrivateKey loadPrivateKey(String resourcePath) throws Exception {
        String pem = loadResource(resourcePath)
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
    }

    private PublicKey loadPublicKey(String resourcePath) throws Exception {
        String pem = loadResource(resourcePath)
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s+", "");
        byte[] der = Base64.getDecoder().decode(pem);
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
    }

    private String loadResource(String path) throws Exception {
        String cleanPath = path.replace("classpath:", "");
        try (var stream = getClass().getResourceAsStream("/" + cleanPath)) {
            if (stream == null) throw new IllegalStateException("Resource not found: " + path);
            return new String(stream.readAllBytes());
        }
    }
}
