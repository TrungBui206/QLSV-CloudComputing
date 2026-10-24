
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.dto.request.LoginRequest;
import hd.quanlysinhvien_hibernatedao.dto.request.RegisterRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AuthResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.UserResponse;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;
import hd.quanlysinhvien_hibernatedao.security.jwt.JwtTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2026, 10, 1, 8, 0);

    @Mock
    private IUserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userDao,
                passwordEncoder,
                jwtTokenService
        );
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

    private User createUser(String username, boolean enabled) {
        User user = new User();

        user.setId("user-1");
        user.setUsername(username);
        user.setEmail(username + "@example.com");
        user.setPasswordHash("encoded-password");
        user.setRole(Role.STUDENT);
        user.setEnabled(enabled);
        user.setCreatedAt(CREATED_AT);

        return user;
    }

    private void allowRegistration(
            String username,
            String email
    ) {
        when(userDao.findByUsername(username))
                .thenReturn(Optional.empty());

        when(userDao.findByEmail(email))
                .thenReturn(Optional.empty());
    }

    @Test
    void register_success() {
        RegisterRequest request = registerRequest();

        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userDao.create(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    user.setId("user-1");
                    user.setCreatedAt(CREATED_AT);

                    return user;
                });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("user-1", response.id());
        assertEquals("student01", response.username());
        assertEquals(
                "student01@example.com",
                response.email()
        );
        assertEquals(Role.STUDENT, response.role());
        assertTrue(response.enabled());
        assertEquals(CREATED_AT, response.createdAt());

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userDao).create(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("student01", savedUser.getUsername());
        assertEquals(
                "student01@example.com",
                savedUser.getEmail()
        );
        assertEquals(
                "encoded-password",
                savedUser.getPasswordHash()
        );
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertTrue(savedUser.isEnabled());

        verify(passwordEncoder).encode("Password123");
        verify(userDao).flush();
        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void register_normalizesUsernameAndEmail() {
        RegisterRequest request = new RegisterRequest(
                "  Student01  ",
                "  STUDENT01@EXAMPLE.COM  ",
                "Password123"
        );

        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userDao.create(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    user.setId("user-1");
                    user.setCreatedAt(CREATED_AT);

                    return user;
                });

        UserResponse response = authService.register(request);

        assertEquals("student01", response.username());
        assertEquals(
                "student01@example.com",
                response.email()
        );

        verify(userDao).findByUsername("student01");
        verify(userDao).findByEmail(
                "student01@example.com"
        );

        verify(passwordEncoder).encode("Password123");
        verify(userDao).flush();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "admin",
            "ADMIN",
            " lecturer ",
            "LECTURER"
    })
    void register_reservedUsername_400(String username) {
        RegisterRequest request = new RegisterRequest(
                username,
                "test@example.com",
                "Password123"
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tên đăng nhập này được dành riêng",
                exception.getReason()
        );

        verifyNoInteractions(
                userDao,
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void register_passwordTooLongForBCrypt_400() {
        String password = "á".repeat(37);

        RegisterRequest request = new RegisterRequest(
                "student01",
                "student01@example.com",
                password
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.BAD_REQUEST.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Mật khẩu quá dài đối với BCrypt",
                exception.getReason()
        );

        verifyNoInteractions(
                userDao,
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void register_passwordExactly72Bytes_success() {
        String password = "A".repeat(72);

        RegisterRequest request = new RegisterRequest(
                "student01",
                "student01@example.com",
                password
        );

        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode(password))
                .thenReturn("encoded-password");

        when(userDao.create(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    user.setId("user-1");
                    user.setCreatedAt(CREATED_AT);

                    return user;
                });

        UserResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("student01", response.username());

        verify(passwordEncoder).encode(password);
        verify(userDao).create(any(User.class));
        verify(userDao).flush();
    }

    @Test
    void register_duplicateUsername_409() {
        RegisterRequest request = registerRequest();

        User existingUser = createUser(
                "student01",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(existingUser));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tên đăng nhập đã tồn tại",
                exception.getReason()
        );

        verify(userDao).findByUsername("student01");

        verify(userDao, never()).findByEmail(any());
        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).flush();

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void register_duplicateEmail_409() {
        RegisterRequest request = registerRequest();

        User existingUser = createUser(
                "existing",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.empty());

        when(userDao.findByEmail("student01@example.com"))
                .thenReturn(Optional.of(existingUser));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(request)
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Email đã được sử dụng",
                exception.getReason()
        );

        verify(userDao).findByUsername("student01");
        verify(userDao).findByEmail(
                "student01@example.com"
        );

        verify(userDao, never()).create(any(User.class));
        verify(userDao, never()).flush();

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void register_databaseErrorOnCreate_409() {
        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userDao.create(any(User.class)))
                .thenThrow(
                        new DataIntegrityViolationException(
                                "Duplicate entry"
                        )
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(registerRequest())
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tên đăng nhập hoặc email đã tồn tại",
                exception.getReason()
        );

        assertNotNull(exception.getCause());

        verify(userDao).create(any(User.class));
        verify(userDao, never()).flush();
    }

    @Test
    void register_databaseErrorOnFlush_409() {
        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("encoded-password");

        when(userDao.create(any(User.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        doThrow(
                new DataIntegrityViolationException(
                        "Unique constraint violation"
                )
        ).when(userDao).flush();

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.register(registerRequest())
                );

        assertEquals(
                HttpStatus.CONFLICT.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tên đăng nhập hoặc email đã tồn tại",
                exception.getReason()
        );

        verify(userDao).create(any(User.class));
        verify(userDao).flush();
    }

    @Test
    void register_passwordIsEncodedBeforeSaving() {
        allowRegistration(
                "student01",
                "student01@example.com"
        );

        when(passwordEncoder.encode("Password123"))
                .thenReturn("bcrypt-hash");

        when(userDao.create(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    user.setId("user-1");
                    user.setCreatedAt(CREATED_AT);

                    return user;
                });

        authService.register(registerRequest());

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userDao).create(captor.capture());

        User savedUser = captor.getValue();

        assertEquals(
                "bcrypt-hash",
                savedUser.getPasswordHash()
        );

        assertFalse(
                "Password123".equals(
                        savedUser.getPasswordHash()
                )
        );

        verify(passwordEncoder).encode("Password123");
    }

    @Test
    void login_success() {
        LoginRequest request = loginRequest();

        User user = createUser(
                "student01",
                true
        );

        UserResponse userResponse = UserResponse.from(user);

        AuthResponse expected = new AuthResponse(
                "jwt-access-token",
                "Bearer",
                3600L,
                userResponse
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtTokenService.generateToken(user))
                .thenReturn(expected);

        AuthResponse actual = authService.login(request);

        assertNotNull(actual);
        assertSame(expected, actual);
        assertEquals(
                "jwt-access-token",
                actual.accessToken()
        );
        assertEquals("Bearer", actual.tokenType());
        assertEquals(3600L, actual.expiresIn());
        assertEquals("student01", actual.user().username());

        verify(userDao).findByUsername("student01");

        verify(passwordEncoder).matches(
                "Password123",
                "encoded-password"
        );

        verify(jwtTokenService).generateToken(user);
    }

    @Test
    void login_normalizesUsername() {
        User user = createUser(
                "student01",
                true
        );

        LoginRequest request = new LoginRequest(
                "  STUDENT01  ",
                "Password123"
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "encoded-password"
        )).thenReturn(true);

        when(jwtTokenService.generateToken(user))
                .thenReturn(
                        new AuthResponse(
                                "jwt-token",
                                "Bearer",
                                3600L,
                                UserResponse.from(user)
                        )
                );

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.accessToken());

        verify(userDao).findByUsername("student01");
        verify(jwtTokenService).generateToken(user);
    }

    @Test
    void login_userNotFound_401() {
        when(userDao.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest(
                "unknown",
                "Password123"
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tài khoản hoặc mật khẩu không đúng",
                exception.getReason()
        );

        verify(userDao).findByUsername("unknown");

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void login_wrongPassword_401() {
        User user = createUser(
                "student01",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "WrongPassword",
                "encoded-password"
        )).thenReturn(false);

        LoginRequest request = new LoginRequest(
                "student01",
                "WrongPassword"
        );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(request)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tài khoản hoặc mật khẩu không đúng",
                exception.getReason()
        );

        verify(passwordEncoder).matches(
                "WrongPassword",
                "encoded-password"
        );

        verifyNoInteractions(jwtTokenService);
    }

    @Test
    void login_disabledUser_401() {
        User user = createUser(
                "student01",
                false
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.login(loginRequest())
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tài khoản hoặc mật khẩu không đúng",
                exception.getReason()
        );

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void login_passwordIsNotTrimmed() {
        User user = createUser(
                "student01",
                true
        );

        LoginRequest request = new LoginRequest(
                "student01",
                " Password123 "
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                " Password123 ",
                "encoded-password"
        )).thenReturn(true);

        when(jwtTokenService.generateToken(user))
                .thenReturn(
                        new AuthResponse(
                                "jwt-token",
                                "Bearer",
                                3600L,
                                UserResponse.from(user)
                        )
                );

        AuthResponse response = authService.login(request);

        assertEquals("jwt-token", response.accessToken());

        verify(passwordEncoder).matches(
                " Password123 ",
                "encoded-password"
        );
    }

    @Test
    void login_doesNotGenerateTokenWhenPasswordIsWrong() {
        User user = createUser(
                "student01",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "Password123",
                "encoded-password"
        )).thenReturn(false);

        assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest())
        );

        verify(jwtTokenService, never())
                .generateToken(any(User.class));
    }

    @Test
    void getCurrentUser_success() {
        User user = createUser(
                "student01",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                authService.getCurrentUser("student01");

        assertNotNull(response);
        assertEquals("user-1", response.id());
        assertEquals("student01", response.username());
        assertEquals(
                "student01@example.com",
                response.email()
        );
        assertEquals(Role.STUDENT, response.role());
        assertTrue(response.enabled());
        assertEquals(CREATED_AT, response.createdAt());

        verify(userDao).findByUsername("student01");

        verifyNoInteractions(
                passwordEncoder,
                jwtTokenService
        );
    }

    @Test
    void getCurrentUser_adminSuccess() {
        User user = createUser(
                "admin01",
                true
        );

        user.setRole(Role.ADMIN);

        when(userDao.findByUsername("admin01"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                authService.getCurrentUser("admin01");

        assertEquals("admin01", response.username());
        assertEquals(Role.ADMIN, response.role());
        assertTrue(response.enabled());

        verify(userDao).findByUsername("admin01");
    }

    @Test
    void getCurrentUser_notFound_401() {
        when(userDao.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.getCurrentUser("unknown")
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tài khoản không tồn tại",
                exception.getReason()
        );

        verify(userDao).findByUsername("unknown");
    }

    @Test
    void getCurrentUser_disabled_403() {
        User user = createUser(
                "student01",
                false
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> authService.getCurrentUser("student01")
                );

        assertEquals(
                HttpStatus.FORBIDDEN.value(),
                exception.getStatusCode().value()
        );

        assertEquals(
                "Tài khoản đã bị vô hiệu hóa",
                exception.getReason()
        );

        verify(userDao).findByUsername("student01");
    }

    @Test
    void getCurrentUser_usesProvidedUsername() {
        User user = createUser(
                "student01",
                true
        );

        when(userDao.findByUsername("student01"))
                .thenReturn(Optional.of(user));

        UserResponse response =
                authService.getCurrentUser("student01");

        assertEquals("student01", response.username());

        verify(userDao).findByUsername("student01");
        verifyNoInteractions(jwtTokenService);
    }
}
