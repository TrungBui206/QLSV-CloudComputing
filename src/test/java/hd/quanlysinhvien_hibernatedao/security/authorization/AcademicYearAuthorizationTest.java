
package hd.quanlysinhvien_hibernatedao.security.authorization;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.service.AcademicYearService;

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
class AcademicYearAuthorizationTest extends AuthorizationTestBase {

    private static final String BASE_URL = "/api/academic-years";

    private static final String REQUEST_BODY = """
            {
                "code": "AY2026",
                "name": "Academic Year 2026-2027",
                "description": "Academic year for testing",
                "startYear": 2026,
                "endYear": 2027
            }
            """;

    private static final AcademicYearResponse ACADEMIC_YEAR_RESPONSE =
            new AcademicYearResponse(
                    1L,
                    "AY2026",
                    "Academic Year 2026-2027",
                    "Academic year for testing",
                    2026,
                    2027,
                    null,
                    null
            );

    @MockitoBean
    private AcademicYearService academicYearService;

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanSearchAcademicYears(Role role) throws Exception {

        PageResponse<AcademicYearResponse> pageResponse =
                new PageResponse<>(
                        List.of(ACADEMIC_YEAR_RESPONSE),
                        0,
                        10,
                        1L,
                        1
                );

        when(academicYearService.search(
                "", 0, 10, "id", "asc"
        )).thenReturn(pageResponse);

        mockMvc.perform(
                        get(BASE_URL + "/search")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].code").value("AY2026"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(academicYearService).search(
                "", 0, 10, "id", "asc"
        );
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewAcademicYearById(Role role) throws Exception {

        when(academicYearService.findById(1L))
                .thenReturn(ACADEMIC_YEAR_RESPONSE);

        mockMvc.perform(
                        get(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"))
                .andExpect(jsonPath("$.data.startYear").value(2026))
                .andExpect(jsonPath("$.data.endYear").value(2027));

        verify(academicYearService).findById(1L);
    }

    @Test
    void adminCanCreateAcademicYear() throws Exception {

        when(academicYearService.create(any(AcademicYearRequest.class)))
                .thenReturn(ACADEMIC_YEAR_RESPONSE);

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", bearerToken(Role.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("AY2026"));

        verify(academicYearService)
                .create(any(AcademicYearRequest.class));
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotCreateAcademicYear(Role role)
            throws Exception {

        mockMvc.perform(
                        post(BASE_URL)
                                .header("Authorization", bearerToken(role))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void adminCanUpdateAcademicYear() throws Exception {

        when(academicYearService.update(
                eq(1L),
                any(AcademicYearRequest.class)
        )).thenReturn(ACADEMIC_YEAR_RESPONSE);

        mockMvc.perform(
                        put(BASE_URL + "/1")
                                .header("Authorization", bearerToken(Role.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name")
                        .value("Academic Year 2026-2027"));

        verify(academicYearService)
                .update(eq(1L), any(AcademicYearRequest.class));
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotUpdateAcademicYear(Role role)
            throws Exception {

        mockMvc.perform(
                        put(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void adminCanDeleteAcademicYear() throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/1")
                                .header("Authorization", bearerToken(Role.ADMIN))
                )
                .andExpect(status().isNoContent());

        verify(academicYearService).delete(1L);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotDeleteAcademicYear(Role role)
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/1")
                                .header("Authorization", bearerToken(role))
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void unauthenticatedCannotSearchAcademicYears() throws Exception {

        mockMvc.perform(get(BASE_URL + "/search"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void unauthenticatedCannotViewAcademicYearById() throws Exception {

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void unauthenticatedCannotCreateAcademicYear() throws Exception {

        mockMvc.perform(
                        post(BASE_URL)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void unauthenticatedCannotUpdateAcademicYear() throws Exception {

        mockMvc.perform(
                        put(BASE_URL + "/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(academicYearService);
    }

    @Test
    void unauthenticatedCannotDeleteAcademicYear() throws Exception {

        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(academicYearService);
    }
}
