package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.StudentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.StudentResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface StudentRest {

    @Operation(
            summary = "Lấy danh sách sinh viên",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping
    ResponseEntity<ApiResponse<List<StudentResponse>>> getAll();

    @Operation(
            summary = "Tìm kiếm sinh viên",
            description = "Tìm kiếm, lọc theo khoa, phân trang và sắp xếp sinh viên"
    )
    @GetMapping("/search")
    ResponseEntity<ApiResponse<PageResponse<StudentResponse>>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @Operation(
            summary = "Lấy sinh viên theo ID",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<StudentResponse>> getById(
            @PathVariable Long id
    );

    @Operation(
            summary = "Thêm sinh viên",
            description = "Chỉ ADMIN và LECTURER được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Thêm sinh viên thành công",
            useReturnTypeSchema = true
    )
    @PostMapping
    ResponseEntity<ApiResponse<StudentResponse>> create(
            @Valid @RequestBody StudentRequest request
    );

    @Operation(
            summary = "Cập nhật sinh viên",
            description = "Chỉ ADMIN và LECTURER được phép thực hiện"
    )
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<StudentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody StudentRequest request
    );

    @Operation(
            summary = "Xóa sinh viên",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Xóa sinh viên thành công, không trả về nội dung",
            content = @Content
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}