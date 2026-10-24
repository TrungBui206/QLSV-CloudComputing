
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.DuplicateResourceException;
import hd.quanlysinhvien_hibernatedao.common.exception.ResourceNotFoundException;
import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.service.ClassInfoService;

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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class ClassInfoRestImplTest {

    private static final String URL = "/api/classes";

    private static final String VALID_REQUEST = """
            {
                "id": "CSE01",
                "name": "Computer Science 01",
                "domain": "Computer Science",
                "description": "Class information"
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
    private ClassInfoService classInfoService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private ClassInfoRequest classInfoRequest() {
        return new ClassInfoRequest(
                "CSE01",
                "Computer Science 01",
                "Computer Science",
                "Class information"
        );
    }

    private ClassInfoResponse classInfoResponse() {
        return new ClassInfoResponse(
                "CSE01",
                "Computer Science 01",
                "Computer Science",
                "Class information"
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
        when(classInfoService.getAll())
                .thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"))
                .andExpect(jsonPath("$[0].name")
                        .value("Computer Science 01"))
                .andExpect(jsonPath("$[0].domain")
                        .value("Computer Science"))
                .andExpect(jsonPath("$[0].description")
                        .value("Class information"));

        verify(classInfoService).getAll();
    }

    @Test
    void getAll_lecturer_success() throws Exception {
        when(classInfoService.getAll())
                .thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"));

        verify(classInfoService).getAll();
    }

    @Test
    void getAll_student_success() throws Exception {
        when(classInfoService.getAll())
                .thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"));

        verify(classInfoService).getAll();
    }

    @Test
    void getAll_emptyList() throws Exception {
        when(classInfoService.getAll())
                .thenReturn(List.of());

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAll_unauthenticated() throws Exception {
        mockMvc.perform(get(URL))
                .andExpect(status().isUnauthorized());

        verify(classInfoService, never()).getAll();
    }

    @Test
    void getById_admin_success() throws Exception {
        when(classInfoService.getById("CSE01"))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        get(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"))
                .andExpect(jsonPath("$.name")
                        .value("Computer Science 01"))
                .andExpect(jsonPath("$.domain")
                        .value("Computer Science"));

        verify(classInfoService).getById("CSE01");
    }

    @Test
    void getById_lecturer_success() throws Exception {
        when(classInfoService.getById("CSE01"))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        get(URL + "/CSE01")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"));
    }

    @Test
    void getById_student_success() throws Exception {
        when(classInfoService.getById("CSE01"))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        get(URL + "/CSE01")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"));

        verify(classInfoService).getById("CSE01");
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(classInfoService.getById("UNKNOWN"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy lớp học: UNKNOWN"
                        )
                );

        mockMvc.perform(
                        get(URL + "/UNKNOWN")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy lớp học: UNKNOWN"))
                .andExpect(jsonPath("$.path")
                        .value("/api/classes/UNKNOWN"));
    }

    @Test
    void getById_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/CSE01"))
                .andExpect(status().isUnauthorized());

        verify(classInfoService, never()).getById("CSE01");
    }

    @Test
    void getByName_success() throws Exception {
        when(classInfoService.getByName("Computer Science 01"))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        get(URL + "/name/Computer Science 01")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"))
                .andExpect(jsonPath("$.name")
                        .value("Computer Science 01"));

        verify(classInfoService)
                .getByName("Computer Science 01");
    }

    @Test
    void getByName_notFound_404() throws Exception {
        when(classInfoService.getByName("Unknown"))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy lớp học"
                        )
                );

        mockMvc.perform(
                        get(URL + "/name/Unknown")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getByDomain_success() throws Exception {
        when(classInfoService.getByDomain("Computer Science"))
                .thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL + "/domain/Computer Science")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"))
                .andExpect(jsonPath("$[0].domain")
                        .value("Computer Science"));

        verify(classInfoService)
                .getByDomain("Computer Science");
    }

    @Test
    void getByDomain_emptyList() throws Exception {
        when(classInfoService.getByDomain("Unknown"))
                .thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/domain/Unknown")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void search_allParameters_success() throws Exception {
        when(classInfoService.search(
                "computer",
                0,
                10,
                "name",
                "asc"
        )).thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("query", "computer")
                                .param("lowerLimit", "0")
                                .param("upperLimit", "10")
                                .param("orderBy", "name")
                                .param("orderType", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"))
                .andExpect(jsonPath("$[0].name")
                        .value("Computer Science 01"));

        verify(classInfoService).search(
                "computer",
                0,
                10,
                "name",
                "asc"
        );
    }

    @Test
    void search_withoutParameters_success() throws Exception {
        when(classInfoService.search(
                null,
                null,
                null,
                null,
                null
        )).thenReturn(List.of(classInfoResponse()));

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("CSE01"));

        verify(classInfoService).search(
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    void search_emptyResult() throws Exception {
        when(classInfoService.search(
                eq("unknown"),
                any(),
                any(),
                any(),
                any()
        )).thenReturn(List.of());

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("query", "unknown")
                                .param("lowerLimit", "0")
                                .param("upperLimit", "10")
                                .param("orderBy", "name")
                                .param("orderType", "asc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
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
    void count_admin_success() throws Exception {
        when(classInfoService.getCount("computer"))
                .thenReturn(5);

        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("ADMIN"))
                                .param("query", "computer")
                )
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(classInfoService).getCount("computer");
    }

    @Test
    void count_withoutQuery_success() throws Exception {
        when(classInfoService.getCount(null))
                .thenReturn(2);

        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string("2"));

        verify(classInfoService).getCount(null);
    }

    @Test
    void count_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isForbidden());

        verify(classInfoService, never()).getCount(any());
    }

    @Test
    void count_student_forbidden() throws Exception {
        mockMvc.perform(
                        get(URL + "/count")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(classInfoService, never()).getCount(any());
    }

    @Test
    void count_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/count"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void create_admin_success() throws Exception {
        when(classInfoService.create(any(ClassInfoRequest.class)))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("CSE01"))
                .andExpect(jsonPath("$.name")
                        .value("Computer Science 01"))
                .andExpect(jsonPath("$.domain")
                        .value("Computer Science"));

        verify(classInfoService).create(classInfoRequest());
    }

    @Test
    void create_lecturer_success() throws Exception {
        when(classInfoService.create(any(ClassInfoRequest.class)))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("CSE01"));

        verify(classInfoService).create(classInfoRequest());
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

        verify(classInfoService, never()).create(any());
    }

    @Test
    void create_unauthenticated() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(classInfoService, never()).create(any());
    }

    @Test
    void create_invalidJson_400() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid-json}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Nội dung JSON không hợp lệ"));

        verify(classInfoService, never()).create(any());
    }

    @Test
    void create_duplicateId_409() throws Exception {
        when(classInfoService.create(any(ClassInfoRequest.class)))
                .thenThrow(
                        new DuplicateResourceException(
                                "Mã lớp học đã tồn tại: CSE01"
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
                        .value("Mã lớp học đã tồn tại: CSE01"));
    }

    @Test
    void update_admin_success() throws Exception {
        ClassInfoResponse updated =
                new ClassInfoResponse(
                        "CSE01",
                        "Updated Class",
                        "Computer Science",
                        "Updated description"
                );

        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenReturn(updated);

        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"))
                .andExpect(jsonPath("$.name")
                        .value("Updated Class"))
                .andExpect(jsonPath("$.description")
                        .value("Updated description"));

        verify(classInfoService).update(classInfoRequest());
    }

    @Test
    void update_lecturer_success() throws Exception {
        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"));

        verify(classInfoService).update(classInfoRequest());
    }

    @Test
    void update_student_forbidden() throws Exception {
        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("STUDENT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isForbidden());

        verify(classInfoService, never()).update(any());
    }

    @Test
    void update_unauthenticated() throws Exception {
        mockMvc.perform(
                        put(URL + "/CSE01")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(classInfoService, never()).update(any());
    }

    @Test
    void update_mismatchedId_400() throws Exception {
        String body = """
                {
                    "id": "CSE02",
                    "name": "Computer Science 02",
                    "domain": "Computer Science",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isBadRequest());

        verify(classInfoService, never()).update(any());
    }

    @Test
    void update_withoutBodyId_success() throws Exception {
        String body = """
                {
                    "name": "Computer Science 01",
                    "domain": "Computer Science",
                    "description": "Class information"
                }
                """;

        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenReturn(classInfoResponse());

        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("CSE01"));

        verify(classInfoService).update(classInfoRequest());
    }

    @Test
    void update_notFound_404() throws Exception {
        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy lớp học: UNKNOWN"
                        )
                );

        String body = """
                {
                    "id": "UNKNOWN",
                    "name": "Unknown Class",
                    "domain": "Unknown",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        put(URL + "/UNKNOWN")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy lớp học: UNKNOWN"));
    }

    @Test
    void update_duplicateId_409() throws Exception {
        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenThrow(
                        new DuplicateResourceException(
                                "Lớp học đã tồn tại"
                        )
                );

        mockMvc.perform(
                        put(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value("Lớp học đã tồn tại"));
    }

    @Test
    void delete_admin_success() throws Exception {
        mockMvc.perform(
                        delete(URL + "/CSE01")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Class Info deleted successfully"
                ));

        verify(classInfoService).delete("CSE01");
    }

    @Test
    void delete_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/CSE01")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isForbidden());

        verify(classInfoService, never()).delete("CSE01");
    }

    @Test
    void delete_student_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/CSE01")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(classInfoService, never()).delete("CSE01");
    }

    @Test
    void delete_unauthenticated() throws Exception {
        mockMvc.perform(delete(URL + "/CSE01"))
                .andExpect(status().isUnauthorized());

        verify(classInfoService, never()).delete("CSE01");
    }

    @Test
    void delete_notFound_404() throws Exception {
        doThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy lớp học: UNKNOWN"
                )
        ).when(classInfoService).delete("UNKNOWN");

        mockMvc.perform(
                        delete(URL + "/UNKNOWN")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy lớp học: UNKNOWN"));
    }
}
