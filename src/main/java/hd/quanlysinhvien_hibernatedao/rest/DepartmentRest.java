package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.DepartmentRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.DepartmentResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface DepartmentRest {

    @Operation(
            summary = "Lấy danh sách khoa",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping
    ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAll();

    @Operation(
            summary = "Tìm kiếm khoa",
            description = "Tìm kiếm, phân trang và sắp xếp danh sách khoa"
    )
    @GetMapping("/search")
    ResponseEntity<ApiResponse<PageResponse<DepartmentResponse>>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @Operation(
            summary = "Lấy khoa theo ID",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<DepartmentResponse>> getById(
            @PathVariable Long id
    );

    @Operation(
            summary = "Thêm khoa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Thêm khoa thành công",
            useReturnTypeSchema = true
    )
    @PostMapping
    ResponseEntity<ApiResponse<DepartmentResponse>> create(
            @Valid @RequestBody DepartmentRequest request
    );

    @Operation(
            summary = "Cập nhật khoa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<DepartmentResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request
    );

    @Operation(
            summary = "Xóa khoa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Xóa khoa thành công, không trả về nội dung",
            content = @Content
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}