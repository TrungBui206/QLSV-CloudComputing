
package hd.quanlysinhvien_hibernatedao.security.authorization;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.service.DepartmentService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
class DepartmentAuthorizationTest extends AuthorizationTestBase {

    private static final String BASE_URL = "/api/departments";

    private static final String REQUEST_BODY = """
            {
                "code": "CSE",
                "name": "Computer Science",
                "description": "Computer Science Department"
            }
            """;

    private static final DepartmentResponse DEPARTMENT_RESPONSE =
            new DepartmentResponse(
                    1L,
                    "CSE",
                    "Computer Science",
                    "Computer Science Department",
                    null,
                    null
            );

    @MockitoBean
    private DepartmentService departmentService;

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewDepartments(Role role) throws Exception {

        when(departmentService.findAll())
                .thenReturn(List.of(DEPARTMENT_RESPONSE));

        mockMvc.perform(
                        get(BASE_URL)
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("CSE"));

        verify(departmentService).findAll();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanSearchDepartments(Role role) throws Exception {

        PageResponse<DepartmentResponse> pageResponse =
                new PageResponse<>(
                        List.of(DEPARTMENT_RESPONSE),
                        0,
                        10,
                        1L,
                        1
                );

        when(departmentService.search(
                "", 0, 10, "id", "asc"
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get(BASE_URL + "/search")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(departmentService).search(
                "", 0, 10, "id", "asc"
        );
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewDepartmentById(Role role) throws Exception {

        when(departmentService.findById(1L))
                .thenReturn(DEPARTMENT_RESPONSE);

        mockMvc.perform(
                        get(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Computer Science"));

        verify(departmentService).findById(1L);
    }

    @Test
    void adminCanCreateDepartment() throws Exception {

        when(departmentService.create(any(DepartmentRequest.class)))
                .thenReturn(DEPARTMENT_RESPONSE);

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", bearerToken(Role.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(departmentService)
                .create(any(DepartmentRequest.class));
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotCreateDepartment(Role role)
            throws Exception {

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", bearerToken(role))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(departmentService);
    }

    @Test
    void adminCanUpdateDepartment() throws Exception {

        when(departmentService.update(
                eq(1L),
                any(DepartmentRequest.class)
        )).thenReturn(DEPARTMENT_RESPONSE);

        mockMvc.perform(
                        put(BASE_URL + "/1")
                                .header("Authorization", bearerToken(Role.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1));

        verify(departmentService)
                .update(eq(1L), any(DepartmentRequest.class));
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotUpdateDepartment(Role role)
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(departmentService);
    }

    @Test
    void adminCanDeleteDepartment() throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/1")
                                .header("Authorization", bearerToken(Role.ADMIN))
                )
                .andExpect(status().isNoContent());

        verify(departmentService).delete(1L);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotDeleteDepartment(Role role)
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(departmentService);
    }

    @Test
    void unauthenticatedUserCannotViewDepartments() throws Exception {

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(departmentService);
    }
}
