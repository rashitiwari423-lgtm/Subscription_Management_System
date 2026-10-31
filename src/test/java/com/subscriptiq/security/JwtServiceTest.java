package com.subscriptiq.security;

import com.subscriptiq.user.enums.UserRole;
import com.subscriptiq.user.enums.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L); // 1 hour
    }

    @Test
    @DisplayName("Should generate token and correctly extract claims")
    void testGenerateAndExtractClaims() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(42L)
                .name("Test User")
                .email("test@subscriptiq.com")
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .build();

        String token = jwtService.generateToken(principal);

        assertThat(token).isNotBlank();
        assertThat(jwtService.extractUsername(token)).isEqualTo("test@subscriptiq.com");
        assertThat(jwtService.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtService.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtService.isTokenValid(token, principal)).isTrue();
    }

    @Test
    @DisplayName("Should detect invalid or malformed tokens")
    void testInvalidToken() {
        UserPrincipal principal = UserPrincipal.builder()
                .id(1L)
                .email("test@subscriptiq.com")
                .role(UserRole.CUSTOMER)
                .build();

        assertThat(jwtService.isTokenValid("malformed.jwt.token", principal)).isFalse();
    }

    @Test
    @DisplayName("Should detect expired token")
    void testExpiredToken() {
        // Set expiration to -1000ms (already expired)
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

        String expiredToken = jwtService.generateToken(1L, "user@example.com", "CUSTOMER");

        assertThat(jwtService.isTokenExpired(expiredToken)).isTrue();
    }
}
