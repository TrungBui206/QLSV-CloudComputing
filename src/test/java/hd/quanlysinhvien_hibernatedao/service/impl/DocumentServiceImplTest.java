
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.service.StorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    @Mock
    private IDocumentDao documentDao;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    @Test
    void createDocumentSuccess() {

        MultipartFile file = new MockMultipartFile(
                "files",
                "report.pdf",
                "application/pdf",
                "PDF content".getBytes()
        );

        when(documentDao.create(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        List<DocumentResponse> result =
                documentService.create(
                        List.of(file),
                        "APP01"
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).name());
        assertEquals("APP01", result.get(0).appId());
        assertEquals(1.0, result.get(0).version());
    }

    @Test
    void createDocumentWithNewVersion() {

        Document oldDocument = new Document();
        oldDocument.setId("old-id");
        oldDocument.setAppId("APP01");
        oldDocument.setName("report.pdf");
        oldDocument.setVersion(1.0);
        oldDocument.setActive(true);
        oldDocument.setDeleted(false);

        MultipartFile file = new MockMultipartFile(
                "files",
                "report.pdf",
                "application/pdf",
                "New PDF content".getBytes()
        );

        when(documentDao.getDocumentName(
                "report.pdf", "APP01"
        )).thenReturn(oldDocument);

        when(documentDao.getLatestVersion(
                "report.pdf", "APP01"
        )).thenReturn(1.0);

        when(documentDao.create(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );

        List<DocumentResponse> result =
                documentService.create(
                        List.of(file),
                        "APP01"
                );

        assertEquals(1, result.size());
        assertEquals(2.0, result.get(0).version());
        assertFalse(oldDocument.getActive());

        verify(documentDao).update(oldDocument);
    }

    @Test
    void createInvalidFileThrowsException() {

        MultipartFile file = new MockMultipartFile(
                "files",
                "test.txt",
                "text/plain",
                "Invalid content".getBytes()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> documentService.create(
                        List.of(file),
                        "APP01"
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void deleteDocumentSuccess() {

        Document document = new Document();
        document.setId("doc-1");
        document.setName("report.pdf");
        document.setDeleted(false);
        document.setActive(true);

        when(documentDao.findActiveById("doc-1"))
                .thenReturn(Optional.of(document));

        documentService.delete("doc-1");

        assertTrue(document.getDeleted());
        assertFalse(document.getActive());

        verify(documentDao).update(document);
        verifyNoInteractions(storageService);
    }

    @Test
    void listAllDocumentsSuccess() {

        Document document = new Document();
        document.setId("doc-1");
        document.setAppId("APP01");
        document.setName("report.pdf");
        document.setVersion(1.0);
        document.setDeleted(false);
        document.setActive(true);

        when(documentDao.findAllNotDeleted())
                .thenReturn(List.of(document));

        List<DocumentResponse> result =
                documentService.listAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).name());
    }
}
