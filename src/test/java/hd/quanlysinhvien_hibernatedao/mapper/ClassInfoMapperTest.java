
package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.entity.ClassInfo;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ClassInfoMapperTest {

    private final ClassInfoMapper mapper =
            Mappers.getMapper(ClassInfoMapper.class);

    private ClassInfo createEntity(
            String id,
            String name,
            String domain,
            String description
    ) {
        ClassInfo entity = new ClassInfo();
        entity.setId(id);
        entity.setName(name);
        entity.setDomain(domain);
        entity.setDescription(description);
        return entity;
    }

    @Test
    void toEntity_mapsFieldsAndIgnoresId() {
        ClassInfoRequest request = ClassInfoRequest.builder()
                .id("request-id")
                .name("CSE2026")
                .domain("Computer Science")
                .description("Lop khoa hoc may tinh")
                .build();

        ClassInfo result = mapper.toEntity(request);

        assertNotNull(result);
        assertNull(result.getId());
        assertEquals("CSE2026", result.getName());
        assertEquals("Computer Science", result.getDomain());
        assertEquals(
                "Lop khoa hoc may tinh",
                result.getDescription()
        );
    }

    @Test
    void toEntity_nullRequestReturnsNull() {
        ClassInfo result = mapper.toEntity(null);

        assertNull(result);
    }

    @Test
    void updateEntity_updatesFieldsAndPreservesId() {
        ClassInfo existing = createEntity(
                "original-id",
                "Old Class",
                "Old Domain",
                "Old Description"
        );

        ClassInfoRequest request = ClassInfoRequest.builder()
                .id("different-id")
                .name("New Class")
                .domain("New Domain")
                .description("New Description")
                .build();

        mapper.updateEntity(request, existing);

        assertEquals("original-id", existing.getId());
        assertEquals("New Class", existing.getName());
        assertEquals("New Domain", existing.getDomain());
        assertEquals(
                "New Description",
                existing.getDescription()
        );
    }

    @Test
    void updateEntity_nullRequestKeepsExistingData() {
        ClassInfo existing = createEntity(
                "class-001",
                "CSE2026",
                "Computer Science",
                "Description"
        );

        mapper.updateEntity(null, existing);

        assertEquals("class-001", existing.getId());
        assertEquals("CSE2026", existing.getName());
        assertEquals("Computer Science", existing.getDomain());
        assertEquals("Description", existing.getDescription());
    }

    @Test
    void toResponse_mapsAllFields() {
        ClassInfo entity = createEntity(
                "class-001",
                "CSE2026",
                "Computer Science",
                "Description"
        );

        ClassInfoResponse result = mapper.toResponse(entity);

        assertNotNull(result);
        assertEquals("class-001", result.id());
        assertEquals("CSE2026", result.name());
        assertEquals("Computer Science", result.domain());
        assertEquals("Description", result.description());
    }

    @Test
    void toResponse_nullEntityReturnsNull() {
        ClassInfoResponse result = mapper.toResponse(null);

        assertNull(result);
    }

    @Test
    void toResponses_mapsListAndPreservesOrder() {
        ClassInfo first = createEntity(
                "class-001",
                "CSE2026",
                "Computer Science",
                "First"
        );

        ClassInfo second = createEntity(
                "class-002",
                "CSE2027",
                "Software Engineering",
                "Second"
        );

        List<ClassInfoResponse> result =
                mapper.toResponses(List.of(first, second));

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("class-001", result.get(0).id());
        assertEquals("CSE2026", result.get(0).name());

        assertEquals("class-002", result.get(1).id());
        assertEquals("CSE2027", result.get(1).name());
    }

    @Test
    void toResponses_emptyListReturnsEmptyList() {
        List<ClassInfoResponse> result =
                mapper.toResponses(List.of());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void toResponses_nullListReturnsNull() {
        List<ClassInfoResponse> result =
                mapper.toResponses(null);

        assertNull(result);
    }
}
