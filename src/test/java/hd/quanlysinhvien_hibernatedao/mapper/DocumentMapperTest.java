package hd.quanlysinhvien_hibernatedao.mapper;

import hd.quanlysinhvien_hibernatedao.dto.request.DocumentMetadataRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DocumentMapperTest {

    private final DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

    private DocumentMetadataRequest metadata() {
        return DocumentMetadataRequest.builder()
                .appId("APP01")
                .name("report.pdf")
                .path("APP01/unique_report.pdf")
                .owner("system")
                .version(1.0)
                .deleted(false)
                .active(true)
                .build();
    }

    private Document entity() {
        Document result = new Document();
        result.setId("doc-001");
        result.setAppId("APP01");
        result.setName("report.pdf");
        result.setPath("APP01/unique_report.pdf");
        result.setOwner("system");
        result.setVersion(1.0);
        result.setDeleted(false);
        result.setActive(true);
        result.setCreationTime(LocalDateTime.of(2026, 10, 1, 8, 30));
        result.setModificationTime(LocalDateTime.of(2026, 10, 2, 9, 45));
        return result;
    }

    @Test
    void builder_createsMetadataWithoutChangingFields() {
        DocumentMetadataRequest input = metadata();
        assertEquals("APP01", input.appId());
        assertEquals("report.pdf", input.name());
        assertEquals("APP01/unique_report.pdf", input.path());
        assertEquals("system", input.owner());
        assertEquals(1.0, input.version());
        assertEquals(Boolean.FALSE, input.deleted());
        assertEquals(Boolean.TRUE, input.active());
    }

    @Test
    void toBuilder_preservesOriginalAndChangesVersion() {
        DocumentMetadataRequest initial = metadata();
        DocumentMetadataRequest next = initial.toBuilder().version(2.0).build();
        assertEquals(1.0, initial.version());
        assertEquals(2.0, next.version());
        assertEquals(initial.path(), next.path());
        assertEquals(initial.name(), next.name());
    }

    @Test
    void toEntity_mapsAllMetadata() {
        Document document = mapper.toEntity(metadata());
        assertNotNull(document);
        assertEquals("APP01", document.getAppId());
        assertEquals("report.pdf", document.getName());
        assertEquals("APP01/unique_report.pdf", document.getPath());
        assertEquals("system", document.getOwner());
        assertEquals(1.0, document.getVersion());
        assertFalse(document.getDeleted());
        assertTrue(document.getActive());
    }

    @Test
    void toEntity_doesNotSupplyDatabaseManagedFields() {
        Document document = mapper.toEntity(metadata());
        assertNull(document.getId());
        assertNull(document.getCreationTime());
        assertNull(document.getModificationTime());
    }

    @Test
    void toEntity_mapsNewVersionAndInactiveMetadata() {
        DocumentMetadataRequest request = metadata().toBuilder()
                .version(3.0)
                .active(false)
                .deleted(true)
                .build();
        Document document = mapper.toEntity(request);
        assertEquals(3.0, document.getVersion());
        assertFalse(document.getActive());
        assertTrue(document.getDeleted());
    }

    @Test
    void toEntity_nullInputReturnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toResponse_mapsEveryDocumentField() {
        Document doc = entity();
        DocumentResponse response = mapper.toResponse(doc);
        assertEquals(doc.getId(), response.id());
        assertEquals(doc.getAppId(), response.appId());
        assertEquals(doc.getName(), response.name());
        assertEquals(doc.getPath(), response.path());
        assertEquals(doc.getOwner(), response.owner());
        assertEquals(doc.getVersion(), response.version());
        assertEquals(doc.getDeleted(), response.deleted());
        assertEquals(doc.getActive(), response.active());
        assertEquals(doc.getCreationTime(), response.creationTime());
        assertEquals(doc.getModificationTime(), response.modificationTime());
    }

    @Test
    void toResponse_nullInputReturnsNull() {
        assertNull(mapper.toResponse(null));
    }

    @Test
    void toResponses_preservesOrder() {
        Document first = entity();
        Document second = entity();
        second.setId("doc-002");
        second.setName("other.docx");
        List<DocumentResponse> results = mapper.toResponses(List.of(first, second));
        assertEquals(2, results.size());
        assertEquals("doc-001", results.get(0).id());
        assertEquals("doc-002", results.get(1).id());
        assertEquals("other.docx", results.get(1).name());
    }

    @Test
    void toResponses_emptyListReturnsEmptyList() {
        assertTrue(mapper.toResponses(List.of()).isEmpty());
    }

    @Test
    void toResponses_nullInputReturnsNull() {
        assertNull(mapper.toResponses(null));
    }

    @Test
    void toResponse_reflectsSoftDeleteFlags() {
        Document document = entity();
        document.setDeleted(true);
        document.setActive(false);
        DocumentResponse response = mapper.toResponse(document);
        assertTrue(response.deleted());
        assertFalse(response.active());
    }
}
