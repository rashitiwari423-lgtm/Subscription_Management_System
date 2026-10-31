package com.subscriptiq.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.function.Function;

public interface JwtService {

    String generateToken(UserPrincipal userPrincipal);

    String generateToken(Long userId, String email, String role);

    String extractUsername(String token);

    Long extractUserId(String token);

    String extractRole(String token);

    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isTokenExpired(String token);

    long getExpirationTimeMs();
}
