package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;
import hd.quanlysinhvien_hibernatedao.service.StorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DocumentFlowIntegrationTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private IDocumentDao documentDao;

    @MockitoBean
    private StorageService storageService;

    private String appId() {
        return "DOC_TEST_" + UUID.randomUUID().toString().replace("-", "");
    }

    private MockMultipartFile file(String name) {
        return new MockMultipartFile(
                "files", name, "application/pdf",
                "test document content".getBytes(StandardCharsets.UTF_8)
        );
    }

    @Test
    void upload_persistsMetadataAndGeneratesId() {
        String appId = appId();
        DocumentResponse created = documentService.create(
                List.of(file("report.pdf")), appId
        ).get(0);

        assertNotNull(created.id());
        assertNotNull(created.creationTime());
        assertNotNull(created.modificationTime());
        assertEquals(appId, created.appId());
        assertEquals("report.pdf", created.name());
        assertEquals("system", created.owner());
        assertEquals(1.0, created.version());
        assertEquals(Boolean.TRUE, created.active());
        assertEquals(Boolean.FALSE, created.deleted());
        assertTrue(created.path().startsWith(appId + "/"));
        assertEquals(created.id(), documentService.getById(created.id()).id());
        verify(storageService).upload(eq(created.path()), any(), anyLong(), eq("application/pdf"));
    }

    @Test
    void upload_multipleFilesInOneRequest() {
        String appId = appId();
        List<DocumentResponse> created = documentService.create(
                List.of(file("first.pdf"), file("second.pdf")), appId
        );

        assertEquals(2, created.size());
        assertNotEquals(created.get(0).id(), created.get(1).id());
        assertEquals(2, documentService.listByApp(appId).size());
        verify(storageService, times(2)).upload(anyString(), any(), anyLong(), anyString());
    }

    @Test
    void upload_sameFilenameCreatesNewVersionAndDeactivatesPrevious() {
        String appId = appId();
        DocumentResponse first = documentService.create(
                List.of(file("report.pdf")), appId
        ).get(0);
        DocumentResponse second = documentService.create(
                List.of(file("report.pdf")), appId
        ).get(0);

        assertNotEquals(first.id(), second.id());
        assertEquals(1.0, first.version());
        assertEquals(2.0, second.version());
        Document previous = documentDao.findActiveById(first.id()).orElseThrow();
        assertFalse(previous.getActive());
        assertTrue(documentDao.findActiveById(second.id()).orElseThrow().getActive());
    }

    @Test
    void download_readsContentThroughStorageService() {
        DocumentResponse created = documentService.create(
                List.of(file("report.pdf")), appId()
        ).get(0);
        byte[] bytes = "hello from mock storage".getBytes(StandardCharsets.UTF_8);
        when(storageService.download(created.path()))
                .thenReturn(new ByteArrayInputStream(bytes));

        assertArrayEquals(bytes, documentService.download(created.path()));
        assertEquals("report.pdf", documentService.getFileName(created.path()));
        verify(storageService).download(created.path());
    }

    @Test
    void softDelete_hidesDocumentWithoutDeletingObject() {
        DocumentResponse created = documentService.create(
                List.of(file("report.pdf")), appId()
        ).get(0);

        documentService.delete(created.id());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> documentService.getById(created.id())
        );
        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        assertEquals(Boolean.TRUE,
                documentDao.findById(created.id()).orElseThrow().getDeleted());
        verify(storageService, never()).delete(anyString());
    }

    @Test
    void invalidFile_isRejectedBeforeStorage() {
        MockMultipartFile invalid = new MockMultipartFile(
                "files", "unsafe.exe", "application/octet-stream", new byte[]{1, 2, 3}
        );

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> documentService.create(List.of(invalid), appId())
        );
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(storageService);
    }

    @Test
    void listAndSearch_findPersistedDocument() {
        String appId = appId();
        DocumentResponse created = documentService.create(
                List.of(file("report.pdf")), appId
        ).get(0);

        assertEquals(1, documentService.listByApp(appId).size());
        assertEquals(1L, documentService.getDocCount("report", appId));
        List<DocumentResponse> found = documentService.getDoc(
                "report", 0, 10, "creationTime", "desc", appId
        );
        assertEquals(1, found.size());
        assertEquals(created.id(), found.get(0).id());
    }
}
