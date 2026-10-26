package hd.quanlysinhvien_hibernatedao.rest;

import hd.quanlysinhvien_hibernatedao.dto.request.AcademicYearRequest;
import hd.quanlysinhvien_hibernatedao.dto.response.AcademicYearResponse;
import hd.quanlysinhvien_hibernatedao.dto.response.PageResponse;
import hd.quanlysinhvien_hibernatedao.wrapper.ApiResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

public interface AcademicYearRest {

    @Operation(
            summary = "Tìm kiếm niên khóa",
            description = "Tìm kiếm, phân trang và sắp xếp niên khóa"
    )
    @GetMapping("/search")
    ResponseEntity<ApiResponse<PageResponse<AcademicYearResponse>>> search(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    );

    @Operation(
            summary = "Lấy niên khóa theo ID",
            description = "ADMIN, LECTURER và STUDENT được phép xem"
    )
    @GetMapping("/{id}")
    ResponseEntity<ApiResponse<AcademicYearResponse>> getById(
            @PathVariable Long id
    );

    @Operation(
            summary = "Thêm niên khóa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Thêm niên khóa thành công",
            useReturnTypeSchema = true
    )
    @PostMapping
    ResponseEntity<ApiResponse<AcademicYearResponse>> create(
            @Valid @RequestBody AcademicYearRequest request
    );

    @Operation(
            summary = "Cập nhật niên khóa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<AcademicYearResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AcademicYearRequest request
    );

    @Operation(
            summary = "Xóa niên khóa",
            description = "Chỉ ADMIN được phép thực hiện"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "204",
            description = "Xóa niên khóa thành công, không trả về nội dung",
            content = @Content
    )
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(
            @PathVariable Long id
    );
}