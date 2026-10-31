package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DepartmentFlowIntegrationTest {

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private EntityManager entityManager;

    private DepartmentRequest request(String code) {
        return DepartmentRequest.builder()
                .code(code)
                .name("Computer Science")
                .description("Department integration test")
                .build();
    }

    @Test
    void createAndRead_roundTrip() {
        DepartmentResponse created = departmentService.create(request("CSE_ROUNDTRIP"));

        assertNotNull(created.id());
        assertEquals("CSE_ROUNDTRIP", created.code());

        DepartmentResponse found = departmentService.findById(created.id());
        assertEquals(created.id(), found.id());
        assertEquals("Computer Science", found.name());
    }

    @Test
    void update_preservesIdAndChangesFields() {
        DepartmentResponse created = departmentService.create(request("CSE_BEFORE"));

        DepartmentRequest changed = request("CSE_AFTER")
                .toBuilder()
                .name("Advanced Computer Science")
                .build();

        DepartmentResponse updated = departmentService.update(created.id(), changed);

        assertEquals(created.id(), updated.id());
        assertEquals("CSE_AFTER", updated.code());
        assertEquals("Advanced Computer Science", updated.name());
    }

    @Test
    void search_returnsCreatedDepartment() {
        departmentService.create(request("CSE_FINDME"));

        PageResponse<DepartmentResponse> result = departmentService.search(
                "CSE_FINDME", 0, 10, "code", "asc"
        );

        assertEquals(1L, result.totalElements());
        assertEquals("CSE_FINDME", result.content().get(0).code());
    }

    @Test
    void delete_removesDepartment() {
        DepartmentResponse created = departmentService.create(request("CSE_DELETE"));
        departmentService.delete(created.id());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.findById(created.id())
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void create_duplicateCodeReturnsConflict() {
        departmentService.create(request("CSE_DUPLICATE"));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.create(request("CSE_DUPLICATE"))
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }

    @Test
    void delete_departmentWithStudentReturnsConflict() {
        DepartmentResponse created = departmentService.create(request("CSE_HAS_STUDENT"));
        Department managedDepartment = entityManager.find(Department.class, created.id());

        Student student = new Student();
        student.setName("Test Student");
        student.setEmail("department-delete-test@example.com");
        student.setDepartment(managedDepartment);
        entityManager.persist(student);
        entityManager.flush();

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> departmentService.delete(created.id())
        );
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
    }
}
