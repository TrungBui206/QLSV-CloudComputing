
package hd.quanlysinhvien_hibernatedao.security.authorization;

import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.RequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class DocumentAuthorizationTest extends AuthorizationTestBase {

    private static final String BASE_URL = "/api/documents";
    private static final String APP_ID = "APP01";
    private static final String DOCUMENT_ID = "doc-1";
    private static final String FILE_PATH = "APP01/report.pdf";

    @MockitoBean
    private DocumentService documentService;

    private static DocumentResponse documentResponse() {
        return new DocumentResponse(
                DOCUMENT_ID,
                APP_ID,
                "report.pdf",
                FILE_PATH,
                "test-user",
                1.0,
                false,
                true,
                null,
                null
        );
    }

    private static MockMultipartFile pdfFile() {
        return new MockMultipartFile(
                "files",
                "report.pdf",
                "application/pdf",
                "PDF test content".getBytes(StandardCharsets.UTF_8)
        );
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanListDocuments(Role role) throws Exception {

        when(documentService.listAll())
                .thenReturn(List.of(documentResponse()));

        mockMvc.perform(
                        get(BASE_URL)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(DOCUMENT_ID))
                .andExpect(jsonPath("$.data[0].name").value("report.pdf"));

        verify(documentService).listAll();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanListDocumentsByAppParameter(Role role)
            throws Exception {

        when(documentService.listByApp(APP_ID))
                .thenReturn(List.of(documentResponse()));

        mockMvc.perform(
                        get(BASE_URL)
                                .param("appId", APP_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].appId").value(APP_ID));

        verify(documentService).listByApp(APP_ID);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanSearchDocuments(Role role) throws Exception {

        when(documentService.getDoc(
                null,
                0,
                10,
                "creationTime",
                "desc",
                null
        )).thenReturn(List.of(documentResponse()));

        mockMvc.perform(
                        get(BASE_URL + "/search")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(DOCUMENT_ID));

        verify(documentService).getDoc(
                null, 0, 10, "creationTime", "desc", null
        );
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanCountDocuments(Role role) throws Exception {

        when(documentService.getDocCount(null, null))
                .thenReturn(5L);

        mockMvc.perform(
                        get(BASE_URL + "/count")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(5));

        verify(documentService).getDocCount(null, null);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanGetDocumentsByApp(Role role) throws Exception {

        when(documentService.listByApp(APP_ID))
                .thenReturn(List.of(documentResponse()));

        mockMvc.perform(
                        get(BASE_URL + "/app/" + APP_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].appId").value(APP_ID));

        verify(documentService).listByApp(APP_ID);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanGetDocumentById(Role role) throws Exception {

        when(documentService.getById(DOCUMENT_ID))
                .thenReturn(documentResponse());

        mockMvc.perform(
                        get(BASE_URL + "/" + DOCUMENT_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(DOCUMENT_ID))
                .andExpect(jsonPath("$.data.path").value(FILE_PATH));

        verify(documentService).getById(DOCUMENT_ID);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanDownloadByPath(Role role) throws Exception {

        byte[] fileContent =
                "PDF test content".getBytes(StandardCharsets.UTF_8);

        when(documentService.download(FILE_PATH))
                .thenReturn(fileContent);

        when(documentService.getFileName(FILE_PATH))
                .thenReturn("report.pdf");

        mockMvc.perform(
                        get(BASE_URL + "/download")
                                .param("filePath", FILE_PATH)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileContent))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                ));

        verify(documentService).download(FILE_PATH);
        verify(documentService).getFileName(FILE_PATH);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanDownloadById(Role role) throws Exception {

        byte[] fileContent =
                "PDF test content".getBytes(StandardCharsets.UTF_8);

        when(documentService.getById(DOCUMENT_ID))
                .thenReturn(documentResponse());

        when(documentService.download(FILE_PATH))
                .thenReturn(fileContent);

        when(documentService.getFileName(FILE_PATH))
                .thenReturn("report.pdf");

        mockMvc.perform(
                        get(BASE_URL + "/download/" + DOCUMENT_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes(fileContent))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                ));

        verify(documentService).getById(DOCUMENT_ID);
        verify(documentService).download(FILE_PATH);
        verify(documentService).getFileName(FILE_PATH);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "LECTURER"}
    )
    void adminAndLecturerCanUploadDocuments(Role role)
            throws Exception {

        when(documentService.create(any(), eq(APP_ID)))
                .thenReturn(List.of(documentResponse()));

        mockMvc.perform(
                        multipart(BASE_URL)
                                .file(pdfFile())
                                .param("appId", APP_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(DOCUMENT_ID));

        verify(documentService).create(any(), eq(APP_ID));
    }

    @Test
    void studentCannotUploadDocuments() throws Exception {

        mockMvc.perform(
                        multipart(BASE_URL)
                                .file(pdfFile())
                                .param("appId", APP_ID)
                                .header(
                                        "Authorization",
                                        bearerToken(Role.STUDENT)
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    @Test
    void adminCanDeleteDocument() throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/" + DOCUMENT_ID)
                                .header(
                                        "Authorization",
                                        bearerToken(Role.ADMIN)
                                )
                )
                .andExpect(status().isNoContent());

        verify(documentService).delete(DOCUMENT_ID);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotDeleteDocument(Role role)
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/" + DOCUMENT_ID)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(documentService);
    }

    static Stream<Arguments> unauthenticatedRequests() {
        return Stream.of(
                Arguments.of(
                        "List documents",
                        get(BASE_URL)
                ),
                Arguments.of(
                        "Search documents",
                        get(BASE_URL + "/search")
                ),
                Arguments.of(
                        "Count documents",
                        get(BASE_URL + "/count")
                ),
                Arguments.of(
                        "Get documents by app",
                        get(BASE_URL + "/app/" + APP_ID)
                ),
                Arguments.of(
                        "Get document by ID",
                        get(BASE_URL + "/" + DOCUMENT_ID)
                ),
                Arguments.of(
                        "Download by path",
                        get(BASE_URL + "/download")
                                .param("filePath", FILE_PATH)
                ),
                Arguments.of(
                        "Download by ID",
                        get(BASE_URL + "/download/" + DOCUMENT_ID)
                ),
                Arguments.of(
                        "Upload document",
                        multipart(BASE_URL)
                                .file(pdfFile())
                                .param("appId", APP_ID)
                ),
                Arguments.of(
                        "Delete document",
                        delete(BASE_URL + "/" + DOCUMENT_ID)
                )
        );
    }

    @ParameterizedTest(name = "{index} - {0}")
    @MethodSource("unauthenticatedRequests")
    void unauthenticatedCannotAccessDocuments(
            String operation,
            RequestBuilder request
    ) throws Exception {

        mockMvc.perform(request)
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(documentService);
    }
}
