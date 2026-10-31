package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Department;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DepartmentMapperTest {

    private final DepartmentMapper mapper = Mappers.getMapper(DepartmentMapper.class);

    private DepartmentRequest request() {
        return new DepartmentRequest(
                "CSE", "Computer Science", "Computer Science Department"
        );
    }

    private Department entity() {
        Department department = new Department();
        department.setId(1L);
        department.setCode("CSE");
        department.setName("Computer Science");
        department.setDescription("Computer Science Department");
        department.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        department.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        return department;
    }

    @Test
    void toEntity_mapsFieldsButIgnoresIdAndTimestamps() {
        Department result = mapper.toEntity(request());

        assertNotNull(result);
        assertEquals("CSE", result.getCode());
        assertEquals("Computer Science", result.getName());
        assertEquals("Computer Science Department", result.getDescription());
        assertNull(result.getId());
        assertNull(result.getCreatedAt());
        assertNull(result.getUpdatedAt());
    }

    @Test
    void toEntity_nullRequestReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void updateEntity_preservesIdAndTimestamps() {
        Department original = entity();
        LocalDateTime createdAt = original.getCreatedAt();
        LocalDateTime updatedAt = original.getUpdatedAt();

        DepartmentRequest changed = request().toBuilder()
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .build();

        mapper.updateEntity(changed, original);

        assertEquals(1L, original.getId());
        assertEquals("IT", original.getCode());
        assertEquals("Information Technology", original.getName());
        assertEquals("IT Department", original.getDescription());
        assertEquals(createdAt, original.getCreatedAt());
        assertEquals(updatedAt, original.getUpdatedAt());
    }

    @Test
    void updateEntity_nullRequestDoesNotChangeEntity() {
        Department original = entity();
        mapper.updateEntity(null, original);
        assertEquals(1L, original.getId());
        assertEquals("CSE", original.getCode());
        assertEquals("Computer Science", original.getName());
        assertEquals("Computer Science Department", original.getDescription());
    }

    @Test
    void toResponse_mapsAllFields() {
        Department original = entity();
        DepartmentResponse result = mapper.toResponse(original);

        assertNotNull(result);
        assertEquals(original.getId(), result.id());
        assertEquals(original.getCode(), result.code());
        assertEquals(original.getName(), result.name());
        assertEquals(original.getDescription(), result.description());
        assertEquals(original.getCreatedAt(), result.createdAt());
        assertEquals(original.getUpdatedAt(), result.updatedAt());
    }

    @Test
    void toResponse_nullEntityReturnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponses_preservesOrder() {
        Department first = entity();
        Department second = entity();
        second.setId(2L);
        second.setCode("IT");

        List<DepartmentResponse> result = mapper.toResponses(List.of(first, second));

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        assertEquals("IT", result.get(1).code());
    }

    @Test
    void toResponses_emptyListReturnsEmpty() {
        assertNotNull(mapper.toResponses(List.of()));
        assertTrue(mapper.toResponses(List.of()).isEmpty());
    }

    @Test
    void toResponses_nullListReturnsNull() {
        assertNull(mapper.toResponses(null));
    }

    @Test
    void requestBuilder_buildsAllFields() {
        DepartmentRequest result = DepartmentRequest.builder()
                .code("CSE")
                .name("Computer Science")
                .description("Description")
                .build();
        assertEquals("CSE", result.code());
        assertEquals("Computer Science", result.name());
        assertEquals("Description", result.description());
    }

    @Test
    void requestToBuilder_preservesOriginal() {
        DepartmentRequest original = request();
        DepartmentRequest modified = original.toBuilder()
                .name("Advanced Computer Science")
                .description("Updated")
                .build();

        assertEquals("Computer Science", original.name());
        assertEquals("Computer Science Department", original.description());
        assertEquals("CSE", modified.code());
        assertEquals("Advanced Computer Science", modified.name());
        assertEquals("Updated", modified.description());
    }
}
