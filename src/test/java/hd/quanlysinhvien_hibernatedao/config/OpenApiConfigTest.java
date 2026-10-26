package hd.quanlysinhvien_hibernatedao.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class OpenApiConfigTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .build();
    }

    @Test
    void shouldContainOpenApiInformation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.info.title",
                        is("Student Management System API")
                ))
                .andExpect(jsonPath(
                        "$.info.version",
                        is("1.0.0")
                ));
    }

    @Test
    void shouldRegisterBearerAuthentication() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.components.securitySchemes.bearerAuth.type",
                        is("http")
                ))
                .andExpect(jsonPath(
                        "$.components.securitySchemes.bearerAuth.scheme",
                        is("bearer")
                ));
    }

    @Test
    void shouldRegisterApiErrorSchema() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.components.schemas.ApiError"
                ).exists());
    }

    @Test
    void shouldRequireJwtForClasses() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].get.security[0].bearerAuth"
                ).isArray());
    }

    @Test
    void shouldNotRequireJwtForLogin() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/auth/login'].post.security"
                ).doesNotExist());
    }

    @Test
    void shouldDocumentUnauthorizedError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].get.responses['401'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentForbiddenError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].get.responses['403'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentValidationError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].post.responses['400'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentNotFoundError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes/{id}'].get.responses['404'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentConflictError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].post.responses['409'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentServerError() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes'].get.responses['500'].content['application/json'].schema['$ref']",
                        is("#/components/schemas/ApiError")
                ));
    }

    @Test
    void shouldDocumentClassInfoDeleteAs200() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/classes/{id}'].delete.responses['200']"
                ).exists());
    }

    @Test
    void shouldDocumentStudentDeleteAs204() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/students/{id}'].delete.responses['204']"
                ).exists());
    }

    @Test
    void shouldDocumentDocumentDownloadAsBinary() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(
                        "$.paths['/api/documents/download/{id}'].get.responses['200'].content['application/octet-stream'].schema.format",
                        is("binary")
                ));
    }
}