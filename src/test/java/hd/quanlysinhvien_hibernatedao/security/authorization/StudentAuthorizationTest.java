package hd.quanlysinhvien_hibernatedao.security.authorization;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.service.StudentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class StudentAuthorizationTest extends AuthorizationTestBase {

    @MockitoBean
    private StudentService studentService;

    private static final String BASE_URL = "/api/students";

    private static final String REQUEST_BODY = """
            {
                "name": "Sinh viên kiểm thử",
                "email": "student.test@example.com",
                "dob": "2002-04-15",
                "departmentId": 1
            }
            """;

    private static final StudentResponse STUDENT_RESPONSE =
            new StudentResponse(
                    101L,
                    "Sinh viên kiểm thử",
                    "student.test@example.com",
                    LocalDate.of(2002, 4, 15),
                    1L,
                    "Công nghệ thông tin",
                    null,
                    null
            );

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewStudents(Role role) throws Exception {
        when(studentService.findAll())
                .thenReturn(List.of(STUDENT_RESPONSE));

        mockMvc.perform(
                        get(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id")
                        .value(101));

        verify(studentService).findAll();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewStudentById(Role role)
            throws Exception {

        when(studentService.findById(101L))
                .thenReturn(STUDENT_RESPONSE);

        mockMvc.perform(
                        get(BASE_URL + "/101")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name")
                        .value("Sinh viên kiểm thử"));

        verify(studentService).findById(101L);
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanSearchStudents(Role role)
            throws Exception {

        PageResponse<StudentResponse> page =
                new PageResponse<>(
                        List.of(STUDENT_RESPONSE),
                        0,
                        10,
                        1L,
                        1
                );

        when(studentService.search(
                "",
                null,
                0,
                10,
                "id",
                "asc"
        )).thenReturn(page);

        mockMvc.perform(
                        get(BASE_URL + "/search")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].id")
                        .value(101));

        verify(studentService).search(
                "",
                null,
                0,
                10,
                "id",
                "asc"
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "LECTURER"}
    )
    void adminAndLecturerCanCreateStudents(Role role)
            throws Exception {

        when(studentService.create(any(StudentRequest.class)))
                .thenReturn(STUDENT_RESPONSE);

        mockMvc.perform(
                        post(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id")
                        .value(101));

        verify(studentService)
                .create(any(StudentRequest.class));
    }

    @Test
    void studentCannotCreateStudents() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(Role.STUDENT)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403));

        verifyNoInteractions(studentService);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "LECTURER"}
    )
    void adminAndLecturerCanUpdateStudents(Role role)
            throws Exception {

        when(studentService.update(
                org.mockito.ArgumentMatchers.eq(101L),
                any(StudentRequest.class)
        )).thenReturn(STUDENT_RESPONSE);

        mockMvc.perform(
                        put(BASE_URL + "/101")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(101));

        verify(studentService).update(
                org.mockito.ArgumentMatchers.eq(101L),
                any(StudentRequest.class)
        );
    }

    @Test
    void studentCannotUpdateStudents() throws Exception {
        mockMvc.perform(
                        put(BASE_URL + "/101")
                                .header(
                                        "Authorization",
                                        bearerToken(Role.STUDENT)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(studentService);
    }

    @Test
    void adminCanDeleteStudents() throws Exception {
        mockMvc.perform(
                        delete(BASE_URL + "/101")
                                .header(
                                        "Authorization",
                                        bearerToken(Role.ADMIN)
                                )
                )
                .andExpect(status().isNoContent());

        verify(studentService).delete(101L);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotDeleteStudents(Role role)
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/101")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status")
                        .value(403));

        verifyNoInteractions(studentService);
    }

    @Test
    void unauthenticatedCannotViewStudents()
            throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status")
                        .value(401));

        verifyNoInteractions(studentService);
    }
}