
package hd.quanlysinhvien_hibernatedao.common.handler;

import hd.quanlysinhvien_hibernatedao.common.exception.*;
import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;

import jakarta.validation.Valid;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    private LocalValidatorFactoryBean validator;

    // ==========================================
    // 1. SETUP AND CLEANUP
    // ==========================================

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

    // ==========================================
    // 2. RESOURCE NOT FOUND - HTTP 404
    // ==========================================

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
    void noResourceFound_returns404() throws Exception {

        mockMvc.perform(
                        get("/test/errors/no-resource")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Không tìm thấy tài nguyên"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/no-resource"));
    }

    // ==========================================
    // 3. DUPLICATE RESOURCE - HTTP 409
    // ==========================================

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

    // ==========================================
    // 4. BAD REQUEST - HTTP 400
    // ==========================================

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

    // ==========================================
    // 5. UNSUPPORTED FILE EXTENSION
    // ==========================================

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

    // ==========================================
    // 6. STORAGE EXCEPTION - HTTP 500
    // ==========================================

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
                        .value(
                                "Có lỗi xảy ra khi xử lý lưu trữ tài liệu"
                        ));
    }

    // ==========================================
    // 7. ILLEGAL ARGUMENT - HTTP 400
    // ==========================================

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

    // ==========================================
    // 8. DATA INTEGRITY VIOLATION
    // ==========================================

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

    // ==========================================
    // 9. GENERAL EXCEPTION - HTTP 500
    // ==========================================

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

    // ==========================================
    // 10. VALIDATION ERROR
    // ==========================================

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

    // ==========================================
    // 11. INVALID JSON
    // ==========================================

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

    // ==========================================
    // 12. TYPE MISMATCH
    // ==========================================

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

    // ==========================================
    // 13. MISSING REQUEST PARAMETER
    // ==========================================

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

    // ==========================================
    // 14. RESPONSE STATUS - NULL REASON
    // ==========================================

    @Test
    void responseStatusNullReason_returnsDefaultMessage()
            throws Exception {

        mockMvc.perform(
                        get("/test/errors/response-null")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Not Found"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/response-null"))
                .andExpect(jsonPath("$.timestamp").isNotEmpty());
    }

    // ==========================================
    // 15. RESPONSE STATUS - BLANK REASON
    // ==========================================

    @Test
    void responseStatusBlankReason_returnsDefaultMessage()
            throws Exception {

        mockMvc.perform(
                        get("/test/errors/response-blank")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Bad Request"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/response-blank"));
    }

    // ==========================================
    // 16. RESPONSE STATUS - CUSTOM REASON
    // ==========================================

    @Test
    void responseStatusCustomReason_returnsCustomMessage()
            throws Exception {

        mockMvc.perform(
                        get("/test/errors/response-custom")
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error")
                        .value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Sinh viên đã tồn tại"))
                .andExpect(jsonPath("$.path")
                        .value("/test/errors/response-custom"));
    }

    // ==========================================
    // 17. CONTROLLER DUNG RIENG CHO UNIT TEST
    // ==========================================

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @GetMapping("/errors/{type}")
        public void throwException(
                @PathVariable("type") String type
        ) throws NoResourceFoundException {

            switch (type) {

                case "not-found" ->
                        throw new ResourceNotFoundException(
                                "Không tìm thấy khoa"
                        );

                // Mock để không phụ thuộc chữ ký constructor
                // của NoResourceFoundException giữa các phiên bản Spring
                case "no-resource" ->
                        throw org.mockito.Mockito.mock(
                                NoResourceFoundException.class
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

                // Reason = null
                case "response-null" ->
                        throw new ResponseStatusException(
                                HttpStatus.NOT_FOUND
                        );

                // Reason chi co khoang trang
                case "response-blank" ->
                        throw new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "   "
                        );

                // Reason co noi dung cu the
                case "response-custom" ->
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Sinh viên đã tồn tại"
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
