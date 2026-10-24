
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.DuplicateResourceException;
import hd.quanlysinhvien_hibernatedao.common.exception.ResourceNotFoundException;
import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;

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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class DepartmentRestImplTest {

    private static final String URL = "/api/departments";

    private static final String VALID_REQUEST = """
            {
                "code": "CSE",
                "name": "Computer Science",
                "description": "Computer Science Department"
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
    private DepartmentService departmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private DepartmentResponse departmentResponse() {
        return new DepartmentResponse(
                1L,
                "CSE",
                "Computer Science",
                "Computer Science Department",
                null,
                null
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
    void getAll_admin_success() throws Exception {
        when(departmentService.findAll())
                .thenReturn(List.of(departmentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].code").value("CSE"))
                .andExpect(jsonPath("$[0].name")
                        .value("Computer Science"));

        verify(departmentService).findAll();
    }

    @Test
    void getAll_lecturer_success() throws Exception {
        when(departmentService.findAll())
                .thenReturn(List.of(departmentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CSE"));

        verify(departmentService).findAll();
    }

    @Test
    void getAll_student_success() throws Exception {
        when(departmentService.findAll())
                .thenReturn(List.of(departmentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CSE"));

        verify(departmentService).findAll();
    }

    @Test
    void getAll_unauthenticated() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(departmentService, never()).findAll();
    }

    @Test
    void getAll_emptyList() throws Exception {
        when(departmentService.findAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_defaultParameters() throws Exception {
        PageResponse<DepartmentResponse> response =
                new PageResponse<>(
                        List.of(departmentResponse()),
                        0,
                        10,
                        1L,
                        1
                );

        when(departmentService.search(
                "", 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].code").value("CSE"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(departmentService).search(
                "", 0, 10, "id", "asc"
        );
    }

    @Test
    void search_customParameters() throws Exception {
        PageResponse<DepartmentResponse> response =
                new PageResponse<>(
                        List.of(departmentResponse()),
                        1,
                        5,
                        6L,
                        2
                );

        when(departmentService.search(
                "computer", 1, 5, "name", "desc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("LECTURER"))
                                .param("keyword", "computer")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sortBy", "name")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].code").value("CSE"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(departmentService).search(
                "computer", 1, 5, "name", "desc"
        );
    }

    @Test
    void search_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_admin_success() throws Exception {
        when(departmentService.findById(1L))
                .thenReturn(departmentResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CSE"))
                .andExpect(jsonPath("$.name")
                        .value("Computer Science"));

        verify(departmentService).findById(1L);
    }

    @Test
    void getById_student_success() throws Exception {
        when(departmentService.findById(1L))
                .thenReturn(departmentResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(departmentService.findById(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy khoa với ID: 999"
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
                        .value("Không tìm thấy khoa với ID: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/departments/999"));
    }

    @Test
    void getById_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_admin_success() throws Exception {
        when(departmentService.create(
                any(DepartmentRequest.class)
        )).thenReturn(departmentResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.code").value("CSE"));

        verify(departmentService).create(
                new DepartmentRequest(
                        "CSE",
                        "Computer Science",
                        "Computer Science Department"
                )
        );
    }

    @Test
    void create_unauthenticated() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(departmentService, never()).create(any());
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

        verify(departmentService, never()).create(any());
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

        verify(departmentService, never()).create(any());
    }

    @Test
    void create_blankCode_400() throws Exception {
        String body = """
                {
                    "code": "",
                    "name": "Computer Science",
                    "description": "Test"
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
                .andExpect(jsonPath("$.message").isNotEmpty());

        verify(departmentService, never()).create(any());
    }

    @Test
    void create_blankName_400() throws Exception {
        String body = """
                {
                    "code": "CSE",
                    "name": "",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(departmentService, never()).create(any());
    }

    @Test
    void create_tooLongCode_400() throws Exception {
        String body = """
                {
                    "code": "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
                    "name": "Computer Science",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(departmentService, never()).create(any());
    }

    @Test
    void create_duplicateCode_409() throws Exception {
        when(departmentService.create(
                any(DepartmentRequest.class)
        )).thenThrow(
                new DuplicateResourceException(
                        "Mã khoa đã tồn tại: CSE"
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
                        .value("Mã khoa đã tồn tại: CSE"));
    }

    @Test
    void update_admin_success() throws Exception {
        DepartmentResponse updated =
                new DepartmentResponse(
                        1L,
                        "CSE",
                        "Updated Department",
                        "Updated description",
                        null,
                        null
                );

        when(departmentService.update(
                eq(1L),
                any(DepartmentRequest.class)
        )).thenReturn(updated);

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Updated Department"));

        verify(departmentService).update(
                1L,
                new DepartmentRequest(
                        "CSE",
                        "Computer Science",
                        "Computer Science Department"
                )
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

        verify(departmentService, never())
                .update(eq(1L), any());
    }

    @Test
    void update_invalidRequest_400() throws Exception {
        String body = """
                {
                    "code": "",
                    "name": "",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(departmentService, never())
                .update(eq(1L), any());
    }

    @Test
    void update_notFound_404() throws Exception {
        when(departmentService.update(
                eq(999L),
                any(DepartmentRequest.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy khoa với ID: 999"
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
                        .value("Không tìm thấy khoa với ID: 999"));
    }

    @Test
    void delete_admin_success() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNoContent());

        verify(departmentService).delete(1L);
    }

    @Test
    void delete_student_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(departmentService, never()).delete(1L);
    }

    @Test
    void delete_unauthenticated() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isUnauthorized());

        verify(departmentService, never()).delete(1L);
    }

    @Test
    void delete_notFound_404() throws Exception {
        org.mockito.Mockito.doThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy khoa với ID: 999"
                )
        ).when(departmentService).delete(999L);

        mockMvc.perform(
                        delete(URL + "/999")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy khoa với ID: 999"));
    }
}
