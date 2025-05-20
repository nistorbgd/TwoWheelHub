package com.twowheelhub.backend.security;

import com.twowheelhub.backend.dto.AuthResponse;
import com.twowheelhub.backend.entity.AppUser;
import com.twowheelhub.backend.entity.Token;
import com.twowheelhub.backend.entity.TokenType;
import com.twowheelhub.backend.repository.TokenRepository;
import com.twowheelhub.backend.service.TokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${refresh.expiration}")
    private long refreshExpiration;

    private final TokenRepository tokenRepository;

    private final TokenService tokenService;

    public String generateToken(AppUser user, TokenType tokenType) {
        long expiration = (tokenType == TokenType.ACCESS) ? jwtExpiration : refreshExpiration;
        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }

    private Key getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserName(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private Date extractExpiration(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUserName(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public AuthResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or Invalid Authorization Token");
        }

        final String refreshToken = authHeader.substring(7);

        Token storedRefreshToken = tokenRepository
                .findByTokenAndTokenType(refreshToken, TokenType.REFRESH)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        if (storedRefreshToken.isExpired() || storedRefreshToken.isRevoked()) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        AppUser user = storedRefreshToken.getUser();
        String newAccessToken = generateToken(user, TokenType.ACCESS);

        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, newAccessToken,TokenType.ACCESS);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
