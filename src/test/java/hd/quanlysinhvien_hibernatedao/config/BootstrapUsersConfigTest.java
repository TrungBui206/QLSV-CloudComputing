
package hd.quanlysinhvien_hibernatedao.config;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.boot.CommandLineRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapUsersConfigTest {

    private static final String ADMIN_PASSWORD =
            "AdminStrong!123";

    private static final String LECTURER_PASSWORD =
            "LecturerStrong!123";

    @Mock
    private IUserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    private BootstrapUsersConfig config;

    @BeforeEach
    void setUp() {
        config = new BootstrapUsersConfig();
    }

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private void runBootstrap(
            String adminPassword,
            String lecturerPassword
    ) throws Exception {

        CommandLineRunner runner = config.bootstrapUsers(
                userDao,
                passwordEncoder,
                adminPassword,
                lecturerPassword
        );

        runner.run();
    }

    private void assertNoUsersCreated() {
        verify(userDao, never()).create(any(User.class));
        verifyNoInteractions(passwordEncoder);
    }

    // ==========================================
    // TEST 1: TAO HAI TAI KHOAN MOI
    // ==========================================

    @Test
    void bootstrapCreatesAdminAndLecturer()
            throws Exception {

        when(userDao.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(userDao.findByUsername("lecturer"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(ADMIN_PASSWORD))
                .thenReturn("hashed-admin");

        when(passwordEncoder.encode(LECTURER_PASSWORD))
                .thenReturn("hashed-lecturer");

        runBootstrap(
                ADMIN_PASSWORD,
                LECTURER_PASSWORD
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userDao, times(2))
                .create(captor.capture());

        List<User> createdUsers =
                captor.getAllValues();

        assertEquals(2, createdUsers.size());

        User admin = createdUsers.get(0);
        User lecturer = createdUsers.get(1);

        // Kiem tra ADMIN
        assertEquals(
                "admin",
                admin.getUsername()
        );

        assertEquals(
                "admin@local.invalid",
                admin.getEmail()
        );

        assertEquals(
                Role.ADMIN,
                admin.getRole()
        );

        assertEquals(
                "hashed-admin",
                admin.getPasswordHash()
        );

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.getField(
                        admin,
                        "enabled"
                )
        );

        // Kiem tra LECTURER
        assertEquals(
                "lecturer",
                lecturer.getUsername()
        );

        assertEquals(
                "lecturer@local.invalid",
                lecturer.getEmail()
        );

        assertEquals(
                Role.LECTURER,
                lecturer.getRole()
        );

        assertEquals(
                "hashed-lecturer",
                lecturer.getPasswordHash()
        );

        assertEquals(
                Boolean.TRUE,
                ReflectionTestUtils.getField(
                        lecturer,
                        "enabled"
                )
        );

        verify(passwordEncoder).encode(
                ADMIN_PASSWORD
        );

        verify(passwordEncoder).encode(
                LECTURER_PASSWORD
        );
    }

    // ==========================================
    // TEST 2: HAI TAI KHOAN DA TON TAI
    // ==========================================

    @Test
    void bootstrapDoesNotCreateExistingUsers()
            throws Exception {

        User existingAdmin = new User();
        User existingLecturer = new User();

        when(userDao.findByUsername("admin"))
                .thenReturn(Optional.of(existingAdmin));

        when(userDao.findByUsername("lecturer"))
                .thenReturn(Optional.of(existingLecturer));

        runBootstrap(
                ADMIN_PASSWORD,
                LECTURER_PASSWORD
        );

        assertNoUsersCreated();

        verify(userDao).findByUsername("admin");
        verify(userDao).findByUsername("lecturer");
    }

    // ==========================================
    // TEST 3: ADMIN DA TON TAI
    // ==========================================

    @Test
    void bootstrapCreatesOnlyLecturerWhenAdminExists()
            throws Exception {

        when(userDao.findByUsername("admin"))
                .thenReturn(Optional.of(new User()));

        when(userDao.findByUsername("lecturer"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(LECTURER_PASSWORD))
                .thenReturn("hashed-lecturer");

        runBootstrap(
                ADMIN_PASSWORD,
                LECTURER_PASSWORD
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userDao, times(1))
                .create(captor.capture());

        assertEquals(
                "lecturer",
                captor.getValue().getUsername()
        );

        assertEquals(
                Role.LECTURER,
                captor.getValue().getRole()
        );

        verify(passwordEncoder, never())
                .encode(ADMIN_PASSWORD);
    }

    // ==========================================
    // TEST 4: LECTURER DA TON TAI
    // ==========================================

    @Test
    void bootstrapCreatesOnlyAdminWhenLecturerExists()
            throws Exception {

        when(userDao.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(userDao.findByUsername("lecturer"))
                .thenReturn(Optional.of(new User()));

        when(passwordEncoder.encode(ADMIN_PASSWORD))
                .thenReturn("hashed-admin");

        runBootstrap(
                ADMIN_PASSWORD,
                LECTURER_PASSWORD
        );

        ArgumentCaptor<User> captor =
                ArgumentCaptor.forClass(User.class);

        verify(userDao, times(1))
                .create(captor.capture());

        assertEquals(
                "admin",
                captor.getValue().getUsername()
        );

        assertEquals(
                Role.ADMIN,
                captor.getValue().getRole()
        );

        verify(passwordEncoder, never())
                .encode(LECTURER_PASSWORD);
    }

    // ==========================================
    // TEST 5: MAT KHAU ADMIN QUA NGAN
    // ==========================================

    @Test
    void bootstrapRejectsShortAdminPassword() {

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> runBootstrap(
                                "12345678901",
                                LECTURER_PASSWORD
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("ít nhất 12 ký tự")
        );

        verifyNoInteractions(userDao, passwordEncoder);
    }

    // ==========================================
    // TEST 6: MAT KHAU LECTURER QUA NGAN
    // ==========================================

    @Test
    void bootstrapRejectsShortLecturerPassword() {

        assertThrows(
                IllegalStateException.class,
                () -> runBootstrap(
                        ADMIN_PASSWORD,
                        "12345678901"
                )
        );

        verifyNoInteractions(userDao, passwordEncoder);
    }

    // ==========================================
    // TEST 7: ADMIN PASSWORD VUOT 72 BYTE
    // ==========================================

    @Test
    void bootstrapRejectsAdminPasswordOver72Bytes() {

        // Moi ky tu e co dau nay chiem 2 byte UTF-8.
        // 37 ky tu = 74 byte.

        String longPassword = "é".repeat(37);

        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () -> runBootstrap(
                                longPassword,
                                LECTURER_PASSWORD
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("72 byte")
        );

        verifyNoInteractions(userDao, passwordEncoder);
    }

    // ==========================================
    // TEST 8: LECTURER PASSWORD VUOT 72 BYTE
    // ==========================================

    @Test
    void bootstrapRejectsLecturerPasswordOver72Bytes() {

        String longPassword = "é".repeat(37);

        assertThrows(
                IllegalStateException.class,
                () -> runBootstrap(
                        ADMIN_PASSWORD,
                        longPassword
                )
        );

        verifyNoInteractions(userDao, passwordEncoder);
    }

    // ==========================================
    // TEST 9: MAT KHAU O GIOI HAN HOP LE
    // ==========================================

    @Test
    void bootstrapAcceptsPasswordAtAllowedBoundaries()
            throws Exception {

        String adminPassword = "A".repeat(12);
        String lecturerPassword = "B".repeat(72);

        when(userDao.findByUsername("admin"))
                .thenReturn(Optional.empty());

        when(userDao.findByUsername("lecturer"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(adminPassword))
                .thenReturn("hash-admin");

        when(passwordEncoder.encode(lecturerPassword))
                .thenReturn("hash-lecturer");

        runBootstrap(
                adminPassword,
                lecturerPassword
        );

        verify(userDao, times(2))
                .create(any(User.class));

        verify(passwordEncoder).encode(
                adminPassword
        );

        verify(passwordEncoder).encode(
                lecturerPassword
        );
    }
}
