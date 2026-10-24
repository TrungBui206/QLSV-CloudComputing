
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.BadRequestException;
import hd.quanlysinhvien_hibernatedao.common.exception.DuplicateResourceException;
import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.service.AuthService;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.jwt.issuer=quanlysinhvien-hibernatedao"
        }
)
@ActiveProfiles("test")
class AuthRestImplTest {

    private static final String URL = "/api/auth";

    private static final String REGISTER_REQUEST = """
            {
                "username": "student01",
                "email": "student01@example.com",
                "password": "Password123"
            }
            """;

    private static final String LOGIN_REQUEST = """
            {
                "username": "student01",
                "password": "Password123"
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private AuthService authService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private RegisterRequest registerRequest() {
        return new RegisterRequest(
                "student01",
                "student01@example.com",
                "Password123"
        );
    }

    private LoginRequest loginRequest() {
        return new LoginRequest(
                "student01",
                "Password123"
        );
    }

    private AuthResponse authResponse() {
        return new AuthResponse(
                "header.payload.signature",
                "Bearer",
                3600L,
                null
        );
    }

    private String token(String username, String role) {
        Instant now = Instant.now();

        return createToken(
                username,
                role,
                now,
                now.plusSeconds(3600)
        );
    }

    private String createToken(
            String username,
            String role,
            Instant issuedAt,
            Instant expiresAt
    ) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("quanlysinhvien-hibernatedao")
                .subject(username)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", List.of(role))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return "Bearer " + jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    @Test
    void register_validRequest_created() throws Exception {
        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REGISTER_REQUEST)
                )
                .andExpect(status().isCreated());

        verify(authService).register(registerRequest());
    }

    @Test
    void register_withAuthentication_created() throws Exception {
        mockMvc.perform(
                        post(URL + "/register")
                                .header(
                                        "Authorization",
                                        token("admin01", "ADMIN")
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REGISTER_REQUEST)
                )
                .andExpect(status().isCreated());

        verify(authService).register(registerRequest());
    }

    @Test
    void register_duplicateUsername_409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(
                        new DuplicateResourceException(
                                "Tên đăng nhập đã tồn tại"
                        )
                );

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REGISTER_REQUEST)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Tên đăng nhập đã tồn tại"));
    }

    @Test
    void register_duplicateEmail_409() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(
                        new DuplicateResourceException(
                                "Email đã tồn tại"
                        )
                );

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REGISTER_REQUEST)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Email đã tồn tại"));
    }

    @Test
    void register_blankUsername_400() throws Exception {
        String body = """
                {
                    "username": "",
                    "email": "student01@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("username:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_usernameTooShort_400() throws Exception {
        String body = """
                {
                    "username": "ab",
                    "email": "student01@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("username:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_usernameInvalidCharacters_400() throws Exception {
        String body = """
                {
                    "username": "student@01",
                    "email": "student01@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("username:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_invalidEmail_400() throws Exception {
        String body = """
                {
                    "username": "student01",
                    "email": "invalid-email",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("email:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_blankEmail_400() throws Exception {
        String body = """
                {
                    "username": "student01",
                    "email": "",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).register(any());
    }

    @Test
    void register_passwordTooShort_400() throws Exception {
        String body = """
                {
                    "username": "student01",
                    "email": "student01@example.com",
                    "password": "1234567"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("password:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_passwordTooLong_400() throws Exception {
        String password = "A".repeat(73);

        String body = """
                {
                    "username": "student01",
                    "email": "student01@example.com",
                    "password": "%s"
                }
                """.formatted(password);

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("password:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_missingUsername_400() throws Exception {
        String body = """
                {
                    "email": "student01@example.com",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("username:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_missingPassword_400() throws Exception {
        String body = """
                {
                    "username": "student01",
                    "email": "student01@example.com"
                }
                """;

        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("password:")));

        verify(authService, never()).register(any());
    }

    @Test
    void register_invalidJson_400() throws Exception {
        mockMvc.perform(
                        post(URL + "/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid-json}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Nội dung JSON không hợp lệ"));

        verify(authService, never()).register(any());
    }

    @Test
    void login_validRequest_success() throws Exception {
        when(authService.login(loginRequest()))
                .thenReturn(authResponse());

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(LOGIN_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken")
                        .value("header.payload.signature"))
                .andExpect(jsonPath("$.tokenType")
                        .value("Bearer"))
                .andExpect(jsonPath("$.expiresIn")
                        .value(3600));

        verify(authService).login(loginRequest());
    }

    @Test
    void login_blankUsername_400() throws Exception {
        String body = """
                {
                    "username": "",
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("username:")));

        verify(authService, never()).login(any());
    }

    @Test
    void login_blankPassword_400() throws Exception {
        String body = """
                {
                    "username": "student01",
                    "password": ""
                }
                """;

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("password:")));

        verify(authService, never()).login(any());
    }

    @Test
    void login_missingUsername_400() throws Exception {
        String body = """
                {
                    "password": "Password123"
                }
                """;

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).login(any());
    }

    @Test
    void login_missingPassword_400() throws Exception {
        String body = """
                {
                    "username": "student01"
                }
                """;

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(authService, never()).login(any());
    }

    @Test
    void login_invalidJson_400() throws Exception {
        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid-json}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Nội dung JSON không hợp lệ"));

        verify(authService, never()).login(any());
    }


    @Test
    void login_invalidCredentials_401() throws Exception {
        when(authService.login(loginRequest()))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Tài khoản hoặc mật khẩu không đúng"
                        )
                );

        mockMvc.perform(
                        post(URL + "/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(LOGIN_REQUEST)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message")
                        .value("Tài khoản hoặc mật khẩu không đúng"));

        verify(authService).login(loginRequest());
    }


    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "LECTURER", "STUDENT"})
    void getCurrentUser_authenticated_success(String role) throws Exception {
        mockMvc.perform(
                        get(URL + "/me")
                                .header(
                                        "Authorization",
                                        token("student01", role)
                                )
                )
                .andExpect(status().isOk());

        verify(authService).getCurrentUser("student01");
    }

    @Test
    void getCurrentUser_differentUsername_success() throws Exception {
        mockMvc.perform(
                        get(URL + "/me")
                                .header(
                                        "Authorization",
                                        token("lecturer01", "LECTURER")
                                )
                )
                .andExpect(status().isOk());

        verify(authService).getCurrentUser("lecturer01");
    }

    @Test
    void getCurrentUser_unauthenticated_401() throws Exception {
        mockMvc.perform(get(URL + "/me"))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).getCurrentUser(any());
    }

    @Test
    void getCurrentUser_invalidToken_401() throws Exception {
        mockMvc.perform(
                        get(URL + "/me")
                                .header(
                                        "Authorization",
                                        "Bearer invalid-token"
                                )
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never()).getCurrentUser(any());
    }

    @Test
    void getCurrentUser_expiredToken_401() throws Exception {
        Instant past = Instant.now().minusSeconds(7200);

        String expiredToken = createToken(
                "student01",
                "STUDENT",
                past,
                past.plusSeconds(3600)
        );

        mockMvc.perform(
                        get(URL + "/me")
                                .header("Authorization", expiredToken)
                )
                .andExpect(status().isUnauthorized());

        verify(authService, never()).getCurrentUser(any());
    }
}
