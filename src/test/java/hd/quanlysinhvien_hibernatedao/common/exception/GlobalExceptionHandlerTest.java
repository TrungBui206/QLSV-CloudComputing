
package hd.quanlysinhvien_hibernatedao.common.exception;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;

import jakarta.validation.Valid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    private LocalValidatorFactoryBean validator;

    @BeforeEach
    void setUp() {
        validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();

        assertNotNull(mockMvc);
    }

    @AfterEach
    void tearDown() {
        validator.destroy();
    }

    @Test
    void resourceNotFound_returns404() throws Exception {
        mockMvc.perform(
                        get("/test/errors/not-found")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy khoa"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/not-found"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    @Test
    void duplicateResource_returns409() throws Exception {
        mockMvc.perform(
                        get("/test/errors/duplicate")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Mã khoa đã tồn tại"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/duplicate"));
    }

    @Test
    void badRequest_returns400() throws Exception {
        mockMvc.perform(
                        get("/test/errors/bad-request")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Dữ liệu không hợp lệ"));
    }

    @Test
    void unsupportedFile_returns400() throws Exception {
        mockMvc.perform(
                        get("/test/errors/unsupported-file")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Định dạng file không được hỗ trợ"));
    }

    @Test
    void storageException_returns500() throws Exception {
        mockMvc.perform(
                        get("/test/errors/storage")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("Có lỗi xảy ra khi xử lý lưu trữ tài liệu"));
    }

    @Test
    void illegalArgument_returns400() throws Exception {
        mockMvc.perform(
                        get("/test/errors/illegal-argument")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ"));
    }

    @Test
    void dataIntegrityViolation_returns409() throws Exception {
        mockMvc.perform(
                        get("/test/errors/data-integrity")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Dữ liệu bị trùng hoặc vi phạm ràng buộc cơ sở dữ liệu"
                        ));
    }

    @Test
    void generalException_returns500() throws Exception {
        mockMvc.perform(
                        get("/test/errors/general")
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error")
                        .value("Internal Server Error"))
                .andExpect(jsonPath("$.message")
                        .value("Có lỗi xảy ra trên máy chủ"));
    }

    @Test
    void validation_blankCode_returns400() throws Exception {
        String requestBody = """
                {
                    "code": "",
                    "name": "Computer Science",
                    "description": "Test"
                }
                """;

        mockMvc.perform(
                        post("/test/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(containsString("code:")))
                .andExpect(jsonPath("$.path")
                        .value("/test/validate"));
    }

    @Test
    void invalidJson_returns400() throws Exception {
        mockMvc.perform(
                        post("/test/validate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{invalid-json}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Nội dung JSON không hợp lệ"));
    }

    @Test
    void typeMismatch_returns400() throws Exception {
        mockMvc.perform(
                        get("/test/numbers/abc")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Tham số không hợp lệ: id"));
    }

    @Test
    void missingParameter_returns400() throws Exception {
        mockMvc.perform(
                        get("/test/required")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Thiếu tham số: keyword"));
    }

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/errors/{type}")
        public void throwException(
                @PathVariable("type") String type
        ) {
            switch (type) {
                case "not-found" ->
                        throw new ResourceNotFoundException(
                                "Không tìm thấy khoa"
                        );

                case "duplicate" ->
                        throw new DuplicateResourceException(
                                "Mã khoa đã tồn tại"
                        );

                case "bad-request" ->
                        throw new BadRequestException(
                                "Dữ liệu không hợp lệ"
                        );

                case "unsupported-file" ->
                        throw new FileExtensionNotSupportedException(
                                "Định dạng file không được hỗ trợ"
                        );

                case "storage" ->
                        throw new StorageException(
                                "MinIO không thể lưu file"
                        );

                case "illegal-argument" ->
                        throw new IllegalArgumentException(
                                "Tham số không hợp lệ"
                        );

                case "data-integrity" ->
                        throw new DataIntegrityViolationException(
                                "Duplicate database entry"
                        );

                default ->
                        throw new IllegalStateException(
                                "Unexpected error"
                        );
            }
        }

        @PostMapping("/validate")
        public void validate(
                @Valid @RequestBody DepartmentRequest request
        ) {
        }

        @GetMapping("/numbers/{id}")
        public void number(
                @PathVariable("id") Long id
        ) {
        }

        @GetMapping("/required")
        public void required(
                @RequestParam("keyword") String keyword
        ) {
        }
    }
}
