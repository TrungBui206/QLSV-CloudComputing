package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.FileExtensionNotSupportedException;
import hd.quanlysinhvien_hibernatedao.common.exception.ResourceNotFoundException;
import hd.quanlysinhvien_hibernatedao.common.exception.StorageException;
import hd.quanlysinhvien_hibernatedao.dto.response.DocumentResponse;
import hd.quanlysinhvien_hibernatedao.service.DocumentService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
                "app.jwt.issuer=quanlysinhvien-hibernatedao"
        }
)
@ActiveProfiles("test")
class DocumentRestImplTest {

    private static final String URL = "/api/documents";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private DocumentService documentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private String token(String role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("quanlysinhvien-hibernatedao")
                .subject("test-user")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .claim("roles", List.of(role))
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        return "Bearer " + jwtEncoder.encode(
                JwtEncoderParameters.from(header, claims)
        ).getTokenValue();
    }

    private MockMultipartFile pdfFile() {
        return new MockMultipartFile(
                "files",
                "report.pdf",
                "application/pdf",
                "PDF test content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile textFile() {
        return new MockMultipartFile(
                "files",
                "notes.txt",
                "text/plain",
                "Text test content".getBytes(StandardCharsets.UTF_8)
        );
    }

    private DocumentResponse documentResponse(String path) {
        DocumentResponse response = mock(DocumentResponse.class);

        when(response.path()).thenReturn(path);

        return response;
    }

    @ParameterizedTest
    @ValueSource(strings = {"ADMIN", "LECTURER", "STUDENT"})
    void listAll_authenticatedRoles_success(String role) throws Exception {
        when(documentService.listAll()).thenReturn(List.of());

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).listAll();
        verify(documentService, never()).listByApp(any());
    }

    @Test
    void listAll_blankAppId_success() throws Exception {
        when(documentService.listAll()).thenReturn(List.of());

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                                .param("appId", "   ")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).listAll();
    }

    @Test
    void listByApp_success() throws Exception {
        when(documentService.listByApp("APP01"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("STUDENT"))
                                .param("appId", "APP01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).listByApp("APP01");
        verify(documentService, never()).listAll();
    }

    @Test
    void listAll_unauthenticated() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).listAll();
    }

    @Test
    void search_defaultParameters_success() throws Exception {
        when(documentService.getDoc(
                null, 0, 10, "creationTime", "desc", null
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).getDoc(
                null, 0, 10, "creationTime", "desc", null
        );
    }

    @Test
    void search_customParameters_success() throws Exception {
        when(documentService.getDoc(
                "report",
                5,
                15,
                "name",
                "asc",
                "APP01"
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("LECTURER"))
                                .param("query", "report")
                                .param("lowerLimit", "5")
                                .param("upperLimit", "15")
                                .param("orderBy", "name")
                                .param("orderType", "asc")
                                .param("appId", "APP01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).getDoc(
                "report", 5, 15, "name", "asc", "APP01"
        );
    }

    @Test
    void search_student_success() throws Exception {
        when(documentService.getDoc(
                null, 0, 10, "creationTime", "desc", null
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).getDoc(
                null, 0, 10, "creationTime", "desc", null
        );
    }

    @Test
    void search_invalidLowerLimit_400() throws Exception {
        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("lowerLimit", "abc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ: lowerLimit"));
    }

    @Test
    void search_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void count_success() throws Exception {
        when(documentService.getDocCount("report", "APP01"))
                .thenReturn(5L);

        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("ADMIN"))
                                .param("query", "report")
                                .param("appId", "APP01")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(5));

        verify(documentService).getDocCount("report", "APP01");
    }

    @Test
    void count_withoutParameters_success() throws Exception {
        when(documentService.getDocCount(null, null))
                .thenReturn(0L);

        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(0));

        verify(documentService).getDocCount(null, null);
    }

    @Test
    void count_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getByApp_success() throws Exception {
        when(documentService.listByApp("APP01"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/app/APP01")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).listByApp("APP01");
    }

    @Test
    void getByApp_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/app/APP01"))
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).listByApp("APP01");
    }

    @Test
    void getById_success() throws Exception {
        DocumentResponse response =
                documentResponse("APP01/report.pdf");

        when(documentService.getById("doc-1"))
                .thenReturn(response);

        mockMvc.perform(
                        get(URL + "/doc-1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.path")
                        .value("APP01/report.pdf"));

        verify(documentService).getById("doc-1");
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(documentService.getById("unknown"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy tài liệu"
                        )
                );

        mockMvc.perform(
                        get(URL + "/unknown")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy tài liệu"))
                .andExpect(jsonPath("$.path")
                        .value("/api/documents/unknown"));
    }

    @Test
    void getById_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/doc-1"))
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).getById("doc-1");
    }

    @Test
    void create_admin_success() throws Exception {
        when(documentService.create(any(), eq("APP01")))
                .thenReturn(List.of());

        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .param("appId", "APP01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).create(
                org.mockito.ArgumentMatchers.argThat(
                        files -> files.size() == 1
                                && "report.pdf".equals(
                                files.get(0).getOriginalFilename()
                        )
                ),
                eq("APP01")
        );
    }

    @Test
    void create_multipleFiles_success() throws Exception {
        when(documentService.create(any(), eq("APP01")))
                .thenReturn(List.of());

        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .file(textFile())
                                .param("appId", "APP01")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(documentService).create(
                org.mockito.ArgumentMatchers.argThat(
                        files -> files.size() == 2
                                && "report.pdf".equals(
                                files.get(0).getOriginalFilename()
                        )
                                && "notes.txt".equals(
                                files.get(1).getOriginalFilename()
                        )
                ),
                eq("APP01")
        );
    }

    @Test
    void create_student_forbidden() throws Exception {
        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .param("appId", "APP01")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(documentService, never()).create(any(), any());
    }

    @Test
    void create_unauthenticated() throws Exception {
        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .param("appId", "APP01")
                )
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).create(any(), any());
    }

    @Test
    void create_missingAppId_400() throws Exception {
        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Thiếu tham số: appId"));

        verify(documentService, never()).create(any(), any());
    }

    @Test
    void create_missingFiles_400() throws Exception {
        mockMvc.perform(
                        multipart(URL)
                                .param("appId", "APP01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isBadRequest());

        verify(documentService, never()).create(any(), any());
    }

    @Test
    void create_unsupportedFile_400() throws Exception {
        when(documentService.create(any(), eq("APP01")))
                .thenThrow(
                        new FileExtensionNotSupportedException(
                                "Định dạng file không được hỗ trợ"
                        )
                );

        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .param("appId", "APP01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Định dạng file không được hỗ trợ"));
    }

    @Test
    void create_storageError_500() throws Exception {
        when(documentService.create(any(), eq("APP01")))
                .thenThrow(
                        new StorageException(
                                "MinIO unavailable"
                        )
                );

        mockMvc.perform(
                        multipart(URL)
                                .file(pdfFile())
                                .param("appId", "APP01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Có lỗi xảy ra khi xử lý lưu trữ tài liệu"
                        ));
    }

    @Test
    void downloadByPath_success() throws Exception {
        byte[] bytes = "PDF content"
                .getBytes(StandardCharsets.UTF_8);

        when(documentService.download("APP01/report.pdf"))
                .thenReturn(bytes);

        when(documentService.getFileName("APP01/report.pdf"))
                .thenReturn("report.pdf");

        mockMvc.perform(
                        get(URL + "/download")
                                .header("Authorization", token("STUDENT"))
                                .param("filePath", "APP01/report.pdf")
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        containsString("attachment")
                ));

        verify(documentService).download("APP01/report.pdf");
        verify(documentService).getFileName("APP01/report.pdf");
    }

    @Test
    void downloadByPath_missingParameter_400() throws Exception {
        mockMvc.perform(
                        get(URL + "/download")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Thiếu tham số: filePath"));

        verify(documentService, never()).download(any());
    }

    @Test
    void downloadByPath_notFound_404() throws Exception {
        when(documentService.download("APP01/missing.pdf"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy file"
                        )
                );

        mockMvc.perform(
                        get(URL + "/download")
                                .header("Authorization", token("ADMIN"))
                                .param("filePath", "APP01/missing.pdf")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy file"));
    }

    @Test
    void downloadById_success() throws Exception {
        DocumentResponse response =
                documentResponse("APP01/report.pdf");

        byte[] bytes = "Document content"
                .getBytes(StandardCharsets.UTF_8);

        when(documentService.getById("doc-1"))
                .thenReturn(response);

        when(documentService.download("APP01/report.pdf"))
                .thenReturn(bytes);

        when(documentService.getFileName("APP01/report.pdf"))
                .thenReturn("report.pdf");

        mockMvc.perform(
                        get(URL + "/download/doc-1")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(content().bytes(bytes))
                .andExpect(content().contentType(
                        MediaType.APPLICATION_OCTET_STREAM
                ))
                .andExpect(header().string(
                        "Content-Disposition",
                        containsString("attachment")
                ));

        verify(documentService).getById("doc-1");
        verify(documentService).download("APP01/report.pdf");
        verify(documentService).getFileName("APP01/report.pdf");
    }

    @Test
    void downloadById_notFound_404() throws Exception {
        when(documentService.getById("unknown"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy tài liệu"
                        )
                );

        mockMvc.perform(
                        get(URL + "/download/unknown")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

        verify(documentService, never()).download(any());
    }

    @Test
    void download_unauthenticated() throws Exception {
        mockMvc.perform(
                        get(URL + "/download")
                                .param("filePath", "APP01/report.pdf")
                )
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).download(any());
    }

    @Test
    void delete_admin_success() throws Exception {
        mockMvc.perform(
                        delete(URL + "/doc-1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNoContent());

        verify(documentService).delete("doc-1");
    }

    @Test
    void delete_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/doc-1")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isForbidden());

        verify(documentService, never()).delete("doc-1");
    }

    @Test
    void delete_student_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/doc-1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(documentService, never()).delete("doc-1");
    }

    @Test
    void delete_unauthenticated() throws Exception {
        mockMvc.perform(
                        delete(URL + "/doc-1")
                )
                .andExpect(status().isUnauthorized());

        verify(documentService, never()).delete("doc-1");
    }

    @Test
    void delete_notFound_404() throws Exception {
        doThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy tài liệu"
                )
        ).when(documentService).delete("unknown");

        mockMvc.perform(
                        delete(URL + "/unknown")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy tài liệu"));
    }

    @Test
    void delete_storageError_500() throws Exception {
        doThrow(
                new StorageException(
                        "Cannot delete file from MinIO"
                )
        ).when(documentService).delete("doc-1");

        mockMvc.perform(
                        delete(URL + "/doc-1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Có lỗi xảy ra khi xử lý lưu trữ tài liệu"
                        ));
    }
}