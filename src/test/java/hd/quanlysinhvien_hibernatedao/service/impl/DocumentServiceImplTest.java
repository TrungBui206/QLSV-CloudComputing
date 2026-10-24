
package hd.quanlysinhvien_hibernatedao.service.impl;

import hd.quanlysinhvien_hibernatedao.dao.IDocumentDao;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.entity.Document;
import hd.quanlysinhvien_hibernatedao.service.StorageService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceImplTest {

    private static final String APP_ID = "APP01";

    private static final long MB = 1024L * 1024L;

    @Mock
    private IDocumentDao documentDao;

    @Mock
    private StorageService storageService;

    @InjectMocks
    private DocumentServiceImpl documentService;

    // ==========================================
    // HELPER METHODS
    // ==========================================

    private MockMultipartFile createFile(String fileName) {
        return new MockMultipartFile(
                "files",
                fileName,
                "application/pdf",
                "PDF content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private Document createDocument() {
        Document document = new Document();

        document.setId("doc-1");
        document.setAppId(APP_ID);
        document.setName("report.pdf");
        document.setPath("APP01/report.pdf");
        document.setVersion(1.0);
        document.setActive(true);
        document.setDeleted(false);

        return document;
    }

    private void mockCreateDocument() {
        when(documentDao.create(any(Document.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0)
                );
    }

    private void assertBadRequest(
            org.junit.jupiter.api.function.Executable action
    ) {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                action
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );
    }

    // ==========================================
    // GROUP 1: UPLOAD SUCCESS
    // ==========================================

    @Test
    void createDocumentSuccess() {

        mockCreateDocument();

        List<DocumentResponse> result =
                documentService.create(
                        List.of(createFile("report.pdf")),
                        APP_ID
                );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).name());
        assertEquals(APP_ID, result.get(0).appId());
        assertEquals(1.0, result.get(0).version());

        verify(storageService).upload(
                anyString(),
                any(InputStream.class),
                anyLong(),
                eq("application/pdf")
        );

        verify(documentDao).flush();
    }

    @Test
    void createDocumentWithNewVersion() {

        Document oldDocument = createDocument();

        when(documentDao.getDocumentName(
                "report.pdf",
                APP_ID
        )).thenReturn(oldDocument);

        when(documentDao.getLatestVersion(
                "report.pdf",
                APP_ID
        )).thenReturn(1.0);

        mockCreateDocument();

        List<DocumentResponse> result =
                documentService.create(
                        List.of(createFile("report.pdf")),
                        APP_ID
                );

        assertEquals(1, result.size());
        assertEquals(2.0, result.get(0).version());

        assertFalse(oldDocument.getActive());

        verify(documentDao).update(oldDocument);
    }

    @Test
    void createDocumentTrimAppId() {

        mockCreateDocument();

        documentService.create(
                List.of(createFile("report.pdf")),
                "  APP01  "
        );

        ArgumentCaptor<Document> captor =
                ArgumentCaptor.forClass(Document.class);

        verify(documentDao).create(captor.capture());

        assertEquals(
                APP_ID,
                captor.getValue().getAppId()
        );

        assertTrue(
                captor.getValue().getPath()
                        .startsWith("APP01/")
        );
    }

    // ==========================================
    // GROUP 2: UPLOAD VALIDATION
    // ==========================================

    @Test
    void createInvalidFileThrowsException() {

        MultipartFile file = createFile("test.txt");

        assertBadRequest(() ->
                documentService.create(
                        List.of(file),
                        APP_ID
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void createNullAppIdThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile("report.pdf")),
                        null
                )
        );
    }

    @Test
    void createBlankAppIdThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile("report.pdf")),
                        "   "
                )
        );
    }

    @Test
    void createTooLongAppIdThrowsException() {

        String appId = "A".repeat(101);

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile("report.pdf")),
                        appId
                )
        );
    }

    @Test
    void createNullFilesThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        null,
                        APP_ID
                )
        );
    }

    @Test
    void createEmptyFileListThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        List.of(),
                        APP_ID
                )
        );
    }

    @Test
    void createEmptyFileThrowsException() {

        MultipartFile file = new MockMultipartFile(
                "files",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertBadRequest(() ->
                documentService.create(
                        List.of(file),
                        APP_ID
                )
        );
    }

    @Test
    void createFileOver10MBThrowsException() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getSize())
                .thenReturn(11L * MB);

        assertBadRequest(() ->
                documentService.create(
                        List.of(file),
                        APP_ID
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void createTotalSizeOver50MBThrowsException() {

        MultipartFile file = mock(MultipartFile.class);

        when(file.getSize())
                .thenReturn(10L * MB);

        when(file.getOriginalFilename())
                .thenReturn("large.pdf");

        // Moi file 10MB, 6 file = 60MB
        List<MultipartFile> files = List.of(
                file, file, file,
                file, file, file
        );

        assertBadRequest(() ->
                documentService.create(
                        files,
                        APP_ID
                )
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void createInvalidFileNameThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile("../report.pdf")),
                        APP_ID
                )
        );
    }

    @Test
    void createMissingExtensionThrowsException() {

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile("report")),
                        APP_ID
                )
        );
    }

    @Test
    void createLongFileNameThrowsException() {

        String fileName =
                "a".repeat(51) + ".pdf";

        assertBadRequest(() ->
                documentService.create(
                        List.of(createFile(fileName)),
                        APP_ID
                )
        );
    }

    // ==========================================
    // GROUP 3: UPLOAD ERRORS
    // ==========================================

    @Test
    void createDatabaseErrorCleansUpMinio() {

        when(documentDao.create(any(Document.class)))
                .thenThrow(
                        new RuntimeException("Database failed")
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.create(
                                List.of(createFile("report.pdf")),
                                APP_ID
                        )
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );

        ArgumentCaptor<String> pathCaptor =
                ArgumentCaptor.forClass(String.class);

        verify(storageService)
                .delete(pathCaptor.capture());

        assertTrue(
                pathCaptor.getValue()
                        .startsWith(APP_ID + "/")
        );
    }

    @Test
    void createMinioUploadErrorThrowsException() {

        doThrow(new IllegalStateException("MinIO failed"))
                .when(storageService)
                .upload(
                        anyString(),
                        any(InputStream.class),
                        anyLong(),
                        anyString()
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.create(
                                List.of(createFile("report.pdf")),
                                APP_ID
                        )
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );

        verify(documentDao, never())
                .create(any(Document.class));
    }

    // ==========================================
    // GROUP 4: LIST AND SEARCH
    // ==========================================

    @Test
    void listAllDocumentsSuccess() {

        Document document = createDocument();

        when(documentDao.findAllNotDeleted())
                .thenReturn(List.of(document));

        List<DocumentResponse> result =
                documentService.listAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).name());
    }

    @Test
    void listAllEmptyReturnsEmptyList() {

        when(documentDao.findAllNotDeleted())
                .thenReturn(List.of());

        assertTrue(
                documentService.listAll().isEmpty()
        );
    }

    @Test
    void listByAppSuccess() {

        when(documentDao.getDocumentByAppId(APP_ID))
                .thenReturn(List.of(createDocument()));

        List<DocumentResponse> result =
                documentService.listByApp("  APP01  ");

        assertEquals(1, result.size());

        verify(documentDao)
                .getDocumentByAppId(APP_ID);
    }

    @Test
    void listByAppBlankThrowsException() {

        assertBadRequest(() ->
                documentService.listByApp("   ")
        );
    }

    @Test
    void getDocCountSuccess() {

        when(documentDao.getDocCount("report", APP_ID))
                .thenReturn(5L);

        Long count =
                documentService.getDocCount(
                        "report",
                        APP_ID
                );

        assertEquals(5L, count);
    }

    @Test
    void getDocEmptyResult() {

        when(documentDao.getDoc(
                "report",
                0,
                10,
                "name",
                "asc",
                APP_ID
        )).thenReturn(List.of());

        List<DocumentResponse> result =
                documentService.getDoc(
                        "report",
                        0,
                        10,
                        "name",
                        "asc",
                        APP_ID
                );

        assertTrue(result.isEmpty());
    }

    // ==========================================
    // GROUP 5: GET DOCUMENT BY ID
    // ==========================================

    @Test
    void getByIdSuccess() {

        Document document = createDocument();

        when(documentDao.findActiveById("doc-1"))
                .thenReturn(Optional.of(document));

        DocumentResponse response =
                documentService.getById("doc-1");

        assertNotNull(response);
        assertEquals("report.pdf", response.name());
    }

    @Test
    void getByIdNotFound() {

        when(documentDao.findActiveById("missing"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.getById("missing")
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    // ==========================================
    // GROUP 6: DOWNLOAD
    // ==========================================

    @Test
    void downloadDocumentSuccess() {

        String path = "APP01/report.pdf";

        Document document = createDocument();

        when(documentDao.findActiveByPath(path))
                .thenReturn(Optional.of(document));

        when(storageService.download(path))
                .thenReturn(
                        new ByteArrayInputStream(
                                "Hello MinIO".getBytes(
                                        StandardCharsets.UTF_8
                                )
                        )
                );

        byte[] result =
                documentService.download(path);

        assertEquals(
                "Hello MinIO",
                new String(
                        result,
                        StandardCharsets.UTF_8
                )
        );

        verify(storageService).download(path);
    }

    @Test
    void downloadDocumentNotFound() {

        String path = "APP01/missing.pdf";

        when(documentDao.findActiveByPath(path))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.download(path)
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verifyNoInteractions(storageService);
    }

    @Test
    void downloadMinioError() {

        String path = "APP01/report.pdf";

        when(documentDao.findActiveByPath(path))
                .thenReturn(Optional.of(createDocument()));

        when(storageService.download(path))
                .thenThrow(
                        new IllegalStateException("MinIO failed")
                );

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.download(path)
                );

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getStatusCode()
        );
    }

    @Test
    void getFileNameSuccess() {

        String path = "APP01/report.pdf";

        when(documentDao.findActiveByPath(path))
                .thenReturn(Optional.of(createDocument()));

        String name =
                documentService.getFileName(path);

        assertEquals("report.pdf", name);
    }

    // ==========================================
    // GROUP 7: DELETE
    // ==========================================

    @Test
    void deleteDocumentSuccess() {

        Document document = createDocument();

        when(documentDao.findActiveById("doc-1"))
                .thenReturn(Optional.of(document));

        documentService.delete("doc-1");

        assertTrue(document.getDeleted());
        assertFalse(document.getActive());

        verify(documentDao).update(document);

        // Soft delete khong xoa object tren MinIO
        verifyNoInteractions(storageService);
    }

    @Test
    void deleteDocumentNotFound() {

        when(documentDao.findActiveById("missing"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> documentService.delete("missing")
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );

        verify(documentDao, never())
                .update(any(Document.class));
    }
}
