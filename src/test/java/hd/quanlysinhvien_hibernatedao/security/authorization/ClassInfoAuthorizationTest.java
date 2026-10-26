package hd.quanlysinhvien_hibernatedao.security.authorization;

import hd.quanlysinhvien_hibernatedao.dto.request.ClassInfoRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.ClassInfoResponse;
import hd.quanlysinhvien_hibernatedao.service.ClassInfoService;

import jakarta.servlet.Filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.Locale;

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
class ClassInfoAuthorizationTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private Filter securityFilterChain;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Value("${app.jwt.issuer}")
    private String issuer;

    @MockitoBean
    private ClassInfoService classInfoService;

    private MockMvc mockMvc;

    private static final String BASE_URL = "/api/classes";

    private static final String REQUEST_BODY = """
            {
                "id": "L001",
                "name": "Lớp kiểm thử",
                "domain": "Information Technology",
                "description": "Kiểm thử phân quyền"
            }
            """;

    private static final ClassInfoResponse CLASS_RESPONSE =
            new ClassInfoResponse(
                    "L001",
                    "Lớp kiểm thử",
                    "Information Technology",
                    "Kiểm thử phân quyền"
            );

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .addFilters(securityFilterChain)
                .build();
    }

    @ParameterizedTest
    @EnumSource(Role.class)
    void allRolesCanViewClasses(Role role) throws Exception {
        when(classInfoService.getAll())
                .thenReturn(List.of(CLASS_RESPONSE));

        mockMvc.perform(
                        get(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id")
                        .value("L001"));

        verify(classInfoService).getAll();
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "LECTURER"}
    )
    void adminAndLecturerCanCreateClasses(Role role)
            throws Exception {

        when(classInfoService.create(any(ClassInfoRequest.class)))
                .thenReturn(CLASS_RESPONSE);

        mockMvc.perform(
                        post(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isCreated());

        verify(classInfoService)
                .create(any(ClassInfoRequest.class));
    }

    @Test
    void studentCannotCreateClasses() throws Exception {
        mockMvc.perform(
                        post(BASE_URL)
                                .header(
                                        "Authorization",
                                        bearerToken(Role.STUDENT)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(classInfoService);
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"ADMIN", "LECTURER"}
    )
    void adminAndLecturerCanUpdateClasses(Role role)
            throws Exception {

        when(classInfoService.update(any(ClassInfoRequest.class)))
                .thenReturn(CLASS_RESPONSE);

        mockMvc.perform(
                        put(BASE_URL + "/L001")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isOk());

        verify(classInfoService)
                .update(any(ClassInfoRequest.class));
    }

    @Test
    void studentCannotUpdateClasses() throws Exception {
        mockMvc.perform(
                        put(BASE_URL + "/L001")
                                .header(
                                        "Authorization",
                                        bearerToken(Role.STUDENT)
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(REQUEST_BODY)
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(classInfoService);
    }

    @Test
    void adminCanDeleteClasses() throws Exception {
        mockMvc.perform(
                        delete(BASE_URL + "/L001")
                                .header(
                                        "Authorization",
                                        bearerToken(Role.ADMIN)
                                )
                )
                .andExpect(status().isOk());

        verify(classInfoService).delete("L001");
    }

    @ParameterizedTest
    @EnumSource(
            value = Role.class,
            names = {"LECTURER", "STUDENT"}
    )
    void lecturerAndStudentCannotDeleteClasses(Role role)
            throws Exception {

        mockMvc.perform(
                        delete(BASE_URL + "/L001")
                                .header(
                                        "Authorization",
                                        bearerToken(role)
                                )
                )
                .andExpect(status().isForbidden());

        verifyNoInteractions(classInfoService);
    }

    private String bearerToken(Role role) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .subject(
                        role.name().toLowerCase(Locale.ROOT)
                )
                .claim(
                        "roles",
                        List.of(role.name())
                )
                .build();

        JwsHeader header = JwsHeader
                .with(MacAlgorithm.HS256)
                .build();

        String token = jwtEncoder.encode(
                JwtEncoderParameters.from(
                        header,
                        claims
                )
        ).getTokenValue();

        return "Bearer " + token;
    }
}