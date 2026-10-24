
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserDaoImplTest {

    @Autowired
    private UserDaoImpl userDao;

    @Autowired
    private EntityManager entityManager;

    private String saveUser(
            String username,
            String email,
            Role role,
            boolean enabled
    ) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash("test-hashed-password");
        user.setRole(role);
        user.setEnabled(enabled);

        User saved = userDao.create(user);

        entityManager.flush();

        String id = saved.getId();

        entityManager.clear();

        return id;
    }

    @Test
    void create_success() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        assertNotNull(id);

        User found = userDao.get(id);

        assertNotNull(found);
        assertEquals("student01", found.getUsername());
        assertEquals("student01@example.com", found.getEmail());
        assertEquals("test-hashed-password", found.getPasswordHash());
        assertEquals(Role.STUDENT, found.getRole());
        assertTrue(found.isEnabled());
    }

    @Test
    void create_autoGenerateUUID() {
        String firstId = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        String secondId = saveUser(
                "student02",
                "student02@example.com",
                Role.STUDENT,
                true
        );

        assertNotNull(firstId);
        assertNotNull(secondId);

        assertNotEquals(firstId, secondId);

        assertEquals(36, firstId.length());
        assertEquals(36, secondId.length());

        assertDoesNotThrow(() -> UUID.fromString(firstId));
        assertDoesNotThrow(() -> UUID.fromString(secondId));

        User first = userDao.get(firstId);
        User second = userDao.get(secondId);

        assertNotNull(first.getCreatedAt());
        assertNotNull(second.getCreatedAt());
    }

    @Test
    void findByUsername_success() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        Optional<User> result =
                userDao.findByUsername("student01");

        assertTrue(result.isPresent());
        assertEquals("student01", result.get().getUsername());
        assertEquals(
                "student01@example.com",
                result.get().getEmail()
        );
    }

    @Test
    void findByUsername_notFound() {
        Optional<User> result =
                userDao.findByUsername("unknown_user");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByEmail_success() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        Optional<User> result =
                userDao.findByEmail("student01@example.com");

        assertTrue(result.isPresent());
        assertEquals(
                "student01",
                result.get().getUsername()
        );
    }

    @Test
    void findByEmail_notFound() {
        Optional<User> result =
                userDao.findByEmail("unknown@example.com");

        assertTrue(result.isEmpty());
    }

    @Test
    void findByUsername_multipleUsers() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "lecturer01",
                "lecturer01@example.com",
                Role.LECTURER,
                true
        );

        Optional<User> result =
                userDao.findByUsername("lecturer01");

        assertTrue(result.isPresent());
        assertEquals("lecturer01", result.get().getUsername());
        assertEquals(Role.LECTURER, result.get().getRole());
    }

    @Test
    void findByEmail_multipleUsers() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "admin01",
                "admin01@example.com",
                Role.ADMIN,
                true
        );

        Optional<User> result =
                userDao.findByEmail("admin01@example.com");

        assertTrue(result.isPresent());
        assertEquals("admin01", result.get().getUsername());
        assertEquals(Role.ADMIN, result.get().getRole());
    }

    @Test
    void get_success() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        User result = userDao.get(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("student01", result.getUsername());
    }

    @Test
    void get_notFound() {
        User result = userDao.get(
                "00000000-0000-0000-0000-000000000000"
        );

        assertNull(result);
    }

    @Test
    void findById_success() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        Optional<User> result = userDao.findById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void update_success() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        User existing = userDao.get(id);

        assertNotNull(existing);

        existing.setUsername("student_updated");
        existing.setEmail("updated@example.com");

        userDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        User updated = userDao.get(id);

        assertNotNull(updated);
        assertEquals(
                "student_updated",
                updated.getUsername()
        );
        assertEquals(
                "updated@example.com",
                updated.getEmail()
        );

        assertTrue(
                userDao.findByUsername("student_updated").isPresent()
        );
    }

    @Test
    void update_roleAndEnabled() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        User existing = userDao.get(id);

        assertNotNull(existing);

        existing.setRole(Role.LECTURER);
        existing.setEnabled(false);

        userDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        User updated = userDao.get(id);

        assertNotNull(updated);
        assertEquals(Role.LECTURER, updated.getRole());
        assertFalse(updated.isEnabled());
    }

    @Test
    void delete_success() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        assertNotNull(userDao.get(id));

        userDao.deleteByPk(id);

        entityManager.flush();
        entityManager.clear();

        assertNull(userDao.get(id));

        assertTrue(
                userDao.findByUsername("student01").isEmpty()
        );

        assertTrue(
                userDao.findByEmail("student01@example.com").isEmpty()
        );
    }

    @Test
    void getAll_success() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "lecturer01",
                "lecturer01@example.com",
                Role.LECTURER,
                true
        );

        List<User> result = userDao.getAll();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(user ->
                                "student01".equals(user.getUsername())
                        )
        );

        assertTrue(
                result.stream()
                        .anyMatch(user ->
                                "lecturer01".equals(user.getUsername())
                        )
        );
    }

    @Test
    void getAll_empty() {
        List<User> result = userDao.getAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void exists_true() {
        String id = saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        assertTrue(userDao.exists(id));
    }

    @Test
    void exists_false() {
        assertFalse(
                userDao.exists(
                        "00000000-0000-0000-0000-000000000000"
                )
        );
    }

    @Test
    void getByField_success() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        User result = userDao.getByField(
                "username",
                "student01"
        );

        assertNotNull(result);
        assertEquals("student01", result.getUsername());
    }

    @Test
    void findByField_role() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "student02",
                "student02@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "lecturer01",
                "lecturer01@example.com",
                Role.LECTURER,
                true
        );

        List<User> result = userDao.findByField(
                "role",
                Role.STUDENT
        );

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(user ->
                                user.getRole() == Role.STUDENT
                        )
        );
    }

    @Test
    void count_success() {
        saveUser(
                "student01",
                "student01@example.com",
                Role.STUDENT,
                true
        );

        saveUser(
                "lecturer01",
                "lecturer01@example.com",
                Role.LECTURER,
                true
        );

        assertEquals(2L, userDao.count());
    }
}
