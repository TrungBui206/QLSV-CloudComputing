package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.entity.AcademicYear;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AcademicYearMapperTest {

    private final AcademicYearMapper mapper = Mappers.getMapper(AcademicYearMapper.class);

    private AcademicYearRequest request() {
        return AcademicYearRequest.builder()
                .code("AY2026")
                .name("Academic Year 2026-2027")
                .description("Niên khóa 2026-2027")
                .startYear(2026)
                .endYear(2027)
                .build();
    }

    private AcademicYear entity() {
        AcademicYear entity = new AcademicYear();
        entity.setId(9L);
        entity.setCode("AY2026");
        entity.setName("Academic Year 2026-2027");
        entity.setDescription("Niên khóa 2026-2027");
        entity.setStartYear(2026);
        entity.setEndYear(2027);
        entity.setCreatedAt(LocalDateTime.of(2026, 1, 1, 8, 0));
        entity.setUpdatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));
        return entity;
    }

    @Test
    void builder_createsRecordWithAllValues() {
        AcademicYearRequest request = request();
        assertEquals("AY2026", request.code());
        assertEquals("Academic Year 2026-2027", request.name());
        assertEquals("Niên khóa 2026-2027", request.description());
        assertEquals(2026, request.startYear());
        assertEquals(2027, request.endYear());
    }

    @Test
    void toBuilder_copiesWithoutChangingOriginal() {
        AcademicYearRequest original = request();
        AcademicYearRequest changed = original.toBuilder()
                .code("AY2027")
                .startYear(2027)
                .endYear(2028)
                .build();

        assertEquals("AY2026", original.code());
        assertEquals(2026, original.startYear());
        assertEquals("AY2027", changed.code());
        assertEquals(2027, changed.startYear());
        assertEquals(2028, changed.endYear());
        assertEquals(original.name(), changed.name());
    }

    @Test
    void toEntity_mapsAllRequestFields() {
        AcademicYear entity = mapper.toEntity(request());
        assertNotNull(entity);
        assertNull(entity.getId());
        assertEquals("AY2026", entity.getCode());
        assertEquals("Academic Year 2026-2027", entity.getName());
        assertEquals("Niên khóa 2026-2027", entity.getDescription());
        assertEquals(2026, entity.getStartYear());
        assertEquals(2027, entity.getEndYear());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
    }

    @Test
    void toEntity_nullReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void updateEntity_preservesIdAndTimestamps() {
        AcademicYear existing = entity();
        LocalDateTime createdAt = existing.getCreatedAt();
        LocalDateTime updatedAt = existing.getUpdatedAt();

        AcademicYearRequest changed = request().toBuilder()
                .code("AY2027")
                .name("Updated year")
                .description("Updated description")
                .startYear(2027)
                .endYear(2028)
                .build();

        mapper.updateEntity(changed, existing);

        assertEquals(9L, existing.getId());
        assertEquals(createdAt, existing.getCreatedAt());
        assertEquals(updatedAt, existing.getUpdatedAt());
        assertEquals("AY2027", existing.getCode());
        assertEquals("Updated year", existing.getName());
        assertEquals("Updated description", existing.getDescription());
        assertEquals(2027, existing.getStartYear());
        assertEquals(2028, existing.getEndYear());
    }

    @Test
    void updateEntity_nullRequestDoesNotChangeEntity() {
        AcademicYear existing = entity();
        mapper.updateEntity(null, existing);
        assertEquals(9L, existing.getId());
        assertEquals("AY2026", existing.getCode());
        assertEquals(2026, existing.getStartYear());
    }

    @Test
    void toResponse_mapsAllFields() {
        AcademicYear entity = entity();
        AcademicYearResponse result = mapper.toResponse(entity);
        assertNotNull(result);
        assertEquals(entity.getId(), result.id());
        assertEquals(entity.getCode(), result.code());
        assertEquals(entity.getName(), result.name());
        assertEquals(entity.getDescription(), result.description());
        assertEquals(entity.getStartYear(), result.startYear());
        assertEquals(entity.getEndYear(), result.endYear());
        assertEquals(entity.getCreatedAt(), result.createdAt());
        assertEquals(entity.getUpdatedAt(), result.updatedAt());
    }

    @Test
    void toResponse_nullReturnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponses_mapsListInOrder() {
        AcademicYear first = entity();
        AcademicYear second = entity();
        second.setId(10L);
        second.setCode("AY2027");

        List<AcademicYearResponse> values = mapper.toResponses(List.of(first, second));
        assertEquals(2, values.size());
        assertEquals(9L, values.get(0).id());
        assertEquals(10L, values.get(1).id());
        assertEquals("AY2027", values.get(1).code());
    }

    @Test
    void toResponses_emptyListReturnsEmptyList() {
        assertTrue(mapper.toResponses(List.of()).isEmpty());
    }

    @Test
    void toResponses_nullListReturnsNull() {
        assertNull(mapper.toResponses(null));
    }
}
