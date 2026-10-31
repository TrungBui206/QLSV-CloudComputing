package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import hd.quanlysinhvien_hibernatedao.entity.Student;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StudentMapperTest {

    private final StudentMapper mapper = Mappers.getMapper(StudentMapper.class);

    private Department department() {
        Department department = new Department();
        department.setId(10L);
        department.setCode("CSE");
        department.setName("Computer Science");
        return department;
    }

    private StudentRequest request() {
        return new StudentRequest(
                "Nguyen Van A",
                "student@example.com",
                LocalDate.of(2004, 1, 15),
                10L
        );
    }

    private Student entity() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Nguyen Van A");
        student.setEmail("student@example.com");
        student.setDob(LocalDate.of(2004, 1, 15));
        student.setDepartment(department());
        student.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        student.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        return student;
    }

    @Test
    void toEntity_mapsSimpleFieldsButNotRelationshipOrGeneratedFields() {
        Student result = mapper.toEntity(request());

        assertNotNull(result);
        assertEquals("Nguyen Van A", result.getName());
        assertEquals("student@example.com", result.getEmail());
        assertEquals(LocalDate.of(2004, 1, 15), result.getDob());
        assertNull(result.getId());
        assertNull(result.getDepartment());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_nullReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void updateEntity_updatesFieldsAndPreservesProtectedFields() {
        Student existing = entity();
        Department oldDepartment = existing.getDepartment();
        LocalDateTime createdAt = existing.getCreatedAt();
        LocalDateTime updatedAt = existing.getUpdatedAt();

        StudentRequest changed = new StudentRequest(
                "Tran Van B",
                "new@example.com",
                LocalDate.of(2005, 3, 20),
                99L
        );

        mapper.updateEntity(changed, existing);

        assertEquals(1L, existing.getId());
        assertEquals("Tran Van B", existing.getName());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals(LocalDate.of(2005, 3, 20), existing.getDob());
        assertSame(oldDepartment, existing.getDepartment());
        assertEquals(createdAt, existing.getCreatedAt());
        assertEquals(updatedAt, existing.getUpdatedAt());
    }

    @Test
    void updateEntity_nullRequestKeepsEntityUntouched() {
        Student existing = entity();
        mapper.updateEntity(null, existing);

        assertEquals(1L, existing.getId());
        assertEquals("Nguyen Van A", existing.getName());
        assertEquals("student@example.com", existing.getEmail());
        assertEquals(10L, existing.getDepartment().getId());
    }

    @Test
    void toResponse_mapsDepartmentAndTimestamps() {
        Student student = entity();
        StudentResponse result = mapper.toResponse(student);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Nguyen Van A", result.name());
        assertEquals("student@example.com", result.email());
        assertEquals(LocalDate.of(2004, 1, 15), result.dob());
        assertEquals(10L, result.departmentId());
        assertEquals("Computer Science", result.departmentName());
        assertEquals(student.getCreatedAt(), result.createdAt());
        assertEquals(student.getUpdatedAt(), result.updatedAt());
    }

    @Test
    void toResponse_nullReturnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponses_preservesOrder() {
        Student first = entity();
        Student second = entity();
        second.setId(2L);
        second.setName("Tran Van B");

        List<StudentResponse> result = mapper.toResponses(List.of(first, second));

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        assertEquals("Tran Van B", result.get(1).name());
    }

    @Test
    void toResponses_emptyReturnsEmpty() {
        assertNotNull(mapper.toResponses(List.of()));
        assertTrue(mapper.toResponses(List.of()).isEmpty());
    }

    @Test
    void toResponses_nullReturnsNull() {
        assertNull(mapper.toResponses(null));
    }

    @Test
    void requestBuilder_buildsAllProperties() {
        StudentRequest result = StudentRequest.builder()
                .name("Nguyen Van A")
                .email("student@example.com")
                .dob(LocalDate.of(2004, 1, 15))
                .departmentId(10L)
                .build();

        assertEquals("Nguyen Van A", result.name());
        assertEquals("student@example.com", result.email());
        assertEquals(LocalDate.of(2004, 1, 15), result.dob());
        assertEquals(10L, result.departmentId());
    }

    @Test
    void requestToBuilder_preservesOriginal() {
        StudentRequest original = request();
        StudentRequest modified = original.toBuilder()
                .name("Tran Van B")
                .departmentId(20L)
                .build();

        assertEquals("Nguyen Van A", original.name());
        assertEquals(10L, original.departmentId());
        assertEquals("Tran Van B", modified.name());
        assertEquals(20L, modified.departmentId());
        assertEquals(original.email(), modified.email());
        assertEquals(original.dob(), modified.dob());
    }
}
