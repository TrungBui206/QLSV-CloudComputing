
package hd.quanlysinhvien_hibernatedao.service.impl;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.enums.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceImplTest {

    private static final String ISSUER =
            "quanlysinhvien-hibernatedao";

    private static final long EXPIRATION_MINUTES = 60;

    private static final String SECRET =
            "0123456789abcdef0123456789abcdef";

    private JwtTokenServiceImpl jwtTokenService;
    private JwtDecoder jwtDecoder;
    private User user;

    @BeforeEach
    void setUp() {
        SecretKey secretKey = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtEncoder jwtEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );

        jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtTokenService = new JwtTokenServiceImpl(
                jwtEncoder,
                ISSUER,
                EXPIRATION_MINUTES
        );

        user = new User();
        user.setId("user-001");
        user.setUsername("student01");
        user.setEmail("student@example.com");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
    }

    @Test
    void generateToken_success() {
        AuthResponse response = jwtTokenService.generateToken(user);

        assertNotNull(response);
        assertNotNull(response.accessToken());
        assertFalse(response.accessToken().isBlank());

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertNotNull(jwt);
    }

    @Test
    void generateToken_correctSubject() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertEquals("student01", jwt.getSubject());
    }

    @Test
    void generateToken_correctIssuer() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertEquals(
                ISSUER,
                jwt.getClaimAsString("iss")
        );
    }

    @Test
    void generateToken_correctUserId() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertEquals(
                "user-001",
                jwt.getClaimAsString("uid")
        );
    }

    @Test
    void generateToken_correctRole() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        List<String> roles = jwt.getClaimAsStringList("roles");

        assertNotNull(roles);
        assertEquals(List.of("STUDENT"), roles);
    }

    @Test
    void generateToken_adminRole() {
        user.setRole(Role.ADMIN);

        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertEquals(
                List.of("ADMIN"),
                jwt.getClaimAsStringList("roles")
        );
    }

    @Test
    void generateToken_lecturerRole() {
        user.setRole(Role.LECTURER);

        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertEquals(
                List.of("LECTURER"),
                jwt.getClaimAsStringList("roles")
        );
    }

    @Test
    void generateToken_correctExpiration() {
        Instant before = Instant.now();

        AuthResponse response = jwtTokenService.generateToken(user);

        Instant after = Instant.now();

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        Instant issuedAt = jwt.getIssuedAt();
        Instant expiresAt = jwt.getExpiresAt();

        assertNotNull(issuedAt);
        assertNotNull(expiresAt);

        assertFalse(issuedAt.isBefore(before.minusSeconds(1)));
        assertFalse(issuedAt.isAfter(after.plusSeconds(1)));

        assertEquals(
                3600,
                Duration.between(issuedAt, expiresAt).getSeconds()
        );
    }

    @Test
    void generateToken_correctTokenType() {
        AuthResponse response = jwtTokenService.generateToken(user);

        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }

    @Test
    void generateToken_containsUserInformation() {
        AuthResponse response = jwtTokenService.generateToken(user);

        assertNotNull(response.user());
    }

    @Test
    void generateToken_validHS256Signature() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertNotNull(jwt);
        assertEquals("HS256", jwt.getHeaders().get("alg"));
    }

    @Test
    void generateToken_wrongSecretRejected() {
        AuthResponse response = jwtTokenService.generateToken(user);

        SecretKey wrongKey = new SecretKeySpec(
                "abcdef0123456789abcdef0123456789"
                        .getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtDecoder wrongDecoder = NimbusJwtDecoder
                .withSecretKey(wrongKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        assertThrows(
                JwtException.class,
                () -> wrongDecoder.decode(response.accessToken())
        );
    }

    @Test
    void generateToken_containsIssuedAtAndExpiresAt() {
        AuthResponse response = jwtTokenService.generateToken(user);

        Jwt jwt = jwtDecoder.decode(response.accessToken());

        assertNotNull(jwt.getIssuedAt());
        assertNotNull(jwt.getExpiresAt());

        assertTrue(jwt.getExpiresAt().isAfter(jwt.getIssuedAt()));
    }
}
