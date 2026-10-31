
package hd.quanlysinhvien_hibernatedao.config;

import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;
import hd.quanlysinhvien_hibernatedao.service.AuthService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.jwt.issuer=quanlysinhvien-hibernatedao",
                "app.jwt.expiration-minutes=60"
        }
)
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final String ISSUER =
            "quanlysinhvien-hibernatedao";

    private static final String VALID_SECRET =
            "0123456789abcdef0123456789abcdef";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockitoBean
    private AuthService authService;

    private SecurityConfig securityConfig;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig();

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private UserResponse userResponse() {
        return new UserResponse(
                "user-1",
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true,
                LocalDateTime.of(2026, 10, 1, 8, 0)
        );
    }

    private String createToken(
            String username,
            String role,
            String issuer,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", List.of(role))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private String validToken(String role) {
        Instant now = Instant.now();

        return createToken(
                "student01",
                role,
                ISSUER,
                now,
                now.plusSeconds(3600)
        );
    }

    @Test
    void passwordEncoder_notNull() {
        assertNotNull(passwordEncoder);
    }

    @Test
    void passwordEncoder_encodesPassword() {
        String password = "Password123";

        String encoded = passwordEncoder.encode(password);

        assertNotNull(encoded);
        assertFalse(encoded.isBlank());
        assertFalse(password.equals(encoded));

        assertTrue(
                passwordEncoder.matches(password, encoded)
        );
    }

    @Test
    void passwordEncoder_wrongPasswordRejected() {
        String encoded =
                passwordEncoder.encode("Password123");

        assertFalse(
                passwordEncoder.matches(
                        "WrongPassword",
                        encoded
                )
        );
    }

    @Test
    void jwtSecretKey_validBase64_success() {
        String base64 = Base64.getEncoder()
                .encodeToString(
                        VALID_SECRET.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        SecretKey secretKey =
                securityConfig.jwtSecretKey(base64);

        assertNotNull(secretKey);
        assertEquals("HmacSHA256", secretKey.getAlgorithm());
        assertEquals(32, secretKey.getEncoded().length);
    }

    @Test
    void jwtSecretKey_invalidBase64_rejected() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> securityConfig.jwtSecretKey(
                        "invalid@@secret"
                )
        );

        assertEquals(
                "JWT secret phải là Base64 hợp lệ",
                exception.getMessage()
        );
    }

    @Test
    void jwtSecretKey_tooShort_rejected() {
        String shortSecret = Base64.getEncoder()
                .encodeToString(
                        "short-secret"
                                .getBytes(StandardCharsets.UTF_8)
                );

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> securityConfig.jwtSecretKey(
                        shortSecret
                )
        );

        assertEquals(
                "JWT secret phải có ít nhất 32 byte",
                exception.getMessage()
        );
    }

    @Test
    void jwtEncoderAndDecoder_validToken_success() {
        String token = validToken("STUDENT");

        Jwt jwt = jwtDecoder.decode(token);

        assertNotNull(jwt);

        assertEquals("student01", jwt.getSubject());

        assertEquals(
                ISSUER,
                jwt.getClaimAsString("iss")
        );

        assertEquals(
                List.of("STUDENT"),
                jwt.getClaimAsStringList("roles")
        );

        assertEquals(
                "HS256",
                jwt.getHeaders().get("alg").toString()
        );
    }

    @Test
    void jwtDecoder_invalidFormat_rejected() {
        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode("invalid-token")
        );
    }

    @Test
    void jwtDecoder_wrongIssuer_rejected() {
        Instant now = Instant.now();

        String token = createToken(
                "student01",
                "STUDENT",
                "wrong-issuer",
                now,
                now.plusSeconds(3600)
        );

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(token)
        );
    }

    @Test
    void jwtDecoder_expiredToken_rejected() {
        Instant now = Instant.now();

        String token = createToken(
                "student01",
                "STUDENT",
                ISSUER,
                now.minusSeconds(7200),
                now.minusSeconds(3600)
        );

        assertThrows(
                JwtException.class,
                () -> jwtDecoder.decode(token)
        );
    }

    @Test
    void jwtDecoder_wrongSignature_rejected() {
        String token = validToken("STUDENT");

        String anotherSecret =
                "abcdef0123456789abcdef0123456789";

        String base64 = Base64.getEncoder()
                .encodeToString(
                        anotherSecret.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        SecretKey anotherKey =
                securityConfig.jwtSecretKey(base64);

        JwtDecoder anotherDecoder =
                securityConfig.jwtDecoder(
                        anotherKey,
                        ISSUER
                );

        assertThrows(
                JwtException.class,
                () -> anotherDecoder.decode(token)
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "LECTURER",
            "STUDENT"
    })
    void jwtAuthenticationConverter_correctRole(
            String role
    ) {
        String token = validToken(role);

        Jwt jwt = jwtDecoder.decode(token);

        Authentication authentication =
                jwtAuthenticationConverter.convert(jwt);

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());

        assertEquals(
                "student01",
                authentication.getName()
        );

        assertTrue(
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_" + role)
                        )
        );
    }

    @Test
    void register_withoutToken_allowed() throws Exception {
        when(authService.register(
                any(RegisterRequest.class)
        )).thenReturn(userResponse());

        String body = """
                {
                    "username": "student01",
                    "email": "student01@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.username")
                        .value("student01"));

        verify(authService).register(
                any(RegisterRequest.class)
        );
    }

    @Test
    void login_withoutToken_allowed() throws Exception {
        AuthResponse response = new AuthResponse(
                "test-access-token",
                "Bearer",
                3600L,
                userResponse()
        );

        when(authService.login(
                any(LoginRequest.class)
        )).thenReturn(response);

        String body = """
                {
                    "username": "student01",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken")
                        .value("test-access-token"))
                .andExpect(jsonPath("$.data.tokenType")
                        .value("Bearer"));

        verify(authService).login(
                any(LoginRequest.class)
        );
    }

    @Test
    void getCurrentUser_withoutToken_401() throws Exception {
        mockMvc.perform(
                        get("/api/auth/me")
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never())
                .getCurrentUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ADMIN",
            "LECTURER",
            "STUDENT"
    })
    void getCurrentUser_validToken_200(
            String role
    ) throws Exception {
        when(authService.getCurrentUser("student01"))
                .thenReturn(userResponse());

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + validToken(role)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username")
                        .value("student01"));

        verify(authService).getCurrentUser("student01");
    }

    @Test
    void getCurrentUser_invalidToken_401() throws Exception {
        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never())
                .getCurrentUser(any());
    }

    @Test
    void getCurrentUser_expiredToken_401() throws Exception {
        Instant now = Instant.now();

        String token = createToken(
                "student01",
                "STUDENT",
                ISSUER,
                now.minusSeconds(7200),
                now.minusSeconds(3600)
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never())
                .getCurrentUser(any());
    }

    @Test
    void getCurrentUser_wrongIssuer_401() throws Exception {
        Instant now = Instant.now();

        String token = createToken(
                "student01",
                "STUDENT",
                "another-issuer",
                now,
                now.plusSeconds(3600)
        );

        mockMvc.perform(
                        get("/api/auth/me")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never())
                .getCurrentUser(any());
    }

    @Test
    void classes_withoutToken_401() throws Exception {
        mockMvc.perform(
                        get("/api/classes")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void students_withoutToken_401() throws Exception {
        mockMvc.perform(
                        get("/api/students")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void documents_withoutToken_401() throws Exception {
        mockMvc.perform(
                        get("/api/documents")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void upload_studentForbidden_403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.pdf",
                "application/pdf",
                "Test content".getBytes(
                        StandardCharsets.UTF_8
                )
        );

        mockMvc.perform(
                        multipart("/api/documents")
                                .file(file)
                                .param("appId", "APP01")
                                .header(
                                        "Authorization",
                                        "Bearer " + validToken("STUDENT")
                                )
                )
                .andExpect(status().isForbidden());
    }
}
