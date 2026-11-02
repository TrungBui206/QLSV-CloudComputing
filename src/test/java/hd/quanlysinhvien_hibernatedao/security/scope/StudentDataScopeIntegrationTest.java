
package hd.quanlysinhvien_hibernatedao.security.scope;

import hd.quanlysinhvien_hibernatedao.dao.IUserDao;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.entity.User;
import hd.quanlysinhvien_hibernatedao.security.authorization.Role;
import hd.quanlysinhvien_hibernatedao.service.StudentService;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Kiem thu toan chuoi: SecurityContext -> StudentDataScope -> StudentService -> DAO (H2)
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentDataScopeIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private IUserDao userDao;

    @Autowired
    private EntityManager entityManager;

    private Long ownId;
    private Long otherId;

    @BeforeEach
    void setUp() {
        Department department = new Department();
        department.setCode("SCOPE");
        department.setName("Scope Department");
        entityManager.persist(department);

        ownId = persistStudent("Own Student", "own.student@test.local", department);
        otherId = persistStudent("Other Student", "other.student@test.local", department);

        // Email tai khoan khac hoa/thuong so voi email sinh vien
        User user = new User();
        user.setUsername("scope_student");
        user.setEmail("OWN.Student@test.local");
        user.setPasswordHash("not-used");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);
        userDao.create(user);

        entityManager.flush();
        entityManager.clear();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Long persistStudent(String name, String email, Department department) {
        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setDob(LocalDate.of(2004, 1, 15));
        student.setDepartment(department);
        entityManager.persist(student);
        return student.getId();
    }

    private void loginAs(String username, Role role) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        username,
                        "n/a",
                        List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))
                )
        );
    }

    @Test
    void student_seesOnlyOwnProfile() {
        loginAs("scope_student", Role.STUDENT);

        List<Long> all = studentService.findAll()
                .stream()
                .map(StudentResponse::id)
                .toList();
        assertEquals(List.of(ownId), all);

        PageResponse<StudentResponse> page =
                studentService.search("", null, 0, 10, "id", "asc");
        assertEquals(1L, page.totalElements());
        assertEquals(ownId, page.content().get(0).id());

        assertEquals(ownId, studentService.findById(ownId).id());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.findById(otherId)
        );
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void lecturer_seesAllProfiles() {
        loginAs("any_lecturer", Role.LECTURER);

        List<Long> all = studentService.findAll()
                .stream()
                .map(StudentResponse::id)
                .toList();

        assertTrue(all.containsAll(List.of(ownId, otherId)));
        assertEquals(otherId, studentService.findById(otherId).id());
    }
}
