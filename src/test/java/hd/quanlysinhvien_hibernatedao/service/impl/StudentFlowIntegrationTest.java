package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.service.StudentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudentFlowIntegrationTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private EntityManager entityManager;

    private Long departmentId;

    @BeforeEach
    void setUp() {
        Department department = new Department();
        department.setCode("TEST_CSE");
        department.setName("Test Computer Science");
        entityManager.persist(department);
        entityManager.flush();
        departmentId = department.getId();
    }

    private StudentRequest request(String email) {
        return StudentRequest.builder()
                .name("Nguyen Van A")
                .email(email)
                .dob(LocalDate.of(2004, 1, 15))
                .departmentId(departmentId)
                .build();
    }

    @Test
    void createAndRead_roundTrip() {
        StudentResponse created = studentService.create(
                request("roundtrip@example.com")
        );
        assertNotNull(created.id());
        assertEquals(departmentId, created.departmentId());
        assertEquals("Test Computer Science", created.departmentName());

        StudentResponse retrieved = studentService.findById(created.id());
        assertEquals(created.id(), retrieved.id());
        assertEquals("roundtrip@example.com", retrieved.email());
    }

    @Test
    void update_preservesIdAndChangesFields() {
        StudentResponse created = studentService.create(
                request("before@example.com")
        );
        StudentRequest changed = request("after@example.com")
                .toBuilder()
                .name("Tran Van B")
                .dob(LocalDate.of(2005, 2, 2))
                .build();

        StudentResponse updated = studentService.update(created.id(), changed);

        assertEquals(created.id(), updated.id());
        assertEquals("Tran Van B", updated.name());
        assertEquals("after@example.com", updated.email());
        assertEquals(departmentId, updated.departmentId());
    }

    @Test
    void search_returnsCreatedStudent() {
        studentService.create(request("findme@example.com"));

        PageResponse<StudentResponse> result = studentService.search(
                "findme", departmentId, 0, 10, "name", "asc"
        );

        assertEquals(1, result.totalElements());
        assertEquals("findme@example.com", result.content().get(0).email());
    }

    @Test
    void delete_removesStudent() {
        StudentResponse created = studentService.create(
                request("delete@example.com")
        );
        studentService.delete(created.id());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.findById(created.id())
        );
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void create_duplicateEmailReturnsConflict() {
        studentService.create(request("duplicate@example.com"));
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> studentService.create(request("duplicate@example.com"))
        );
        assertEquals(HttpStatus.CONFLICT, ex.getStatusCode());
    }
}
