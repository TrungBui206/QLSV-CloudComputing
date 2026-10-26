package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.DuplicateResourceException;
import hd.quanlysinhvien_hibernatedao.common.exception.ResourceNotFoundException;
import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.service.AcademicYearService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
class AcademicYearRestImplTest {

    private static final String URL = "/api/academic-years";

    private static final String VALID_REQUEST = """
            {
                "code": "AY2026",
                "name": "Academic Year 2026-2027",
                "description": "Academic year description",
                "startYear": 2026,
                "endYear": 2027
            }
            """;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @MockitoBean
    private AcademicYearService academicYearService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private AcademicYearResponse academicYearResponse() {
        return new AcademicYearResponse(
                1L,
                "AY2026",
                "Academic Year 2026-2027",
                "Academic year description",
                2026,
                2027,
                null,
                null
        );
    }

    private AcademicYearRequest academicYearRequest() {
        return new AcademicYearRequest(
                "AY2026",
                "Academic Year 2026-2027",
                "Academic year description",
                2026,
                2027
        );
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

    @Test
    void search_defaultParameters_admin() throws Exception {
        PageResponse<AcademicYearResponse> response =
                new PageResponse<>(
                        List.of(academicYearResponse()),
                        0,
                        10,
                        1L,
                        1
                );

        when(academicYearService.search(
                "", 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("AY2026"))
                .andExpect(jsonPath("$.data.content[0].name")
                        .value("Academic Year 2026-2027"))
                .andExpect(jsonPath("$.data.content[0].startYear").value(2026))
                .andExpect(jsonPath("$.data.content[0].endYear").value(2027))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(academicYearService).search(
                "", 0, 10, "id", "asc"
        );
    }

    @Test
    void search_customParameters_lecturer() throws Exception {
        PageResponse<AcademicYearResponse> response =
                new PageResponse<>(
                        List.of(academicYearResponse()),
                        1,
                        5,
                        6L,
                        2
                );

        when(academicYearService.search(
                "2026", 1, 5, "startYear", "desc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("LECTURER"))
                                .param("keyword", "2026")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sortBy", "startYear")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].code").value("AY2026"))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.totalPages").value(2));

        verify(academicYearService).search(
                "2026", 1, 5, "startYear", "desc"
        );
    }

    @Test
    void search_student_success() throws Exception {
        PageResponse<AcademicYearResponse> response =
                new PageResponse<>(
                        List.of(academicYearResponse()),
                        0,
                        10,
                        1L,
                        1
                );

        when(academicYearService.search(
                "", 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("AY2026"));

        verify(academicYearService).search(
                "", 0, 10, "id", "asc"
        );
    }

    @Test
    void search_emptyResult() throws Exception {
        PageResponse<AcademicYearResponse> response =
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0L,
                        0
                );

        when(academicYearService.search(
                "unknown", 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("keyword", "unknown")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.totalPages").value(0));
    }

    @Test
    void search_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/search"))
                .andExpect(status().isUnauthorized());

        verify(academicYearService, never()).search(
                any(),
                eq(0),
                eq(10),
                any(),
                any()
        );
    }

    @Test
    void search_invalidPage_400() throws Exception {
        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("page", "abc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ: page"));
    }

    @Test
    void getById_admin_success() throws Exception {
        when(academicYearService.findById(1L))
                .thenReturn(academicYearResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"))
                .andExpect(jsonPath("$.data.name")
                        .value("Academic Year 2026-2027"))
                .andExpect(jsonPath("$.data.description")
                        .value("Academic year description"))
                .andExpect(jsonPath("$.data.startYear").value(2026))
                .andExpect(jsonPath("$.data.endYear").value(2027));

        verify(academicYearService).findById(1L);
    }

    @Test
    void getById_lecturer_success() throws Exception {
        when(academicYearService.findById(1L))
                .thenReturn(academicYearResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"));

        verify(academicYearService).findById(1L);
    }

    @Test
    void getById_student_success() throws Exception {
        when(academicYearService.findById(1L))
                .thenReturn(academicYearResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(academicYearService).findById(1L);
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(academicYearService.findById(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy niên khóa với ID: 999"
                        )
                );

        mockMvc.perform(
                        get(URL + "/999")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy niên khóa với ID: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/academic-years/999"));
    }

    @Test
    void getById_invalidId_400() throws Exception {
        mockMvc.perform(
                        get(URL + "/invalid")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ: id"));
    }

    @Test
    void getById_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isUnauthorized());

        verify(academicYearService, never()).findById(1L);
    }

    @Test
    void create_admin_success() throws Exception {
        when(academicYearService.create(
                any(AcademicYearRequest.class)
        )).thenReturn(academicYearResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"))
                .andExpect(jsonPath("$.data.name")
                        .value("Academic Year 2026-2027"))
                .andExpect(jsonPath("$.data.startYear").value(2026))
                .andExpect(jsonPath("$.data.endYear").value(2027));

        verify(academicYearService).create(academicYearRequest());
    }

    @Test
    void create_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_student_forbidden() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("STUDENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_unauthenticated() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_blankCode_400() throws Exception {
        String body = """
                {
                    "code": "",
                    "name": "Academic Year 2026-2027",
                    "description": "Test",
                    "startYear": 2026,
                    "endYear": 2027
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("code:")));

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_blankName_400() throws Exception {
        String body = """
                {
                    "code": "AY2026",
                    "name": "",
                    "description": "Test",
                    "startYear": 2026,
                    "endYear": 2027
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("name:")));

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_missingStartYear_400() throws Exception {
        String body = """
                {
                    "code": "AY2026",
                    "name": "Academic Year 2026-2027",
                    "description": "Test",
                    "endYear": 2027
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("startYear:")));

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_startYearBelowMinimum_400() throws Exception {
        String body = """
                {
                    "code": "AY1899",
                    "name": "Invalid Academic Year",
                    "description": "Test",
                    "startYear": 1899,
                    "endYear": 1900
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("startYear:")));

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_endYearAboveMaximum_400() throws Exception {
        String body = """
                {
                    "code": "AY2100",
                    "name": "Invalid Academic Year",
                    "description": "Test",
                    "startYear": 2100,
                    "endYear": 2101
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("endYear:")));

        verify(academicYearService, never())
                .create(any(AcademicYearRequest.class));
    }

    @Test
    void create_duplicateCode_409() throws Exception {
        when(academicYearService.create(
                any(AcademicYearRequest.class)
        )).thenThrow(
                new DuplicateResourceException(
                        "Mã niên khóa đã tồn tại: AY2026"
                )
        );

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Mã niên khóa đã tồn tại: AY2026"));
    }

    @Test
    void update_admin_success() throws Exception {
        AcademicYearResponse updated =
                new AcademicYearResponse(
                        1L,
                        "AY2026",
                        "Updated Academic Year",
                        "Updated description",
                        2026,
                        2028,
                        null,
                        null
                );

        when(academicYearService.update(
                eq(1L),
                any(AcademicYearRequest.class)
        )).thenReturn(updated);

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"))
                .andExpect(jsonPath("$.data.name")
                        .value("Updated Academic Year"))
                .andExpect(jsonPath("$.data.startYear").value(2026))
                .andExpect(jsonPath("$.data.endYear").value(2028));

        verify(academicYearService).update(
                1L,
                academicYearRequest()
        );
    }

    @Test
    void update_student_forbidden() throws Exception {
        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never()).update(
                eq(1L),
                any(AcademicYearRequest.class)
        );
    }

    @Test
    void update_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never()).update(
                eq(1L),
                any(AcademicYearRequest.class)
        );
    }

    @Test
    void update_unauthenticated() throws Exception {
        mockMvc.perform(
                        put(URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(academicYearService, never()).update(
                eq(1L),
                any(AcademicYearRequest.class)
        );
    }

    @Test
    void update_invalidRequest_400() throws Exception {
        String body = """
                {
                    "code": "",
                    "name": "",
                    "description": "Test",
                    "startYear": 1800,
                    "endYear": 2200
                }
                """;

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(academicYearService, never()).update(
                eq(1L),
                any(AcademicYearRequest.class)
        );
    }

    @Test
    void update_notFound_404() throws Exception {
        when(academicYearService.update(
                eq(999L),
                any(AcademicYearRequest.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy niên khóa với ID: 999"
                )
        );

        mockMvc.perform(
                        put(URL + "/999")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy niên khóa với ID: 999"));
    }

    @Test
    void update_duplicateCode_409() throws Exception {
        when(academicYearService.update(
                eq(1L),
                any(AcademicYearRequest.class)
        )).thenThrow(
                new DuplicateResourceException(
                        "Mã niên khóa đã tồn tại: AY2026"
                )
        );

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Mã niên khóa đã tồn tại: AY2026"));
    }

    @Test
    void delete_admin_success() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNoContent());

        verify(academicYearService).delete(1L);
    }

    @Test
    void delete_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never()).delete(1L);
    }

    @Test
    void delete_student_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(academicYearService, never()).delete(1L);
    }

    @Test
    void delete_unauthenticated() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isUnauthorized());

        verify(academicYearService, never()).delete(1L);
    }

    @Test
    void delete_notFound_404() throws Exception {
        doThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy niên khóa với ID: 999"
                )
        ).when(academicYearService).delete(999L);

        mockMvc.perform(
                        delete(URL + "/999")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy niên khóa với ID: 999"));
    }
}