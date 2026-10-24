
package hd.quanlysinhvien_hibernatedao.dao.impl;

import hd.quanlysinhvien_hibernatedao.entity.Document;

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
class DocumentDaoImplTest {

    @Autowired
    private DocumentDaoImpl documentDao;

    @Autowired
    private EntityManager entityManager;

    private String saveDocument(
            String name,
            String appId,
            String path,
            Double version,
            Boolean deleted,
            Boolean active
    ) {
        Document document = new Document();
        document.setName(name);
        document.setAppId(appId);
        document.setPath(path);
        document.setOwner("Test Owner");
        document.setVersion(version);
        document.setDeleted(deleted);
        document.setActive(active);

        Document saved = documentDao.create(document);

        entityManager.flush();

        String id = saved.getId();

        entityManager.clear();

        return id;
    }

    private String saveDocument(
            String name,
            String appId,
            String path
    ) {
        return saveDocument(
                name,
                appId,
                path,
                null,
                null,
                null
        );
    }

    @Test
    void create_success() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document found = documentDao.get(id);

        assertNotNull(found);
        assertEquals(id, found.getId());
        assertEquals("report.pdf", found.getName());
        assertEquals("APP01", found.getAppId());
        assertEquals("APP01/report.pdf", found.getPath());
        assertEquals("Test Owner", found.getOwner());
    }

    @Test
    void create_defaultValues() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document document = documentDao.get(id);

        assertNotNull(document);

        assertEquals(1.0, document.getVersion());
        assertEquals(Boolean.FALSE, document.getDeleted());
        assertEquals(Boolean.TRUE, document.getActive());

        assertNotNull(document.getCreationTime());
        assertNotNull(document.getModificationTime());

        assertDoesNotThrow(() -> UUID.fromString(document.getId()));
    }

    @Test
    void create_uniqueUUID() {
        String firstId = saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        String secondId = saveDocument(
                "second.pdf",
                "APP01",
                "APP01/second.pdf"
        );

        assertNotEquals(firstId, secondId);
        assertEquals(36, firstId.length());
        assertEquals(36, secondId.length());
    }

    @Test
    void getDocumentName_success() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document result = documentDao.getDocumentName(
                "report.pdf",
                "APP01"
        );

        assertNotNull(result);
        assertEquals("report.pdf", result.getName());
        assertEquals("APP01", result.getAppId());
    }

    @Test
    void getDocumentName_returnsLatestActiveVersion() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v1.pdf",
                1.0,
                false,
                true
        );

        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v2.pdf",
                2.0,
                false,
                true
        );

        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v3.pdf",
                3.0,
                false,
                false
        );

        Document result = documentDao.getDocumentName(
                "report.pdf",
                "APP01"
        );

        assertNotNull(result);
        assertEquals(2.0, result.getVersion());
        assertEquals("APP01/report-v2.pdf", result.getPath());
    }

    @Test
    void getDocumentName_excludesDeleted() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/deleted.pdf",
                2.0,
                true,
                true
        );

        Document result = documentDao.getDocumentName(
                "report.pdf",
                "APP01"
        );

        assertNull(result);
    }

    @Test
    void getDocumentName_wrongAppId() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document result = documentDao.getDocumentName(
                "report.pdf",
                "APP02"
        );

        assertNull(result);
    }

    @Test
    void getLatestVersion_success() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v1.pdf",
                1.0,
                false,
                true
        );

        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v2.pdf",
                2.0,
                false,
                true
        );

        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v3.pdf",
                3.0,
                false,
                true
        );

        Double result = documentDao.getLatestVersion(
                "report.pdf",
                "APP01"
        );

        assertEquals(3.0, result);
    }

    @Test
    void getLatestVersion_notFound() {
        Double result = documentDao.getLatestVersion(
                "unknown.pdf",
                "APP01"
        );

        assertNull(result);
    }

    @Test
    void getLatestVersion_includesDeletedDocuments() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v1.pdf",
                1.0,
                false,
                true
        );

        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report-v2.pdf",
                2.0,
                true,
                false
        );

        Double result = documentDao.getLatestVersion(
                "report.pdf",
                "APP01"
        );

        assertEquals(2.0, result);
    }

    @Test
    void getDocumentByAppId_success() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP01",
                "APP01/second.pdf"
        );

        saveDocument(
                "third.pdf",
                "APP02",
                "APP02/third.pdf"
        );

        List<Document> result =
                documentDao.getDocumentByAppId("APP01");

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(d -> "APP01".equals(d.getAppId()))
        );
    }

    @Test
    void getDocumentByAppId_excludesDeleted() {
        saveDocument(
                "active.pdf",
                "APP01",
                "APP01/active.pdf"
        );

        saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        List<Document> result =
                documentDao.getDocumentByAppId("APP01");

        assertEquals(1, result.size());
        assertEquals("active.pdf", result.get(0).getName());
    }

    @Test
    void findAllNotDeleted_success() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP02",
                "APP02/second.pdf"
        );

        saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        List<Document> result =
                documentDao.findAllNotDeleted();

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .noneMatch(d -> "deleted.pdf".equals(d.getName()))
        );
    }

    @Test
    void findActiveById_success() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Optional<Document> result =
                documentDao.findActiveById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findActiveById_deletedNotFound() {
        String id = saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        Optional<Document> result =
                documentDao.findActiveById(id);

        assertTrue(result.isEmpty());
    }

    @Test
    void findActiveById_inactiveNotDeleted() {
        String id = saveDocument(
                "inactive.pdf",
                "APP01",
                "APP01/inactive.pdf",
                1.0,
                false,
                false
        );

        Optional<Document> result =
                documentDao.findActiveById(id);

        assertTrue(result.isPresent());
        assertFalse(result.get().getActive());
    }

    @Test
    void findActiveByPath_success() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Optional<Document> result =
                documentDao.findActiveByPath(
                        "APP01/report.pdf"
                );

        assertTrue(result.isPresent());
        assertEquals("report.pdf", result.get().getName());
    }

    @Test
    void findActiveByPath_deletedNotFound() {
        saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        Optional<Document> result =
                documentDao.findActiveByPath(
                        "APP01/deleted.pdf"
                );

        assertTrue(result.isEmpty());
    }

    @Test
    void getDoc_searchByName() {
        saveDocument(
                "Report2026.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        saveDocument(
                "Lecture.pdf",
                "APP01",
                "APP01/lecture.pdf"
        );

        List<Document> result = documentDao.getDoc(
                "REPORT2026",
                0,
                10,
                "name",
                "asc",
                null
        );

        assertEquals(1, result.size());
        assertEquals("Report2026.pdf", result.get(0).getName());
    }

    @Test
    void getDoc_searchByAppIdKeyword() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP02",
                "APP02/second.pdf"
        );

        List<Document> result = documentDao.getDoc(
                "app02",
                0,
                10,
                "name",
                "asc",
                null
        );

        assertEquals(1, result.size());
        assertEquals("APP02", result.get(0).getAppId());
    }

    @Test
    void getDoc_filterByAppId() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP02",
                "APP02/second.pdf"
        );

        List<Document> result = documentDao.getDoc(
                null,
                0,
                10,
                "name",
                "asc",
                "APP01"
        );

        assertEquals(1, result.size());
        assertEquals("APP01", result.get(0).getAppId());
    }

    @Test
    void getDoc_combinedFilters() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        saveDocument(
                "lecture.pdf",
                "APP01",
                "APP01/lecture.pdf"
        );

        saveDocument(
                "report.pdf",
                "APP02",
                "APP02/report.pdf"
        );

        List<Document> result = documentDao.getDoc(
                "report",
                0,
                10,
                "name",
                "asc",
                "APP01"
        );

        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).getName());
        assertEquals("APP01", result.get(0).getAppId());
    }

    @Test
    void getDoc_excludesDeleted() {
        saveDocument(
                "active.pdf",
                "APP01",
                "APP01/active.pdf"
        );

        saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        List<Document> result = documentDao.getDoc(
                null,
                0,
                10,
                "name",
                "asc",
                null
        );

        assertEquals(1, result.size());
        assertEquals("active.pdf", result.get(0).getName());
    }

    @Test
    void getDoc_sortAscending() {
        saveDocument(
                "C.pdf",
                "APP01",
                "APP01/c.pdf"
        );

        saveDocument(
                "A.pdf",
                "APP01",
                "APP01/a.pdf"
        );

        saveDocument(
                "B.pdf",
                "APP01",
                "APP01/b.pdf"
        );

        List<Document> result = documentDao.getDoc(
                null,
                0,
                10,
                "name",
                "asc",
                "APP01"
        );

        assertEquals(3, result.size());
        assertEquals("A.pdf", result.get(0).getName());
        assertEquals("B.pdf", result.get(1).getName());
        assertEquals("C.pdf", result.get(2).getName());
    }

    @Test
    void getDoc_sortDescendingVersion() {
        saveDocument(
                "report-v1.pdf",
                "APP01",
                "APP01/report-v1.pdf",
                1.0,
                false,
                true
        );

        saveDocument(
                "report-v3.pdf",
                "APP01",
                "APP01/report-v3.pdf",
                3.0,
                false,
                true
        );

        saveDocument(
                "report-v2.pdf",
                "APP01",
                "APP01/report-v2.pdf",
                2.0,
                false,
                true
        );

        List<Document> result = documentDao.getDoc(
                null,
                0,
                10,
                "version",
                "desc",
                "APP01"
        );

        assertEquals(3, result.size());
        assertEquals(3.0, result.get(0).getVersion());
        assertEquals(2.0, result.get(1).getVersion());
        assertEquals(1.0, result.get(2).getVersion());
    }

    @Test
    void getDoc_pagination() {
        saveDocument(
                "A.pdf",
                "APP01",
                "APP01/a.pdf"
        );

        saveDocument(
                "B.pdf",
                "APP01",
                "APP01/b.pdf"
        );

        saveDocument(
                "C.pdf",
                "APP01",
                "APP01/c.pdf"
        );

        List<Document> result = documentDao.getDoc(
                null,
                1,
                1,
                "name",
                "asc",
                "APP01"
        );

        assertEquals(1, result.size());
        assertEquals("B.pdf", result.get(0).getName());
    }

    @Test
    void getDoc_defaultLimit() {
        for (int i = 1; i <= 12; i++) {
            saveDocument(
                    "Document" + i + ".pdf",
                    "APP01",
                    "APP01/document" + i + ".pdf"
            );
        }

        List<Document> result = documentDao.getDoc(
                null,
                null,
                null,
                "name",
                "asc",
                "APP01"
        );

        assertEquals(10, result.size());
    }

    @Test
    void getDoc_invalidSortFieldUsesDefault() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        List<Document> result = documentDao.getDoc(
                null,
                0,
                10,
                "invalidField",
                "desc",
                "APP01"
        );

        assertEquals(1, result.size());
    }

    @Test
    void getDocCount_success() {
        saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        saveDocument(
                "report2.pdf",
                "APP01",
                "APP01/report2.pdf"
        );

        saveDocument(
                "lecture.pdf",
                "APP02",
                "APP02/lecture.pdf"
        );

        Long result = documentDao.getDocCount(
                "report",
                "APP01"
        );

        assertEquals(2L, result);
    }

    @Test
    void getDocCount_excludesDeleted() {
        saveDocument(
                "active.pdf",
                "APP01",
                "APP01/active.pdf"
        );

        saveDocument(
                "deleted.pdf",
                "APP01",
                "APP01/deleted.pdf",
                1.0,
                true,
                true
        );

        Long result = documentDao.getDocCount(
                null,
                "APP01"
        );

        assertEquals(1L, result);
    }

    @Test
    void getDocCount_noFilters() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP02",
                "APP02/second.pdf"
        );

        Long result = documentDao.getDocCount(
                null,
                null
        );

        assertEquals(2L, result);
    }

    @Test
    void update_success() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document existing = documentDao.get(id);

        assertNotNull(existing);

        existing.setName("updated-report.pdf");
        existing.setPath("APP01/updated-report.pdf");
        existing.setVersion(2.0);

        documentDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        Document updated = documentDao.get(id);

        assertNotNull(updated);
        assertEquals("updated-report.pdf", updated.getName());
        assertEquals("APP01/updated-report.pdf", updated.getPath());
        assertEquals(2.0, updated.getVersion());
        assertNotNull(updated.getModificationTime());
    }

    @Test
    void softDelete_excludedFromActiveQueries() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        Document existing = documentDao.get(id);

        existing.setDeleted(true);

        documentDao.update(existing);

        entityManager.flush();
        entityManager.clear();

        assertNotNull(documentDao.get(id));

        assertTrue(
                documentDao.findActiveById(id).isEmpty()
        );

        assertTrue(
                documentDao.findActiveByPath(
                        "APP01/report.pdf"
                ).isEmpty()
        );

        assertEquals(
                0L,
                documentDao.getDocCount(null, "APP01")
        );
    }

    @Test
    void delete_success() {
        String id = saveDocument(
                "report.pdf",
                "APP01",
                "APP01/report.pdf"
        );

        documentDao.deleteByPk(id);

        entityManager.flush();
        entityManager.clear();

        assertNull(documentDao.get(id));
    }

    @Test
    void getAll_success() {
        saveDocument(
                "first.pdf",
                "APP01",
                "APP01/first.pdf"
        );

        saveDocument(
                "second.pdf",
                "APP02",
                "APP02/second.pdf"
        );

        List<Document> result = documentDao.getAll();

        assertEquals(2, result.size());
    }
}
