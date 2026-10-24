
package hd.quanlysinhvien_hibernatedao.rest.impl;

import hd.quanlysinhvien_hibernatedao.common.exception.DuplicateResourceException;
import hd.quanlysinhvien_hibernatedao.common.exception.ResourceNotFoundException;
import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.service.StudentService;

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
import java.time.LocalDate;
import java.util.List;

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
class StudentRestImplTest {

    private static final String URL = "/api/students";

    private static final String VALID_REQUEST = """
            {
                "name": "Nguyen Van A",
                "email": "student.a@example.com",
                "dob": "2004-01-15",
                "departmentId": 1
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
    private StudentService studentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    private StudentResponse studentResponse() {
        return new StudentResponse(
                1L,
                "Nguyen Van A",
                "student.a@example.com",
                LocalDate.of(2004, 1, 15),
                1L,
                "Computer Science",
                null,
                null
        );
    }

    private StudentRequest studentRequest() {
        return new StudentRequest(
                "Nguyen Van A",
                "student.a@example.com",
                LocalDate.of(2004, 1, 15),
                1L
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
        when(studentService.findAll())
                .thenReturn(List.of(studentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name")
                        .value("Nguyen Van A"))
                .andExpect(jsonPath("$[0].email")
                        .value("student.a@example.com"))
                .andExpect(jsonPath("$[0].departmentId").value(1))
                .andExpect(jsonPath("$[0].departmentName")
                        .value("Computer Science"));

        verify(studentService).findAll();
    }

    @Test
    void getAll_lecturer_success() throws Exception {
        when(studentService.findAll())
                .thenReturn(List.of(studentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Nguyen Van A"));

        verify(studentService).findAll();
    }

    @Test
    void getAll_student_success() throws Exception {
        when(studentService.findAll())
                .thenReturn(List.of(studentResponse()));

        mockMvc.perform(
                        get(URL)
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));

        verify(studentService).findAll();
    }

    @Test
    void getAll_emptyList() throws Exception {
        when(studentService.findAll())
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

        verify(studentService, never()).findAll();
    }

    @Test
    void search_defaultParameters() throws Exception {
        PageResponse<StudentResponse> response =
                new PageResponse<>(
                        List.of(studentResponse()),
                        0,
                        10,
                        1L,
                        1
                );

        when(studentService.search(
                "", null, 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].name")
                        .value("Nguyen Van A"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(studentService).search(
                "", null, 0, 10, "id", "asc"
        );
    }

    @Test
    void search_customParameters() throws Exception {
        PageResponse<StudentResponse> response =
                new PageResponse<>(
                        List.of(studentResponse()),
                        1,
                        5,
                        6L,
                        2
                );

        when(studentService.search(
                "nguyen", 1L, 1, 5, "name", "desc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("LECTURER"))
                                .param("keyword", "nguyen")
                                .param("departmentId", "1")
                                .param("page", "1")
                                .param("size", "5")
                                .param("sortBy", "name")
                                .param("direction", "desc")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].departmentId").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(6))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(studentService).search(
                "nguyen", 1L, 1, 5, "name", "desc"
        );
    }

    @Test
    void search_emptyResult() throws Exception {
        PageResponse<StudentResponse> response =
                new PageResponse<>(
                        List.of(),
                        0,
                        10,
                        0L,
                        0
                );

        when(studentService.search(
                "unknown", null, 0, 10, "id", "asc"
        )).thenReturn(response);

        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("keyword", "unknown")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void search_invalidDepartmentId_400() throws Exception {
        mockMvc.perform(
                        get(URL + "/search")
                                .header("Authorization", token("ADMIN"))
                                .param("departmentId", "abc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ: departmentId"));
    }

    @Test
    void search_unauthenticated() throws Exception {
        mockMvc.perform(get(URL + "/search"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getById_admin_success() throws Exception {
        when(studentService.findById(1L))
                .thenReturn(studentResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Nguyen Van A"))
                .andExpect(jsonPath("$.email")
                        .value("student.a@example.com"))
                .andExpect(jsonPath("$.dob").value("2004-01-15"))
                .andExpect(jsonPath("$.departmentName")
                        .value("Computer Science"));

        verify(studentService).findById(1L);
    }

    @Test
    void getById_student_success() throws Exception {
        when(studentService.findById(1L))
                .thenReturn(studentResponse());

        mockMvc.perform(
                        get(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(studentService).findById(1L);
    }

    @Test
    void getById_notFound_404() throws Exception {
        when(studentService.findById(999L))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy sinh viên với ID: 999"
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
                        .value("Không tìm thấy sinh viên với ID: 999"))
                .andExpect(jsonPath("$.path")
                        .value("/api/students/999"));
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

        verify(studentService, never()).findById(1L);
    }

    @Test
    void create_admin_success() throws Exception {
        when(studentService.create(any(StudentRequest.class)))
                .thenReturn(studentResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name")
                        .value("Nguyen Van A"))
                .andExpect(jsonPath("$.email")
                        .value("student.a@example.com"))
                .andExpect(jsonPath("$.departmentId").value(1));

        verify(studentService).create(studentRequest());
    }

    @Test
    void create_lecturer_success() throws Exception {
        when(studentService.create(any(StudentRequest.class)))
                .thenReturn(studentResponse());

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));

        verify(studentService).create(studentRequest());
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

        verify(studentService, never())
                .create(any(StudentRequest.class));
    }

    @Test
    void create_unauthenticated() throws Exception {
        mockMvc.perform(
                        post(URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(studentService, never())
                .create(any(StudentRequest.class));
    }

    @Test
    void create_blankName_400() throws Exception {
        String body = """
                {
                    "name": "",
                    "email": "student.a@example.com",
                    "dob": "2004-01-15",
                    "departmentId": 1
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
                        .value(org.hamcrest.Matchers.containsString("name:")));

        verify(studentService, never()).create(any());
    }

    @Test
    void create_invalidEmail_400() throws Exception {
        String body = """
                {
                    "name": "Nguyen Van A",
                    "email": "invalid-email",
                    "dob": "2004-01-15",
                    "departmentId": 1
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
                        .value(org.hamcrest.Matchers.containsString("email:")));

        verify(studentService, never()).create(any());
    }

    @Test
    void create_futureDob_400() throws Exception {
        String body = """
                {
                    "name": "Nguyen Van A",
                    "email": "student.a@example.com",
                    "dob": "2099-01-01",
                    "departmentId": 1
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
                        .value(org.hamcrest.Matchers.containsString("dob:")));

        verify(studentService, never()).create(any());
    }

    @Test
    void create_missingDepartmentId_400() throws Exception {
        String body = """
                {
                    "name": "Nguyen Van A",
                    "email": "student.a@example.com",
                    "dob": "2004-01-15"
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
                        .value(org.hamcrest.Matchers.containsString("departmentId:")));

        verify(studentService, never()).create(any());
    }

    @Test
    void create_duplicateEmail_409() throws Exception {
        when(studentService.create(any(StudentRequest.class)))
                .thenThrow(
                        new DuplicateResourceException(
                                "Email sinh viên đã tồn tại"
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
                        .value("Email sinh viên đã tồn tại"));
    }

    @Test
    void create_departmentNotFound_404() throws Exception {
        when(studentService.create(any(StudentRequest.class)))
                .thenThrow(
                        new ResourceNotFoundException(
                                "Không tìm thấy khoa với ID: 1"
                        )
                );

        mockMvc.perform(
                        post(URL)
                                .header("Authorization", token("ADMIN"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy khoa với ID: 1"));
    }

    @Test
    void update_admin_success() throws Exception {
        StudentResponse updated = new StudentResponse(
                1L,
                "Nguyen Van B",
                "student.b@example.com",
                LocalDate.of(2004, 1, 15),
                1L,
                "Computer Science",
                null,
                null
        );

        when(studentService.update(
                eq(1L),
                any(StudentRequest.class)
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
                        .value("Nguyen Van B"))
                .andExpect(jsonPath("$.email")
                        .value("student.b@example.com"));

        verify(studentService).update(1L, studentRequest());
    }

    @Test
    void update_lecturer_success() throws Exception {
        when(studentService.update(
                eq(1L),
                any(StudentRequest.class)
        )).thenReturn(studentResponse());

        mockMvc.perform(
                        put(URL + "/1")
                                .header("Authorization", token("LECTURER"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(studentService).update(1L, studentRequest());
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

        verify(studentService, never())
                .update(eq(1L), any(StudentRequest.class));
    }

    @Test
    void update_unauthenticated() throws Exception {
        mockMvc.perform(
                        put(URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_REQUEST)
                )
                .andExpect(status().isUnauthorized());

        verify(studentService, never())
                .update(eq(1L), any(StudentRequest.class));
    }

    @Test
    void update_invalidRequest_400() throws Exception {
        String body = """
                {
                    "name": "",
                    "email": "invalid-email",
                    "dob": "2099-01-01",
                    "departmentId": null
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

        verify(studentService, never())
                .update(eq(1L), any(StudentRequest.class));
    }

    @Test
    void update_notFound_404() throws Exception {
        when(studentService.update(
                eq(999L),
                any(StudentRequest.class)
        )).thenThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với ID: 999"
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
                        .value("Không tìm thấy sinh viên với ID: 999"));
    }

    @Test
    void update_duplicateEmail_409() throws Exception {
        when(studentService.update(
                eq(1L),
                any(StudentRequest.class)
        )).thenThrow(
                new DuplicateResourceException(
                        "Email sinh viên đã tồn tại"
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
                        .value("Email sinh viên đã tồn tại"));
    }

    @Test
    void delete_admin_success() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNoContent());

        verify(studentService).delete(1L);
    }

    @Test
    void delete_lecturer_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("LECTURER"))
                )
                .andExpect(status().isForbidden());

        verify(studentService, never()).delete(1L);
    }

    @Test
    void delete_student_forbidden() throws Exception {
        mockMvc.perform(
                        delete(URL + "/1")
                                .header("Authorization", token("STUDENT"))
                )
                .andExpect(status().isForbidden());

        verify(studentService, never()).delete(1L);
    }

    @Test
    void delete_unauthenticated() throws Exception {
        mockMvc.perform(delete(URL + "/1"))
                .andExpect(status().isUnauthorized());

        verify(studentService, never()).delete(1L);
    }

    @Test
    void delete_notFound_404() throws Exception {
        doThrow(
                new ResourceNotFoundException(
                        "Không tìm thấy sinh viên với ID: 999"
                )
        ).when(studentService).delete(999L);

        mockMvc.perform(
                        delete(URL + "/999")
                                .header("Authorization", token("ADMIN"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy sinh viên với ID: 999"));
    }
}
